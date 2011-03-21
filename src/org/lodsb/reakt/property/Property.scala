import org.mt4j.util.math.Vector3D

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

import org.lodsb.reakt.Val
import org.lodsb.reakt.property.VarDefering

object PropertyObserver {

}

class Property[T](deferor: VarDeferor, name: String, init: T, set: (T) => Unit, get: () => T) extends VarDefering[T](deferor, init) {

	//this.update(init)

	override def updateCallback(newVal: T) = {
		set(newVal)
		super.updateCallback(get())
	}

	override def toString() = "Property: " + name + " value: " + this._value + "\n" + super.toString

}

class Attribute[T](name: String, protected[react] var _value: T) extends Val[T] {
	this.emit(_value)

	def update(newVal: T) = {
		_value = newVal
		this.emit(_value)
	}
}

}
