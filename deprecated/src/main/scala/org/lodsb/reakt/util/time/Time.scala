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

import org.lodsb.reakt.time.time.TimeValue
import org.lodsb.reakt.time.time._
import java.util.{Calendar, Date}

//original from:
/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2007-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

object Vortex extends Vortex with Numeric[Any] {
	val vortexTime = new Time(this.toDouble())

    override def days= vortexTime
    override def hours= vortexTime
    override def mins= vortexTime
    override def secs= vortexTime
    override def msecs= vortexTime
    override def nsecs= vortexTime


	override def toDouble(x: Any): Double = Double.NaN
	override def toFloat(x: Any) = Float.NaN
	override def toLong(x: Any) = Double.NaN.longValue
	override def toInt(x: Any) = Double.NaN.intValue
	override def fromInt(x:Int) = this
	override def negate(x:Any) = this
	override def times(x: Any, y:Any): Vortex = this
	override def minus(x: Any, y: Any):Vortex = this
	override def plus(x:Any, y: Any):Vortex = this

	override def compare(x: Any, y:Any) = -1

	//def compare(that: Any) = -1

	override def toString = "Vortex"
}

abstract class Vortex extends Date with TimeValue


object Now extends TimeValue {

	def days = this.apply.days
	def hours = this.apply.hours
	def mins = this.apply.mins
	def secs = this.apply.secs
	def msecs = this.apply.msecs
	def nsecs = this.apply.nsecs

	def apply = {
		val v = Calendar.getInstance
		new TimeValue {
			def days = new Time(v.get(Calendar.DAY_OF_YEAR) * days2Secs)

			def hours = new Time(v.get(Calendar.HOUR_OF_DAY) * hours2Secs)

			def mins = new Time(v.get(Calendar.MINUTE) * mins2Secs)

			def secs = new Time(v.get(Calendar.SECOND));

			def msecs = new Time(System.nanoTime*1000.0*1000.0 * msecs2Secs)

			def nsecs = new Time(System.nanoTime * nsecs2Secs)
		}
	}
}

class Time(protected val rep: Double) extends Ordered[Time] with Numeric[Time] {
	def +(that: Time) = new Time(rep + that.rep)

	def -(that: Time) = new Time(rep - that.rep)

	def /(that: Time) = this.rep / that.rep

	def /(x: Double) = new Time(rep / x)

	def compare(that: Time) = math.signum(that.rep - this.rep).toInt

	def toDays = rep / days2Secs

	def toHours = rep / hours2Secs

	def toMinutes = rep / mins2Secs

	def toSeconds = rep

	def toMilliSeconds = rep / msecs2Secs

	def toNanoSeconds = rep / nsecs2Secs

	override def toString = rep + " secs"

	override def toDouble(x: Time): Double = x.rep
	override def toFloat(x: Time) = x.rep.floatValue
	override def toLong(x: Time) = x.rep.longValue
	override def toInt(x: Time) = x.rep.toInt
	override def fromInt(x:Int) = new Time(x)
	override def negate(x:Time) = new Time(- x.rep)
	override def times(x: Time, y:Time): Time = new Time(y.rep * x.rep)// EH?!?! not vortex?
	override def minus(x: Time, y: Time):Time = new Time(x.rep - y.rep)
	override def plus(x:Time, y: Time):Time = new Time(x.rep + y.rep)

	override def compare(x: Time, y:Time) = x.rep.compare(y.rep)

}
