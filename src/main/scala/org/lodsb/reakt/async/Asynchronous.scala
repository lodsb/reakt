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


import akka.actor._
import org.lodsb.reakt._
import graph.{NodeAsynchronous, NodeBase, Observable}

protected[reakt] class BinOpSignalA[A, B, C](sig1: TSignalet[A],
                                             sig2: TSignalet[B],
                                             bOpFun: (A, B) => C)
  extends BinOpSignal[A,B,C](sig1, sig2, bOpFun)
  with NodeObservableAsynchronous[C, C]

// TODO: move observable stuff upwards
// nodeasynchronous and synchronous should have both type params or none
trait NodeObservableAsynchronous[DefinedType, UndefinedType] extends NodeAsynchronous[DefinedType] with Observable[DefinedType, UndefinedType] {
	protected[async] val reactive: TReactive[DefinedType, UndefinedType];

  @Override
	def observe(observerFun: DefinedType => Boolean) = {
		val (defV, undefV) = reactive.defAndUndeValues
		val observerReactive = new ObserverReactiveA[DefinedType, UndefinedType](defV, undefV, observerFun);

		Reactive.connect(this, observerReactive)

    observerReactive

	}

  def disconnect[T<:TReactive[_,_]](source: T) : Unit = {
    Reactive.disconnect(source.asInstanceOf[NodeBase[_]],this)
  }



  // instead of signalets, maybe a nodeobservable is enough?
  @Override
	protected def createBinOpSignal[A, B, C](sig1: TSignalet[A], sig2: TSignalet[B], binOpFun: (A, B) => C): TSignal[C] = {
		new BinOpSignalA(sig1, sig2, binOpFun)
	}


  //TODO: should be moved to baseclass
  @Override
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

	/**argh, should be done in a more beautiful manner**/
	this._defValue = init;
	this._undefValue=init;

	val reactive = this;
}

class IOA[T](initI: T, initO: T) extends TIO[T] {
	val out = new ValA[T](initO)
	val in = new VarA[T](initI)
}

// should use self types
class ValA[T](initialValue: T) extends TVal[T] with NodeObservableAsynchronous[T, T] {
	val init = initialValue;
	var defaultDefValue = init;
	var defaultUndefValue = init;

	/**argh, should be done in a more beautiful manner**/
	this._defValue = init;
	this._undefValue=init;


	val reactive = this;
}

class VarA[T](initialValue: T) extends TVar[T] with NodeObservableAsynchronous[T, T] {
	val init = initialValue;
	var defaultDefValue = init;
	var defaultUndefValue = init;

	/**argh, should be done in a more beautiful manner**/
	this._defValue = init;
	this._undefValue=init;


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

  // this is always an intermediate signal, so if the
  // dependant is removed, this reactive can get unlinked
  //
  override def rmDependant(d: NodeBase[_]): Unit = {
    super.rmDependant(d)
    dependson.foreach( {
      f => val (n, b) = f;
        n.rmDependant(this);
    })
  }
	override protected def action(msg: X): Unit = {
		if (!fun(msg)) {
			Reactive.disconnectNode(this);
		}
	}
}
