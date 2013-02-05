/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 5 - 6 :: 3 : 13
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

package util.time.sequence.timerclock

import util.time.sequence.TimeDependent


class ClockResampler(private val td: TimeDependent) {

	private var errorSum = 0.0;
	private var deltaSum = 0.0;
	private var currentTick: Long = 0;
	private var oldTick : Long = 0;

	private var ticksOnHold: Long = 0;
	private var ticksFastForward: Long = 0;

	// FIXME: okay? bresenham style
	def updateTick(tick: Long, bpmQuot: Double) = {

		val tickmult = (bpmQuot).asInstanceOf[Int]
		val delta =  (bpmQuot) -tickmult.asInstanceOf[Double]

		deltaSum = deltaSum + delta

		if ((tick % tickmult) == 0) {
			currentTick = currentTick + 1
		}

		if(deltaSum > 100) {
			ticksOnHold = deltaSum.asInstanceOf[Long]
			ticksFastForward = 0;

		} else if(deltaSum < -100) {
			ticksFastForward = -1*deltaSum.asInstanceOf[Long]
			ticksOnHold = 0;
		}

		ticksOnHold = ticksOnHold - 1;

		//System.err.println(tickmult +" " +deltaSum+" " + delta + " " + currentTick + " " + bpmQuot + " T " + tick+ " "+ticksFastForward+ " hold "+ticksOnHold)

		if(ticksOnHold <= 0) {
			while(ticksFastForward > 0) {
				td.emitTick(currentTick)
				currentTick = currentTick + 1;
				ticksFastForward = ticksFastForward -1;
				deltaSum = deltaSum + delta;
			}

			if(oldTick < currentTick) {
					td.emitTick(currentTick)
			}

		} else {
			deltaSum = deltaSum - 2*delta;
		}


		oldTick = currentTick;
	}
}
