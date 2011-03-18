package org.lodsb.reakt

import graph.{NodeBase, Edge, ReactiveGraph}
import java.util.concurrent.atomic.{AtomicLong, AtomicBoolean}
import actors.scheduler.ResizableThreadPoolScheduler
import actors.Actor

object Reactive extends ReactiveGraph {
	lazy val scheduler = {
		val s = new ResizableThreadPoolScheduler(false)
		s.start()
		s
	}

	def connect(source: NodeBase[_], destination: NodeBase[_]) {
		edges ::= Edge(source, destination)

		println(source + " " + destination)

		source.addDependant(destination)
		destination.addDependingOn(source)
	}

	def disconnect(source: NodeBase[_], destination: NodeBase[_]) = {}

	/*
		impl me + locking
	 */
	def disconnectNode(NodeBase: NodeBase[_]) = {}

	private val currentCycle = new AtomicLong
	currentCycle.set(0)

	def cycle = {
		currentCycle.getAndIncrement
	}

}

trait TObservableValue[+DefinedType, +UndefinedType] {
	def value: Either[DefinedType, UndefinedType];

	def observe(observerFun: DefinedType => Boolean): Unit
}

/*
	TODO: classes for conversation
 */

class Reactive[DefinedType, UndefinedType](protected val defaultDefValue: DefinedType,
										   protected val defaultUndefValue: UndefinedType)

	extends NodeBase[DefinedType]
	with TObservableValue[DefinedType, UndefinedType] {

	protected var _undefValue: UndefinedType = defaultUndefValue;
	protected var _defValue: DefinedType = defaultDefValue;

	private val processingMessages = new AtomicBoolean

	protected def generateUndefTypeValue(): UndefinedType = {
		defaultUndefValue
	}

	protected def action(msg: DefinedType): Unit = {}

	override protected def onUpdateValue[B >: DefinedType](value: B): Unit = {
		processingMessages.set(true);
		_defValue = value.asInstanceOf[DefinedType]
		_undefValue = generateUndefTypeValue
		this.action(value.asInstanceOf[DefinedType])

		processingMessages.set(false)
	}


	def bang : Unit = {
		this.emit(this.value.merge)
	}

	override def start : Actor = {
		val actor = super.start

		this.bang

		actor
	}


	def value: Either[DefinedType, UndefinedType] = {
		if (processingMessages.get) {
			Left(_defValue)
		} else {
			Right(_undefValue)
		}
	}

	private class ObserverReactive[X, Y](x: X, y: Y, private val fun: X => Boolean) extends Reactive[X, Y](x, y) {
		override protected def action(msg: X): Unit = {
			if (!fun(msg)) {
				Reactive.disconnectNode(this);
			}
		}
	}

	def observe(observerFun: DefinedType => Boolean): Unit = {
		val observerReactive = new ObserverReactive[DefinedType, UndefinedType](this._defValue,
			this._undefValue,
			observerFun);

		Reactive.connect(this, observerReactive)

		observerReactive.start

	}
}

trait TSignalet[+ValueType] extends TObservableValue[ValueType, ValueType]

object ConstantSignal {
	implicit def something2ConstantSignal[SC](something: SC) = new ConstantSignal(something)
}

class ConstantSignal[+T](init: T) extends TSignalet[T] {
	def value: Either[T, T] = Left(init)

	def observe(observerFun: T => Boolean): Unit = {}
}

class Signal[ValueType](init: ValueType) extends Reactive[ValueType, ValueType](init, init)
with TSignalet[ValueType] {
	implicit def something2ConstantSignal[SC](something: SC) = new ConstantSignal(something)
	// publish initial value
	this.emit(init)

	override protected def generateUndefTypeValue(): ValueType = {
		this._defValue
	}

	def apply(): ValueType = this.value.merge

	protected class BinOpSignal[A, B, C](private val sig1: TSignalet[A], private val sig2: TSignalet[B],
										 private val binOpFun: (A, B) => C)
		extends Signal[C](binOpFun(sig1.value.merge, sig2.value.merge)) {

		sig1.observe(updateA)
		sig2.observe(updateB)

		var a: A = sig1.value.merge;
		var b: B = sig2.value.merge;

		private def updateA(x: A): Boolean = {
			a = x;
			this.emitBinOpResult
			true;
		}

		private def updateB(x: B): Boolean = {
			b = x;
			this.emitBinOpResult
			true;
		}

		private def emitBinOpResult = {
			this.emit(this.binOpFun(a, b))
		}
	}

	def merge[A](that: Signal[A]) = :>(that)

	def :>[A](that: Signal[A]): Signal[Tuple2[ValueType, A]] = {
		new BinOpSignal[ValueType, A, Tuple2[ValueType, A]](this, that, (x: ValueType, y: A) => (x, y));
	}

	def :<[A, B](sig1: Signal[A], sig2: Signal[B]): Unit = {
		Reactive.connect(this, sig1)
		Reactive.connect(this, sig2)
	}

	def ~>[B >: ValueType](that: Var[B]): Var[B] = {
		Reactive.connect(this, that)
		that
	}

	def map[B](f: ValueType => B): Signal[B] = {
		/* TODO: Lazy? */
		val sig = new Val[B](f(this.apply))

		val obsFun ={v: ValueType => {
			sig.emit(f(v)); true
		}}
		this.observe(obsFun)

		sig
	}

	def +(that: TSignalet[ValueType])(implicit numeric: Numeric[ValueType]) =
		new BinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric.plus(x, y))

	def +(that: TSignalet[String]) =
		new BinOpSignal(this, that, (x: ValueType, y: String) => x.toString + y)

	def -(that: TSignalet[ValueType])(implicit numeric: Numeric[ValueType]) =
		new BinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric.minus(x, y))

	def *(that: TSignalet[ValueType])(implicit numeric: Numeric[ValueType]) =
		new BinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric.times(x, y))

	def min(that: TSignalet[ValueType])(implicit numeric: Numeric[ValueType]) =
		new BinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric.min(x, y))

	def max(that: TSignalet[ValueType])(implicit numeric: Numeric[ValueType]) =
		new BinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric.max(x, y))

	def ==(that: TSignalet[ValueType])(implicit numeric: Numeric[ValueType]) =
		new BinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric.equiv(x, y))

	def >=(that: TSignalet[ValueType])(implicit numeric: Numeric[ValueType]) =
		new BinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric.gteq(x, y))

	def <=(that: TSignalet[ValueType])(implicit numeric: Numeric[ValueType]) =
		new BinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric.lteq(x, y))

	def >(that: TSignalet[ValueType])(implicit numeric: Numeric[ValueType]) =
		new BinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric.gt(x, y))

	def <(that: TSignalet[ValueType])(implicit numeric: Numeric[ValueType]) =
		new BinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric.lt(x, y))

	def binOp[A, B](that: Signal[A], binOpFun: (ValueType, A) => B) = {
		(new BinOpSignal(this, that, binOpFun)).asInstanceOf[Signal[B]]
	}

}

class Event[ValueType, UndefValueType](x: ValueType, y: UndefValueType)
	extends Reactive[ValueType, UndefValueType](x, y)
