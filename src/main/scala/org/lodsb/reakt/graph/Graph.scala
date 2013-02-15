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

import actors.Actor
import java.util.Random
import scala.collection.mutable.HashMap
import org.lodsb.reakt.{TReactive, Reactive}
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import org.lodsb.reakt.sync.{NodeObservableSynchronous, ObserverReactiveS}
import org.lodsb.reakt.async.{NodeObservableAsynchronous, VarA}

case class Edge(source: NodeBase[_], destination: NodeBase[_])

case class Propagate[T](cycle: Long, source: NodeBase[_], message: T)


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


	protected def sendMessage[T](node: NodeBase[T], m: T, c: Long = 0): Unit = {
		if (node.isInstanceOf[NodeAsynchronous[_]]) {
			node.asInstanceOf[NodeAsynchronous[_]] ! Propagate(c, this, m)
		} else if (node.isInstanceOf[NodeSynchronous[_]]) {
			node.distributeMessage(c, this, m)
		}
	}

	protected def distributeMessage[Z](c: Long, s: NodeBase[_], msg: Z): Unit = {
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

trait NodeSynchronous[T] extends NodeBase[T]

trait NodeAsynchronous[T] extends Actor with NodeBase[T] {

	def act() = {
		loop {
			react {
				case p@Propagate(c, s, msg) => {
					this.distributeMessage(c, s, msg)
				}
				case _ => System.err.println("Message type not understood!")
			}
		}
	}

}

object Test {
	val noMsg = 100000;
	var it = 5

	def main(args: Array[String]) = {
		val graph = Reactive

		var sources = List[VarA[Int]]()

		for (i <- 0 to it) {
			val s = new VarA(i)
			sources ::= s
			s.start
		}

		var destinations = List[VarA[Int]]()
		for (i <- 0 to it) {
			val d = new VarA[Int](i * 3)
			destinations ::= d
			d.start
		}


		for (i <- 0 to it) {
			graph.connect(sources(i), destinations(i))
		}

		var destinations2 = List[VarA[Int]]()
		for (i <- 0 to it) {
			val d = new VarA[Int](i * 23)
			destinations2 ::= d
			d.start
		}


		for (i <- 0 to it) {
			graph.connect(destinations(i), destinations2(i))
		}

		import scala.actors.Actor._

		var startTime: Long = 0;
		var endTime: Long = 0;

		val lock = new Object

		/*val end = new NodeBase[Int] {
			var t = 0;
			var numRecvMsg = 0;

			override def act() {
				loop {
					react {
						case Propagate(zzz, vvv, uuu) => {
							numRecvMsg = numRecvMsg + 1
							       //println(numRecvMsg)
							if (numRecvMsg >= (noMsg) - 1) {
								endTime = System.nanoTime;
								val execTime = endTime - startTime

								System.err.println("Exectime " + execTime + " #msg " + numRecvMsg + " msg/s " + (numRecvMsg / (execTime / 1000000000)));

								lock.synchronized {
									lock.notify
								}

							}

						}

						case z@_ => println("???" + z)
					}
				}
			}

			override def toString: String = {
				"END"
			}
		}*/
		val end = new VarA[Int](12); //new org.lodsb.reaktExt.Reactive.Signal[Int](123)
		end.start

		for (i <- 0 to it) {
			graph.connect(destinations2(i), end)
		}
		var numRecvMsg = new AtomicInteger;

		end.observe(x => {
			//println(numRecvMsg);
			if (numRecvMsg.incrementAndGet >= noMsg) {
				endTime = System.nanoTime;
				val execTime = endTime - startTime
				System.err.println("Exectime " + execTime + " #msg " + numRecvMsg.get + " msg/s " + (numRecvMsg.get / (execTime / 1000000000)));
				lock.synchronized {
					lock.notify
				}

			};
			true
		});

		val a = new VarA[Int](12);
		val b = new VarA[Int](12);


		val sdf = 1
		println("Start");
		startTime = System.nanoTime
		for (i <- 0 to noMsg) {
			sources.foreach(x => x() = 123)
		}

		lock.synchronized {
			lock.wait
		}

		System.err.println("=====================");

		/*

	var src2 = List[Var[Int]]()

		for (i <- 0 to 5) {
			src2 ::= new Var[Int](0)
		}

		var dst = List[Var[Int]]()
		for (i <- 0 to 5) {
			dst ::= new Var[Int](0)
		}

		var dst2 = List[Var[Int]]()
		for (i <- 0 to 5) {
			dst2 ::= new Var[Int](0)
		}

		for (i <- 0 to 5) {
			Signal {
				dst(i)() = src2(i)()
			}.setAlwaysActive
		}

		for (i <- 0 to 5) {
			Signal {
				dst2(i)() = dst(i)()
			}.setAlwaysActive
		}

		var numRecvMsg2 = 0;
		var endTime2:Long = 0;
		var startTime2:Long = 0;
		for (i <- 0 to 5) {
			observe(dst2(i)) {
				x => {
					numRecvMsg2 = numRecvMsg2 + 1

					println("!!! "+x)
					if (x == (noMsg) - 1) {
						val endTime2 = System.nanoTime;
						val execTime2 = endTime2 - startTime2

						System.err.println("!! Exectime " + execTime2 + " #msg " + numRecvMsg2 + " msg/s " + (numRecvMsg2 / (execTime2 / 1000000000)));
						lock.synchronized {
							lock.notify
						}

					}
					true

				}
			}
		}

		startTime2 = System.nanoTime
		for (i <- 0 to noMsg) {
			src2.foreach({ x=> x() = i})
			println(i)
		}
		    */
		/*lock.synchronized {
			lock.wait
		} */
		/*
				val test = new HashMap[Int, String]
				var vals = new Array[Int](1001)
				var r = new Random

				for(i <- 0 to 1000) {
					val j = r.nextInt
					val k = r.nextDouble+" "

					test.put(j,k)

					vals(i) = j
				}

				var ijk = 0;

				var startTime2 = System.nanoTime

				for(i <- 0 to 100000) {
					vals.foreach(x => {val z = test.get(x);
						ijk = ijk + 1;})
				}

				var endTime2 = System.nanoTime;
				val execTime2 = endTime2 - startTime2
				println(execTime2)
				println(ijk)
				System.err.println("!! Exectime " + execTime2 + " #msg " + ijk + " msg/s " + (ijk / (execTime2 / 1000000000)));*/

		System.exit(0)

	}
}



