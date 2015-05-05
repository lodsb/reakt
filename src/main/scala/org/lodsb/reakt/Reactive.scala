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

import graph.{MessageActor, NodeBase, Edge, ReactiveGraph}
import java.util.concurrent.atomic.{AtomicLong, AtomicBoolean}
import akka.actor.{Props, ActorSystem}
import akka.routing.RandomRouter


object Reactive extends ReactiveGraph {
  private val nrOfWorkers = 32;

  //TODO: maybe do eager eval here to reduce time for first messages?
	lazy val system = ActorSystem("reakt")
  lazy val router = system.actorOf(Props[MessageActor]
                    .withRouter(RandomRouter(nrOfWorkers)), name = "MessageRouter")

  def startup = {
    println(system.name + " started, router " + router.toString())
  }

  def shutdown = system.shutdown()

  private val graphLock = new Object();

	def connect(source: NodeBase[_], destination: NodeBase[_]) = {
    graphLock.synchronized {
      //println("connect src "+source+" dst "+destination)
        _connect(source,destination)
        source.addDependant(destination)
        destination.addDependingOn(source)
    }
	}

  protected[reakt] def fakeConnect(source: NodeBase[_], destination: NodeBase[_]) = {
    //println("fconn "+source+ " dst "+destination)
    graphLock.synchronized {
      _connect(source,destination)
    }
  }

	def disconnectSingle[T<:NodeBase[_]](source: T, destination: T) =  {
    graphLock.synchronized {

      _disconnect(source,destination)
      source.rmDependant(destination)
      destination.rmDependingOn(source)
    }
  }

  //
  // disconnects a path of nodes
  //
  def disconnect[T<:NodeBase[_]](source: T, destination: T) = {
    graphLock.synchronized{
      _path(source,destination,{
        (x,y) => this._disconnect(x, y)
        x.rmDependant(y)
        y.rmDependingOn(x)
      })
    }
   }


	def disconnectNode[T<:NodeBase[_]](node: T) = {
      graphLock.synchronized{
        _predecessors(node,{
          x => this._disconnect(x, node)
          x.rmDependant(node)
          node.rmDependingOn(x)

          node.rmDependant(x)
          x.rmDependingOn(node)

        })
      }
  }

	private val currentCycle = new AtomicLong
	currentCycle.set(0)

	def cycle = {
		currentCycle.getAndIncrement
	}

}

trait TObservableValue[+DefinedType, +UndefinedType] {
	def value: Either[DefinedType, UndefinedType];

	def observe(observerFun: DefinedType => Boolean): TReactive[_,_]
	def map[B](f: DefinedType => B): TSignal[B];

}


/*
	TODO: classes for conversion
	TODO: use self-types
	TODO: reactive-field not used?
 */

trait TReactive[DefinedType, UndefinedType]
	extends TObservableValue[DefinedType, UndefinedType] {

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

  // TODO: should maybe move disconnect* somewhere else, since connecting is done entirely on the Signal level
  // extra trait?
  def disconnect[T<:TReactive[_,_]](source: T) : Unit

  def |~[T<:TReactive[_,_]](source: T) = this.disconnect(source)

	def observe(observerFun: DefinedType => Boolean): TReactive[_,_];

	def emit[T](m: T): Unit
	def emit[T](m: T, c: Long = 0): Unit
}

trait TSignalet[+ValueType] extends TObservableValue[ValueType, ValueType]

trait TSignal[ValueType] extends TReactive[ValueType, ValueType] with TSignalet[ValueType] {
	protected val init: ValueType;

	this.defaultDefValue 	= init;
	this.defaultUndefValue 	= init;
	this._defValue 	= init;
	this._undefValue 	= init;

	override protected def generateUndefTypeValue(): ValueType = {
		this._defValue
	}

	def apply(): ValueType = this.value.merge

	protected def createBinOpSignal[A,B,C](sig1: TSignalet[A], sig2: TSignalet[B], binOpFun: (A, B) => C) : TSignal[C]

	def merge[A](that: TSignal[A]) = :>(that)

	def :>[A](that: TSignal[A]): TSignal[Tuple2[ValueType, A]] = {
    val b = createBinOpSignal[ValueType, A, Tuple2[ValueType, A]](this, that, (x: ValueType, y: A) => (x, y));
    b
	}

	def ~>[B >: ValueType](that: TVar[B]): TVar[B] = {

		this.observe({x:ValueType => that() = x ;true})

		that
	}

	def +(that: TSignalet[ValueType])(implicit numeric1: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric1.plus(x, y))

	def +(that: TSignalet[String]) =
		createBinOpSignal(this, that, (x: ValueType, y: String) => x+""+ y)

	def -(that: TSignalet[ValueType])(implicit numeric2: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric2.minus(x, y))

	def *(that: TSignalet[ValueType])(implicit numeric32: Numeric[ValueType]) =  {
    import numeric32._
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric32.times(x, y))
  }

	def min(that: TSignalet[ValueType])(implicit numeric4: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric4.min(x, y))

	def max(that: TSignalet[ValueType])(implicit numeric5: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric5.max(x, y))

	//TODO:FIXME: where is it called from?
	def eq(that: TSignalet[ValueType])(implicit numeric6: Numeric[ValueType]) =
		createBinOpSignal(this, that, (x: ValueType, y: ValueType) => numeric6.equiv(x, y))

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
