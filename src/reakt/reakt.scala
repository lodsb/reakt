package reakt



package object reakt {
	implicit def something2ConstantSignal[SC](something: SC) = new ConstantSignal(something)
}
