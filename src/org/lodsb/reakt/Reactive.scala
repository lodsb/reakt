/*
 * +1>>  This source code is licensed as GPLv3 if not stated otherwise.
 *    >>  NO responsibility taken for ANY harm, damage done
 *    >>  to you, your data, animals, etc.
 *    >>
 *  +2>>
 *    >>  Last modified:  2011 - 3 - 18 :: 5 : 18
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

package org.lodsb.reakt

import async.BinOpSignalA
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
	def map[B](f: DefinedType => B): TSignal[B];
}

/*
	TODO: classes for conversion
 */

trait TReactive[DefinedType, UndefinedType]
	extends /*NodeBase[DefinedType] with */ TObservableValue[DefinedType, UndefinedType] {

	protected var defaultDefValue: DefinedType;
	protected var defaultUndefValue: UndefinedType;

	protected var _undefValue: UndefinedType = defaultUndefValue;
	protected var _defValue: DefinedType = defaultDefValue;

	protected  val processingMessages = new AtomicBoolean

	protected def generateUndefTypeValue(): UndefinedType = {
		defaultUndefValue
	}

	protected def action(msg: DefinedType): Unit = {}

	protected def onUpdateValue[B >: DefinedType](value: B): Unit = {
		processingMessages.set(true);
		_defValue = value.asInstanceOf[DefinedType]
		_undefValue = generateUndefTypeValue
		this.action(value.asInstanceOf[DefinedType])

		processingMessages.set(false)
	}


	def bang : Unit = {
		this.emit(this.value.merge)
	}

	protected[reakt] def defAndUndeValues: Tuple2[DefinedType,UndefinedType] = (this._defValue, this._undefValue)

	def value: Either[DefinedType, UndefinedType] = {
		if (processingMessages.get) {
			Left(_defValue)
		} else {
			Right(_undefValue)
		}
	}

	def observe(observerFun: DefinedType => Boolean): Unit;

	def emit[T](m: T): Unit
	def emit[T](m: T, c: Long = 0): Unit
}

trait TSignalet[+ValueType] extends TObservableValue[ValueType, ValueType]

trait TSignal[ValueType] extends TReactive[ValueType, ValueType] with TSignalet[ValueType] {
	protected val init: ValueType;

	this.defaultDefValue 	= init;
	this.defaultUndefValue 	= init;

	// publish initial value
	//this.emit(init)

	override protected def generateUndefTypeValue(): ValueType = {
		this._defValue
	}

	def apply(): ValueType = this.value.merge

	protected def createBinOpSignal[A,B,C](sig1: TSignalet[A], sig2: TSignalet[B], binOpFun: (A, B) => C) : TSignal[C]

	def merge[A](that: TSignal[A]) = :>(that)

	def :>[A](that: TSignal[A]): TSignal[Tuple2[ValueType, A]] = {
		createBinOpSignal[ValueType, A, Tuple2[ValueType, A]](this, that, (x: ValueType, y: A) => (x, y));
	}



	def ~>[B >: ValueType](that: TVar[B]): TVar[B] = {

		this.observe({x => that() = x ;true})

		that
	}

	def +(that: TSignalet[ValueType])(implicit numeric1: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric1.plus(x, y))

	def +(that: TSignalet[String]) =
		createBinOpSignal(this, that, (x: ValueType, y: String) => x.toString + y)

	def -(that: TSignalet[ValueType])(implicit numeric2: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric2.minus(x, y))

	def *(that: TSignalet[ValueType])(implicit numeric3: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric3.times(x, y))

	def min(that: TSignalet[ValueType])(implicit numeric4: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric4.min(x, y))

	def max(that: TSignalet[ValueType])(implicit numeric5: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric5.max(x, y))

	//TODO:FIXME: where is it called from?
	//def ==(that: TSignalet[ValueType])(implicit numeric6: Numeric[ValueType]) =
	//	createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric6.equiv(x, y))

	def >=(that: TSignalet[ValueType])(implicit numeric7: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric7.gteq(x, y))

	def <=(that: TSignalet[ValueType])(implicit numeric8: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric8.lteq(x, y))

	def >(that: TSignalet[ValueType])(implicit numeric9: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric9.gt(x, y))

	def <(that: TSignalet[ValueType])(implicit numeric11: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric11.lt(x, y))

	def binOp[A, B](that: TSignal[A], binOpFun: (ValueType, A) => B) = {
		(createBinOpSignal(this, that, binOpFun)).asInstanceOf[TSignal[B]]
	}

}

trait TEventSource[ValueType, UndefValueType] extends TReactive[ValueType, UndefValueType]

abstract class BinOpSignal[A, B, C](private val sig1: TSignalet[A], private val sig2: TSignalet[B],
											 private val binOpFun: (A, B) => C)
	extends TSignal[C] {

	val init = binOpFun(sig1.value.merge, sig2.value.merge)
	var defaultDefValue = init;
	var defaultUndefValue = init;
	val reactive = this;

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
