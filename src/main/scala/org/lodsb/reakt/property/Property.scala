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

trait TypeInfo[T]{
  def manifest : Manifest[T]
}

class Property[T](deferor: VarDeferor, val name: String, init: T, val set: (T) => Unit, val get: () => T)(implicit m: Manifest[T]) extends VarDefering[T](deferor, init) with TypeInfo[T] {

	override def updateCallback(newVal: T) = {
		set(newVal)
		super.updateCallback(get())
	}

	override def toString() = "Property: " + name + " value: " + this.value + "\n" + super.toString

	def :=(value:T) = set(value)

	override def apply() = get()

  override def manifest = m

}

  object Property {
    def ofType[T](klaas: Class[T] , deferor: VarDeferor, name: String, init: T, set: (T) => Unit, get: () => T)  = {
      val manifest = new Manifest[T] {
        override def erasure = klaas
        def runtimeClass = klaas
      }
      new org.lodsb.reakt.property.Property(deferor,name, init, set, get)(manifest)
    }

    def genManifest[T](klaas: Class[T]) : Manifest[T] = {
      new Manifest[T] {
      override def erasure = klaas
      def runtimeClass = klaas
    }}
  }

class Attribute[T](val name: String, _value: T)(implicit m: Manifest[T]) extends ValA[T](_value) with TypeInfo[T] {

	this.update(_value);
	def update(newValue: T) = {
		this.onUpdateValue(newValue)
		this.emit(newValue)
	}

  override def manifest = m
}

object Attribute {
  def ofType[T](klaas: Class[T] , name: String, init: T)  = {
    val manifest = new Manifest[T] {
      override def erasure = klaas
      def runtimeClass = klaas
    }
    new org.lodsb.reakt.property.Attribute(name, init)(manifest)
  }
}

}

