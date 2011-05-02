/*
 * +1>>  This source code is licensed as GPLv3 if not stated otherwise.
 *    >>  NO responsibility taken for ANY harm, damage done
 *    >>  to you, your data, animals, etc.
 *    >>
 *  +2>>
 *    >>  Last modified:  2011 - 4 - 1 :: 4 : 36
 *    >>  Origin: eventgraphtest (project) / reakt (module)
 *    >>
 *  +3>>
 *    >>  Copyright (c) 2011:
 *    >>
 *    >>     |             |     |
 *    >>     |    ,---.,---|,---.|---.
 *    >>     |    |   ||   |`---.|   |
 *    >>     `---'`---'`---'`---'`---'
 *    >>                    // Niklas Kl�gel
 *    >>
 *  +4>>
 *    >>  Made in Bavaria by fat little elves - since 1983.
 */

package org.lodsb.reakt

import async.BinOpSignalA


//  TODO  move somewhere else!
object ConstantSignal {
	implicit def something2ConstantSignal[SC](something: SC) = new ConstantSignal(something)
}

// TODO: FIXME: Covariance!
class ConstantSignal[T](initial: T) extends TSignal[T] {
	val init = initial;
	var defaultUndefValue = init;
	var defaultDefValue   = init;

	override def value: Either[T, T] = Left(initial)
	def emit[T](m: T): Unit = {}
	def emit[T](m: T, c: Long = 0): Unit = {}


	def observe(observerFun: T => Boolean): Unit = {}

	def map[B](f: T => B): TSignal[B] = {
		new ConstantSignal(f(this.value.merge))
	}

	protected def createBinOpSignal[A, B, C](sig1: TSignalet[A], sig2: TSignalet[B], binOpFun: 	(A, B) => C): TSignal[C] = new BinOpSignalA(sig1, sig2, binOpFun);
}
