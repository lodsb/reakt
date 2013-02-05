/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 5 - 5 :: 3 : 26
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

package util.time

import org.lodsb.reakt.time.time.TimeValue
import org.lodsb.reakt.time.{Time}

object Beats {
	def beats2ms(bpm: Double,
				 bar: Double,
				 quarter: Double = 0,
				 sixteenth: Double = 0,
				 sixtyforth: Double = 0,
				 hundredtwentyeigth: Double = 0,
				 fivehundredtwelfth: Double = 0): Double = {

		val quarternotes = 4.0 * bar + 1.0 * quarter + sixteenth / 16.0 +
			sixtyforth / 64.0 + hundredtwentyeigth / 128.0 + fivehundredtwelfth / 512.0;

		bpm2ms(bpm) * quarternotes;
	}

	def bpm2ms(bpm: Double): Double = {
		(bpm / 60.0)*1000.0
	}

	def ms2bpm(ms: Double): Double = {
		60000.0/ms
	}

	// bar::quarternote::sixteenth::sixtyforth::hundredtwentyeight::fivehundredtwelfth
	def apply(beats: List[Int]): Beats = {
		val a = new Array[Int](6);

		val idx = 0;
		beats.foreach(x => {
			a(idx) = x
		})

		new Beats(a(0), a(1), a(2), a(3), a(4), a(5));
	}
}

// bar::quarternote::sixteenth::sixtyforth::hundredtwentyeight::fivehundredtwelfth
class Beats(val bar: Int = 0, val n4: Int = 0, val n16: Int = 0,
			val n64: Int = 0, val n128: Int = 0, val n512: Int = 0) {

	// TODO: add ops
	val nsecs2Secs = 0.000000001
	val msecs2Secs = 0.001
	val mins2Secs = 60
	val hours2Secs = mins2Secs * 60
	val days2Secs = hours2Secs * 24

	def at(bpm: Double): TimeValue = {
		val v = Beats.beats2ms(bar, n4, n16, n64, n128, n512) * 1000.0;

		new TimeValue {
			def days = new Time(v * days2Secs)

			def hours = new Time(v * hours2Secs)

			def mins = new Time(v * mins2Secs)

			def secs = new Time(v)

			def msecs = new Time(v * msecs2Secs)

			def nsecs = new Time(v * nsecs2Secs)
		}
	}
}
