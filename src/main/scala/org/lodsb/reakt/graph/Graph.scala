/*
 * +1>>  This source code is licensed as GPLv3 if not stated otherwise.
 *    >>  NO responsibility taken for ANY harm, damage done
 *    >>  to you, your data, animals, etc.
 *    >>
 *  +2>>
 *    >>  Last modified:  2011 - 3 - 18 :: 4 : 58
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

package org.lodsb.reakt.graph

import akka.actor._
import java.util.Random
import scala.collection.mutable.HashMap
import org.lodsb.reakt.{TSignalet, TSignal, TReactive, Reactive}
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import org.lodsb.reakt.sync.{NodeObservableSynchronous, ObserverReactiveS}
import org.lodsb.reakt.async.{NodeObservableAsynchronous, VarA}

case class Edge(source: NodeBase[_], destination: NodeBase[_])

case class Propagate[T](cycle: Long, source: NodeBase[_], destination: NodeBase[_],  message: T)


abstract class ReactiveGraph {
  import scalax.collection.mutable.Graph
  import scalax.collection.GraphPredef._
  import scalax.collection.GraphEdge._

  private val graph = Graph[NodeBase[_],UnDiEdge]();

  protected[reakt] def _connect(source: NodeBase[_], destination: NodeBase[_]) = {
    graph.add(source);
    graph.add(destination);

    graph.add(source~destination)

  }

  protected[reakt] def _disconnect(source: NodeBase[_], destination: NodeBase[_]) = {
    val edge = source~destination
    graph.remove(edge)
  }

  protected[reakt] def _predecessors(node: NodeBase[_], fun: NodeBase[_] => Unit) = {
    val g = graph.get(node)

    val succ = g.inNeighbors

    succ.foreach({x => fun(x)})

  }


  protected[reakt] def _path(source: NodeBase[_], dest: NodeBase[_], fun: (NodeBase[_],NodeBase[_]) => Unit) =  {
    val s = graph.get(source)
    val d = graph.get(dest)

    val succ = s.shortestPathTo(d)

    if(!succ.isEmpty){
      val path = succ.get
      path.edgeIterator.foreach({
        edge => fun(edge._1,edge._2)
      })
    }

  }

  override def toString(): String = {"Graph:\n----\n"+graph}

}

trait NodeBase[+T] {

	protected var dependants = List[NodeBase[_]]()
	protected var dependson = new HashMap[NodeBase[_], Boolean]()

  protected def removeElemFromList[T](obj: T, list: List[T]) = list diff List(obj)


	protected def onUpdateValue[B >: T](value: B): Unit;

	protected val depLock = new Object

	def addDependant(d: NodeBase[_]): Unit = {
		depLock.synchronized {
			this.dependants ::= d
		}
	}

	//override def scheduler = Reactive.scheduler

	def addDependingOn(source: NodeBase[_]): Unit = {
		depLock.synchronized {
			dependson.put(source, true)
		}
	}

  def rmDependant(d: NodeBase[_]): Unit = {
    depLock.synchronized {
  			dependants = this.removeElemFromList(d,dependants)
  		}
  }

  def rmDependingOn(source: NodeBase[_]): Unit = {
    depLock.synchronized {
        dependson.remove(source)
    }
  }

	private var cntr = 0;

	def :<[A, B](sig1: NodeBase[A], sig2: NodeBase[B]): Unit = {
		Reactive.connect(this, sig1)
		Reactive.connect(this, sig2)
	}

  def disconnectAll: Unit = {
    //TODO: fixme!!
    Reactive.disconnectNode(this)
  }

	def emit[T](m: T): Unit = {
		this.emit(m, Reactive.cycle)
	}

	def emit[T](m: T, c: Long = 0): Unit = {
		dependants.foreach({
			node => this.sendMessage(node, m, c)
		})
	}

	def emitExcept[T](m: T, node: NodeBase[_]): Unit = {
		this.emitExcept(m, node, Reactive.cycle)
	}

	def emitExcept[T](m: T, n: NodeBase[_], c: Long = 0): Unit = {
		dependants.foreach({
			node => if (node != n) {
				this.sendMessage(node, m, c)
			}
		})
	}

	def emitOnly[T](m: T, node: NodeBase[_]): Unit = {
		this.emitOnly(m, node, Reactive.cycle)
	}

	def emitOnly[T](m: T, n: NodeBase[_], c: Long = 0): Unit = {
		this.sendMessage(n, m, c)
	}


	protected def sendMessage[T](node: NodeBase[T], m: T, c: Long = 0): Unit

	/*protected*/ def distributeMessage[Z](c: Long, s: NodeBase[_], msg: Z): Unit = {
    //todo: fixme, allow recursion
		depLock.synchronized {
			if (dependson.size == 0) {
				onUpdateValue(msg.asInstanceOf[T])

				this.emit(msg, c)
			} else if (dependson.contains(s)) {
				cntr = cntr + 1;

				if (cntr == dependson.size) {

					onUpdateValue(msg.asInstanceOf[T])

					this.emit(msg, c)

					cntr = 0;
				}
			}
		}
	}

}

trait NodeSynchronous[T] extends NodeBase[T] {
  @Override
  def sendMessage[T](node: NodeBase[T], m: T, c: Long = 0): Unit  = {
         node.distributeMessage(c, this, m)
  }
}

trait NodeAsynchronous[T] extends NodeBase[T] {

  override def sendMessage[T](node: NodeBase[T], m: T, c: Long = 0): Unit  = {
      //val asyncNode = node.asInstanceOf[NodeAsynchronous[_]]

      Reactive.router ! Propagate(c, this, node, m)
  }
}

trait Observable[T,U] {
  def observe(observerFun: T => Boolean): TReactive[T,U]
  def map[B](f: T => B): TSignal[B]

  protected def createBinOpSignal[A, B, C](sig1: TSignalet[A], sig2: TSignalet[B], binOpFun: (A, B) => C): TSignal[C]
}

class MessageActor extends Actor  {
  def receive = {
    case p@Propagate(c, s, destination, msg) => {
    					destination.distributeMessage(c, s, msg)
    }
    case _ => System.err.println("Message type not understood!")
  }
}


