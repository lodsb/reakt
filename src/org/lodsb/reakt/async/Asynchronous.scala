/*
 *  +1>>  This source code is licensed as GPLv3 if not stated otherwise.
 *    >>  NO responsibility taken for ANY harm, damage done
 *    >>  to you, your data, animals, etc.
 *    >>
 *  +2>>
 *    >>  Last modified:  2011 - 3 - 31 :: 0 : 21
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

package org.lodsb.reakt.async

import org.lodsb.reakt.graph.NodeAsynchronous
import org.lodsb.reakt._
import actors.Actor

protected[reakt] class BinOpSignalA[A, B, C](sig1: TSignalet[A], sig2: TSignalet[B], 											  bOpFun: (A, B) => C) extends BinOpSignal[A,B,C](sig1, sig2, bOpFun)  with NodeObservableAsynchronous[C, C]

trait NodeObservableAsynchronous[DefinedType, UndefinedType] extends NodeAsynchronous[DefinedType] {
	protected[async] val reactive: TReactive[DefinedType, UndefinedType];

	def observe(observerFun: DefinedType => Boolean): Unit = {
		val (defV, undefV) = reactive.defAndUndeValues
		val observerReactive = new ObserverReactiveA[DefinedType, UndefinedType](defV, undefV, observerFun);

		Reactive.connect(this, observerReactive)

		observerReactive.start

	}


/*	override def start : Actor = {

		val actor = super.start

		this.bang

		actor
	}*/


	protected def createBinOpSignal[A, B, C](sig1: TSignalet[A], sig2: TSignalet[B], binOpFun: (A, B) => C): TSignal[C] = {
		new BinOpSignalA(sig1, sig2, binOpFun)
	}


	def map[B](f: DefinedType => B): TSignal[B] = {

		val value = f(reactive.value.merge.asInstanceOf[DefinedType]);
		val sig = new ValA[B](value)

		val obsFun ={v: DefinedType => {
			sig.emit(f(v)); true
		}}
		this.observe(obsFun)

		sig
	}


}


class SignalA[T](initialValue: T) extends TSignal[T] with NodeObservableAsynchronous[T, T] {
	val init = initialValue;
	var defaultDefValue = init;
	var defaultUndefValue = init;

	val reactive = this;
}

class ValA[T](initialValue: T) extends TVal[T] with NodeObservableAsynchronous[T, T] {
	val init = initialValue;
	var defaultDefValue = init;
	var defaultUndefValue = init;

	val reactive = this;
}

class VarA[T](initialValue: T) extends TVar[T] with NodeObservableAsynchronous[T, T] {
	val init = initialValue;
	var defaultDefValue = init;
	var defaultUndefValue = init;

	val reactive = this;
}

class EventSourceA[X, Y](override var defaultDefValue: X, override var defaultUndefValue: Y)
	extends TEventSource[X, Y] with NodeObservableAsynchronous[X, Y] {
	val reactive = this;
}

class ReactiveA[X, Y](override var defaultDefValue: X, override var defaultUndefValue: Y) extends TReactive[X, Y] with NodeObservableAsynchronous[X, Y] {

	val reactive = this;
}

protected[async] class ObserverReactiveA[X, Y](x: X, y: Y, private val fun: X => Boolean)
	extends ReactiveA[X, Y](x, y)
	with NodeObservableAsynchronous[X, Y] {

	override protected def action(msg: X): Unit = {
		if (!fun(msg)) {
			Reactive.disconnectNode(this);
		}
	}
}
