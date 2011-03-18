package org.lodsb.reakt
import org.lodsb.reakt.ConstantSignal._


object Tests {
	def main(args: Array[String]) = {

		val x = new Var[Int](5)
		val z = new Var[String](" WORLD")

		x.observe(x => {println(x); true})
		x.start;

		val y = new Var[Int](0)
		val u = new Var[Int](2000)
		u.start
		y.start

		y <~ x + u + 10000000;

		val meh = y.map(x => x+" !!! asd")
		//Reactive.connect(x,y)

		y.observe(ox => {println("Y: "+ox+" !"); true})
		meh.observe(ox => {println("MEHHH "+ox); true})


		x() = 127

		Thread.sleep(2000)

		u() = 9600
		x() = 10



	}
}
