/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2011 - 5 - 5 :: 5 : 41
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

import org.lodsb.reakt.async.VarA
import timerclock.ClockResampler
import util.time.Beats
import org.lodsb.reakt.sync.{IOS, VarS}

trait TimeDependent {
	var ticks = new IOS[Long](0,0)

	def emitTick(tick: Long) = {
		//no feedback!
		ticks.out.emit(tick)
	}
}


class Player extends TimeDependent {

	private val clkResampler = new ClockResampler(this)
	private var currentBpm = 120.0;
	private var resolutionNanoseconds:Double = Beats.bpm2ms(currentBpm)*1e6/128.0;
	private var resQuot : Double = (this.resolutionNanoseconds/Sequencer.resolutionNanoseconds)

	var run = false;

	var bpm = new VarA[Double](currentBpm)
	bpm.observe(x =>{currentBpm = x;
			        	resolutionNanoseconds = Beats.bpm2ms(currentBpm)*1e6/128.0;
					    resQuot = (this.resolutionNanoseconds/Sequencer.resolutionNanoseconds)
						true
					})

	System.err.println(this.resolutionNanoseconds+ " rss "+resQuot)



	var oldTime = System.nanoTime
	var count = 1;

	private def process(tick: Long) : Unit  = {
		if(count % 10000 == 0) {
			//System.err.println((System.nanoTime-oldTime)/count)
			oldTime = System.nanoTime
			count = 0;
		}

		count = count +1;

		if(run) {clkResampler.updateTick(tick, resQuot)}
	}

	ticks.in.observe(x => {
		process(x);
		true
	})
}

trait Sequence[T] extends TimeDependent {
	val init: T
	val data = new VarS[T](init)

	protected def process(ticks: Long): Unit = {}

	ticks.in.observe(x => {
		process(x);
		true
	})
}
