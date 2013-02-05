/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 3 - 22 :: 6 : 31
    >>  Origin: mt4j (project) / mt4j_mod (module)
    >>
  +3>>
    >>  Copyright (c) 2011:
    >>
    >>     |             |     |
    >>     |    ,---.,---|,---.|---.
    >>     |    |   ||   |`---.|   |
    >>     `---'`---'`---'`---'`---'
    >>                    // Niklas Klügel
    >>
  +4>>
    >>  Made in Bavaria by fat little elves - since 1983.
 */

package org.lodsb.reakt.time

// original from:
/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2007-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

import java.util.{Timer, TimerTask, Date}
import org.lodsb.reakt.async.{SignalA, EventSourceA}
import org.lodsb.reakt.time.time.TimeValue
import org.lodsb.reakt.time.time._

object Clock {
	lazy val milliseconds = withResolution(1.0 msecs)
	lazy val seconds = withResolution(1.0 secs)

	private val timer = new Timer

	/**
	 * An event stream that fires one event after the given time
	 */
	def in(t: Time): EventSourceA[TimeValue, Vortex] = {
		val evt = new EventSourceA[TimeValue, Vortex](Vortex,Vortex);
		doIn(t) {
			evt.emit(Now.apply)
		}

		evt
	}

	/**
	 * An event stream that fires one event after the given date
	 */
	def at(d: Date): EventSourceA[Date, Vortex] = {
		val evt = new EventSourceA[Date, Vortex](Vortex, Vortex);
		doAt(d) {
			evt.emit(d)
		}

		evt
	}

	/**
	 * An event stream that periodically fires an event
	 */
	def every(period: Time): EventSourceA[Unit, Vortex] = {
		val evt = new EventSourceA[Unit, Vortex]((), Vortex);
		doEvery(period) {
			evt.emit(())
		}

		evt
	}

	def withResolution(period: Time): SignalA[Time] = {
		val t0 = (System.nanoTime nsecs)
		val evt = new SignalA[Time](0.0 nsecs)
		doEvery(period) {
			evt.emit(((System.nanoTime nsecs) - t0))
		}

		evt
	}

	def doEvery(msecs: Long)(op: => Unit) {
		timer.schedule(task(op), 0, msecs)
	}

	def doEvery(time: Time)(op: => Unit) {
		doEvery(math.round(time.toMilliSeconds)) {
			op
		}
	}

	def doIn(msecs: Long)(op: => Unit) {
		timer.schedule(task(op), msecs)
	}

	def doIn(time: Time)(op: => Unit) {
		doIn(math.round(time.toMilliSeconds)) {
			op
		}
	}

	def doAt(date: Date)(op: => Unit) {
		timer.schedule(task(op), date)
	}

	protected def task(op: => Unit) = new TimerTask {
		def run {
			op
		}
	}
}
