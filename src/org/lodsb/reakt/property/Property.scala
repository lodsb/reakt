/*
 +1>>  This source code is licensed as GPLv3 if not stated otherwise.
	>>  NO responsibility taken for ANY harm, damage done
	>>  to you, your data, animals, etc.
	>>
  +2>>
	>>  Last modified:  2011 - 2 - 28 :: 7 : 50
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

package org.lodsb.reakt.property {

import org.lodsb.reakt.async.ValA

object PropertyObserver {

}

class Property[T](deferor: VarDeferor, name: String, init: T, val set: (T) => Unit, val get: () => T) extends VarDefering[T](deferor, init) {

	//this.update(init)

	override def updateCallback(newVal: T) = {
		set(newVal)
		super.updateCallback(get())
	}

	override def toString() = "Property: " + name + " value: " + this.value + "\n" + super.toString

}

class Attribute[T](val name: String, _value: T) extends ValA[T](_value) {

	def update(newValue: T) = {
		this.onUpdateValue(newValue)
		this.emit(newValue)
	}
}


}
