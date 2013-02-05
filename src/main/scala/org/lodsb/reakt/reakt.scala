package org.lodsb.reakt

package object conversions {
	implicit def something2ConstantSignal[SC](something: SC) = new ConstantSignal(something)
}

