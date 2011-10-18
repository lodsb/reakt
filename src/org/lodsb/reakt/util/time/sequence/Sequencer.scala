/*
  +1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 5 - 5 :: 2 : 18
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

package util.time.sequence

import timerclock.HighResolutionClock
import org.lodsb.reakt.sync.ValS
import util.time.Beats


object Sequencer extends SequencerInterface {
	private val clock = HighResolutionClock.createClock

	// resolution => 512th note = 0.01ms
	val resolutionNanoseconds: Long = 10000;
	// 0.01 ms internal resolution
	val resolutionMilliseconds:Double = resolutionNanoseconds.asInstanceOf[Double] / 1000000.0;
	val resolutionBpm = Beats.ms2bpm(resolutionMilliseconds * 512.0)
	//    0.000000001

	val ticks = new ValS[Long](0);

	clock.setSequencer(this)
	clock.setInterval(resolutionNanoseconds)
	clock.start

	var run = false

	def processTick(tick: Long) = {

		if (run) {
			ticks.emit(tick);
		}
		true;
	}

	def run_ = (running: Boolean) => {
		this.run = running; if (!run) {
			clock.stopClock
		}
	}


	def main(args: Array[String]): Unit = {
		println("---- SeqBpm " + Sequencer.resolutionBpm)

		val player = new Player

		Sequencer.ticks ~> player.ticks.in
		Sequencer.run = true;

		var count = 1;
		var cnt:Int = 1;
		var oldTime = System.nanoTime

		player.ticks.out.observe(x => {
			if (cnt % 1000 == 0) {
				println((System.nanoTime - oldTime) / cnt);
				cnt = 0;
				oldTime = System.nanoTime;
			}
			count = count + 1;
			cnt = cnt + 1
			true;
		})

		player.run = true;






		var startTime = System.nanoTime

		try {
			Thread.sleep(1000 * 30)
		}
		catch {
			case e: InterruptedException => {
				e.printStackTrace
			}
		}



		var end: Long = System.nanoTime



		System.out.println((end - startTime) / (count.asInstanceOf[Double]))
		println(count)
	}

	Sequencer.run = false;
}


