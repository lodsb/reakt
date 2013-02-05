/*
 * +1>>  This source code is licensed as GPLv3 if not stated otherwise.
 *    >>  NO responsibility taken for ANY harm, damage done
 *    >>  to you, your data, animals, etc.
 *    >>
 *  +2>>
 *    >>  Last modified:  2011 - 4 - 1 :: 3 : 48
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

package org.lodsb.reakt.sync

import org.lodsb.reakt.graph.NodeSynchronous
import org.lodsb.reakt._

class BinOpSignalS[A, B, C](sig1: TSignalet[A], sig2: TSignalet[B], 											  binOpFun: (A, B) => C) extends BinOpSignal[A,B,C](sig1, sig2, binOpFun)  with NodeObservableSynchronous[C, C]

trait NodeObservableSynchronous[DefinedType, UndefinedType] extends NodeSynchronous[DefinedType] {
	protected[sync] val reactive: TReactive[DefinedType, UndefinedType];

	def observe(observerFun: DefinedType => Boolean): Unit = {
		val (defV, undefV) = reactive.defAndUndeValues
		val observerReactive = new ObserverReactiveS[DefinedType, UndefinedType](defV, undefV, observerFun);

		Reactive.connect(this, observerReactive)
	}

	protected def createBinOpSignal[A, B, C](sig1: TSignalet[A], sig2: TSignalet[B], binOpFun: (A, B) => C): TSignal[C] = {
		new BinOpSignalS(sig1, sig2, binOpFun)
	}


	def map[B](f: DefinedType => B): TSignal[B] = {

		val value = f(reactive.value.merge.asInstanceOf[DefinedType]);
		val sig = new ValS[B](value)

		val obsFun ={v: DefinedType => {
			sig.emit(f(v)); true
		}}
		this.observe(obsFun)

		sig
	}

}

class SignalS[T](initialValue: T) extends TSignal[T] with NodeObservableSynchronous[T, T] {
	val init = initialValue;
	var defaultDefValue = init;
	var defaultUndefValue = init;

	/**argh, should be done in a more beautiful manner**/
	this._defValue = init;
	this._undefValue=init;


	val reactive = this;
}

class IOS[T](initI: T, initO: T) extends TIO[T] {
	val out = new ValS[T](initO)
	val in = new VarS[T](initI)
}

class ValS[T](initialValue: T) extends TVal[T] with NodeObservableSynchronous[T, T] {
	val init = initialValue;
	var defaultDefValue = init;
	var defaultUndefValue = init;

	/**argh, should be done in a more beautiful manner**/
	this._defValue = init;
	this._undefValue=init;


	val reactive = this;
}

class VarS[T](initialValue: T) extends TVar[T] with NodeObservableSynchronous[T, T] {
	val init = initialValue;
	var defaultDefValue = init;
	var defaultUndefValue = init;

	/**argh, should be done in a more beautiful manner**/
	this._defValue = init;
	this._undefValue=init;


	val reactive = this;
}

class EventSourceS[X, Y](override var defaultDefValue: X, override var defaultUndefValue: Y)
	extends TEventSource[X, Y] with NodeObservableSynchronous[X, Y] {
	val reactive = this;
}

class ReactiveS[X, Y](override var defaultDefValue: X, override var defaultUndefValue: Y) extends TReactive[X, Y] with NodeObservableSynchronous[X, Y] {

	val reactive = this;
}

protected[sync] class ObserverReactiveS[X, Y](x: X, y: Y, private val fun: X => Boolean)
	extends ReactiveS[X, Y](x, y)
	with NodeObservableSynchronous[X, Y] {

	override protected def action(msg: X): Unit = {
		if (!fun(msg)) {
			Reactive.disconnectNode(this);
		}
	}
}
