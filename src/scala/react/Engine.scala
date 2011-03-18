/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2007-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.react

import scala.collection.mutable.{PriorityQueue, HashSet, ArrayBuffer, SynchronizedBuffer}
import scala.actors.Actor._
import scala.actors.Actor
import java.util.concurrent.ConcurrentHashMap

abstract class Engine {
	def invalidate(r: Dependent)

	def invalidate(rs: Dependents)
}

object Engine extends Engine {

	//for messaging
	case class PropagateFrom(dep: DependencyNode)

	case class Processed(dep: DependencyNode)

	case class SetMessage(dep: DependencyNode, msg: Any)

	case class MessageFor(dep: DependencyNode)

	import java.util.{PriorityQueue, Comparator}

	private def newQueue = new PriorityQueue[Dependent](10, new Comparator[Dependent] {
		def compare(x: Dependent, y: Dependent): Int = x.level - y.level
	})

	private var invalids = newQueue
	private var nextInvalids = newQueue

	private val _processed = new ConcurrentHashMap[DependencyNode, Any]

	def evaluating = _evaluating

	private var _evaluating = false

	private var _level = Int.MaxValue

	def level = _level

	private val _todoLock = new java.util.concurrent.locks.ReentrantLock
	private val _propLock = new java.util.concurrent.locks.ReentrantLock
	private val _todosAvailable = _todoLock.newCondition
	private val _propagationDone = _propLock.newCondition

	private val engineActor = actor {
		loop {
			react {
				case PropagateFrom(d) => {
					runCycle {
						invalidate(d.clearDependents())
					}
				}

				case Processed(d) => {
					_processed.put(d, ProcessedMsg)
				}

				case SetMessage(d, msg) => {
					_processed.put(d, msg)
				}


				case MessageFor(d) => {
					val getVal = _processed.get(d);
					var ret: Option[Any] = None;

					if (getVal != ProcessedMsg && getVal != null) {
						ret = Option[Any](getVal)
					}

					reply(ret)

				}
			}
		}
	}

	this.engineActor.start


	private var _cycleNo = -1

	def scheduleNextCycle(scheduledAtTurn: Long): Unit =
		runNonBlocking {
			// do not run if this scheduled turn is outdated
			if (scheduledAtTurn == _cycleNo - 1) runCycle()
			else scheduleNextCycle(_cycleNo)
		}

	var blockingRunner: (() => Unit) => Unit = {
		op =>
	}
	var nonBlockingRunner: (() => Unit) => Unit = {
		op =>
	}

	private[react] def runBlocking(op: => Unit) {
		blockingRunner(() => op)
	}

	private def runNonBlocking(op: => Unit) {
		nonBlockingRunner(() => op)
	}

	private def runCycle(preOp: => Unit) {
		try {
			_cycleNo += 1
			_evaluating = true
			_level = 0

			// Note: Don't clear the processed here but at the end, since some reactives might
			// have deposited their messages already!

			invalids = nextInvalids
			nextInvalids = newQueue

			//log("### Starting to propagate. Initial invalids: " + invalids)
			// run pre op before we eventually start processing queue
			preOp
			propagateFromQueue()
			//log("### Propagation done.")
		} finally {
			_evaluating = false
			// don't forget to cleanup the dep stack
			/*val s = Signal.dependentStack.get
			if (s != null) {
				s.clear
				s push Observer.Nil
			} */

			Signal.clearDepStack

			_processed.clear()
			_level = Int.MaxValue
			if (!nextInvalids.isEmpty) scheduleNextCycle(_cycleNo)
		}
	}

	/**
	 * Entry method for reactive sources to start propagation.
	 */

	val megaLock: Object = new Object

	def propagateFrom(d: DependencyNode) {
		this.engineActor ! PropagateFrom(d);

		/*megaLock.synchronized{
		runCycle {
			invalidate(d.clearDependents())
		}
		} */
	}

	/**
	 * Propagate to the given node.
	 */
	private def propagateTo(dep: Dependent) {
		if (!_processed.contains(dep)) {
			//log("  Propagating to " + dbgInfo(dep))
			assert(dep.level >= _level, dep.level + "<" + _level)
			_level = dep.level
			dep.receive(this) // validate right now and save message
		}
	}

	/**
	 * Propagate to contents of invalids queue, starting with the node that has the lowest level.
	 */
	private def propagateFromQueue() {
		var r = invalids.poll
		while (r != null) {
			// once we hit observers, we are done with evaluating
			if (r.isInstanceOf[Observer]) _evaluating = false
			propagateTo(r)
			r = invalids.poll
		}
	}

	def invalidate(dep: Dependent) = if (!_processed.contains(dep)) {
		assert(dep.level >= this.level, dep.level + "<" + this.level)
		//log("  Invalidating " + dbgInfo(dep))
		dep.invalidate()
		invalids add dep
	}

	def lift(dep: Dependent) = {
		invalids remove dep
		invalidate(dep)
		//log("Lifted " + dbgInfo(dep))
	}

	def invalidate(deps: Dependents) {
		deps foreach invalidate
	}

	def nextTurn(dep: Dependent) {
		nextInvalids add dep
	}

	private val ProcessedMsg = new Object
	private val lock = new Object

	def messageFor[A](r: DependencyNode): Option[A] = {
			val getVal = _processed.get(r);
			var ret: Option[A] = None;

			if (getVal != ProcessedMsg && getVal != null) {
				ret = Option[A](getVal.asInstanceOf[A])
			}

			ret

	}

	def setMessage(dep: DependencyNode, msg: Any) {
		this.engineActor ! SetMessage(dep, msg)
	}

	def processed(dep: DependencyNode) {
		this.engineActor ! Processed(dep)
	}
}
