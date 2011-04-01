
/*
 * +1>>  This source code is licensed as GPLv3 if not stated otherwise.
 *    >>  NO responsibility taken for ANY harm, damage done
 *    >>  to you, your data, animals, etc.
 *    >>
 *  +2>>
 *    >>  Last modified:  2011 - 3 - 21 :: 4 : 26
 *    >>  Origin: eventgraphtest (project) / reaktExt (module)
 *    >>
 *  +3>>
 *    >>  Copyright (c) 2011:
 *    >>
 *    >>     |             |     |
 *    >>     |    ,---.,---|,---.|---.
 *    >>     |    |   ||   |`---.|   |
 *    >>     `---'`---'`---'`---'`---'
 *    >>                    // Niklas Klügel
 *    >>
 *  +4>>
 *    >>  Made in Bavaria by fat little elves - since 1983.
 */

package org.lodsb.reakt

trait TVar[T] extends TVal[T] {

	//override def action(msg: T) = this.emit(msg)

	def update(newValue: T) = {
		this.onUpdateValue(newValue)
		this.emit(newValue)
	}

	def <~[B <: T](that: TSignal[B]) : TSignal[B] = {
		that.observe({x => this() = x ;true})

		that
	}
}


