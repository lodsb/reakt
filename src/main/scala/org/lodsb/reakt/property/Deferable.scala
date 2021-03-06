/*
 * +1>>  This source code is licensed as GPLv3 if not stated otherwise.
 *    >>  NO responsibility taken for ANY harm, damage done
 *    >>  to you, your data, animals, etc.
 *    >>
 *  +2>>
 *    >>  Last modified:  2011 - 3 - 21 :: 4 : 36
 *    >>  Origin: eventgraphtest (project) / reakt (module)
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

package org.lodsb.reakt.property


import org.lodsb.reakt.async.VarA

case class Deferable[A](varDefer: VarDefering[A], value: A)

class VarDefering[A](protected val deferor: VarDeferor,  value: A) extends VarA[A](value) {

	def updateCallback(newValue: A) = if (newValue != null && this.value != newValue) {
		//TODO: FIXME POSSIBLE BUG
		this._defValue = newValue
		this.onUpdateValue(newValue)
		this.emit(newValue)
	}

	override def update(newValue: A) = {
		deferor.defer(Deferable(this, newValue))
	}
}
