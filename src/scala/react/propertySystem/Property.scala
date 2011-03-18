
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

package scala.react.propertySystem {

import react._

object PropertyObserver {

}

class Property[T](deferor: VarDeferor, name: String, init: T, set: (T) => Unit, get: () => T) extends VarDefering[T](deferor, init) {

	//this.update(init)

	override def updateCallback(newVal: T) = {
		set(newVal)
		super.updateCallback(get())
	}

   	override def toString() = "Property: "+name + " value: "+this._value + "\n" + super.toString

}

class Attribute[T](name: String, protected[react] var _value: T) extends SourceSignal[T] {
	this.emit(_value)
	def update(newVal: T) = {
		_value = newVal
		this.emit(_value)
	}
}






object properties extends Observing{
	def main(args: Array[String]) {

	   /*
		val p  = new Property(this, "test22"  , 10.0, {x: Double => println("set "+x)}, {() => println("GET "); Math.random})
		val p2 = new Property(this, "test33" , 10.0, {x: Double => println("set "+x)}, {() => println("GET "); Math.random})

		var z = 23

		println(p)
		println(p2)

		val trash = Signal {p()=p2()}
		println(trash)

		observe(trash){y => println("--- "+y); true}
		p2() = 123
		//p() =1234455*/
	}

}
}
