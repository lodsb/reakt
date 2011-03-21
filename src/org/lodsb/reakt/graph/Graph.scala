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
import org.lodsb.reakt.{Var, Reactive}
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

case class Edge(source: NodeBase[_], destination: NodeBase[_])

case class Propagate[T](circle: Long, source: NodeBase[_], message: T)


abstract class ReactiveGraph {
	protected var edges = List[Edge]()
	protected var nodes = List[NodeBase[_]]()
}

trait NodeBase[+T] extends Actor {
	private var dependants = List[NodeBase[_]]()
	private var dependson = new HashMap[NodeBase[_], Boolean]()

	protected def onUpdateValue[B >: T](value: B): Unit = {}

	def addDependant(d: NodeBase[_]): Unit = {
		this.dependants ::= d
	}

	//override def scheduler = Reactive.scheduler

	def addDependingOn(source: NodeBase[_]): Unit = {
		dependson.put(source, true)
	}

	var cntr = 0;

	def emit[T](m: T, c: Long = 0): Unit = {
		//println("emit! "+m+" "+dependants.size);
		dependants.foreach({
			node => node ! Propagate(c, this, m)
		})
	}

	def emit[T](m: T): Unit = {
		this.emit(m, Reactive.cycle)
	}

	def act() = {
		loop {
			react {
				case p@Propagate(c, s, msg) => {

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

				case _ => println("===????")
			}
		}
	}
}

object Test {
	val noMsg = 100000;
	var it = 5

	def main(args: Array[String]) = {
		val graph = Reactive

		var sources = List[Var[Int]]()

		for (i <- 0 to it) {
			val s = new Var(i)
			sources ::= s
			s.start
		}

		var destinations = List[Var[Int]]()
		for (i <- 0 to it) {
			val d = new Var[Int](i * 3)
			destinations ::= d
			d.start
		}


		for (i <- 0 to it) {
			graph.connect(sources(i), destinations(i))
		}

		var destinations2 = List[Var[Int]]()
		for (i <- 0 to it) {
			val d = new Var[Int](i * 23)
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
		val end = new Var[Int](12); //new org.lodsb.reakt.Reactive.Signal[Int](123)
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

		val a = new Var[Int](12);
		val b = new Var[Int](12);


		val sdf = 1
		println("Start");
		startTime = System.nanoTime
		for (i <- 0 to noMsg) {
			sources.foreach(x => x() = 123 )
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



