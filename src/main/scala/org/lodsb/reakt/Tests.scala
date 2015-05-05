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

import async.VarA
import org.lodsb.reakt.Implicits._
import sync.VarS


object Tests {

  var pipelength : Int = 0;
  var rounds : Int = 0;
  var evalType : String = ""

  // add raw function calling
	def main(args: Array[String]) = {

    Reactive.startup 

    println(args.mkString(","))

    if(args.length < 3) {
      println("Error: not enough params, type [f, s, a] pipelength rounds")
      System.exit(0)
    }

		val x = new VarA[Int](5)
		val z = new VarA[String](" WORLD")

		x.observe({ff => println(ff); true})
		//x.start;

		val y = new VarS[Int](0)
		val u = new VarA[Int](2000)

    val src = new VarS[Long](0)
    val dst = new VarS[Long](0)

    dst <~ src

    println("running...")

    pipelength = args(1).toInt
    rounds = args(2).toInt
    evalType = args(0)



    args(0) match {
      case "f" => runTest2(rounds, pipelength)
      case "s" => {
        var g = genGraph(pipelength, ()=> new VarS[Long](0))
        runTest1(g._1, g._2, "Synchronous", nums = rounds)
      }
      case "a" => {
        var g = genGraph(pipelength, ()=> new VarA[Long](0))
        runTest1(g._1, g._2, "Asynchronous", nums = rounds)

        Thread.sleep(600000)
      }
      case _ => println("Error, wrong test case")
    }

    /*

    runTest1(src, dst, "Test1")

    println("=====")
    var g = genGraph(2, ()=> new VarS[Long](0))
    runTest1(g._1, g._2, "Test 1b")

    println("=====")
    g = genGraph(2, ()=> new VarA[Long](0))
    runTest1(g._1, g._2, "Test 1c")

    Thread.sleep(60000);

    println("=====")
    g = genGraph(2, ()=> new VarA[Long](0))
    runTest1(g._1, g._2, "Test 1d")

    Thread.sleep(60000);

*/

    //
    /*
    val s1 = new VarS[Long](0)
    val s2 = new VarS[Long](0)
    val s3 = new VarS[Long](0)
    val s4 = new VarS[Long](0)
    val d2 = new VarS[Long](0)

    s2 <~ s1
    s3 <~ s2
    s4 <~ s3
    d2 <~ s4

    runTest1(s1, d2, "Test2")

    println("---")
    Thread.sleep(60000);
    val srcA = new VarA[Long](0)
    val dstA = new VarA[Long](0)

    dstA <~ srcA
    runTest1(srcA, dstA, "Test3A")

    println("---")
    Thread.sleep(60000);
    val s1A = new VarA[Long](0)
    val s2A = new VarA[Long](0)
    val s3A = new VarA[Long](0)
    val s4A = new VarA[Long](0)
    val d2A = new VarA[Long](0)

    s2A <~ s1A
    s3A <~ s2A
    s4A <~ s3A
    d2A <~ s4A

    runTest1(s1A, d2A, "Test4A")

    Thread.sleep(60000);
    */
    //println("baseline test")
    //runTest2()

    Reactive.shutdown

    System.exit(0)

  }

  def genGraph(nums: Int, f: () => TVar[Long]) : (TVar[Long], TVar[Long]) = {
    val graphElems = (1 to nums).map(x=>f())
    // connected pipe of length nums
    graphElems.tail.zip(graphElems).foreach( x=> x._1 <~ x._2)

    (graphElems.head,graphElems.last)
  }

  def runTest2(nums: Int = 10000000, length: Int=2) {

    val s = "BaselineTest"

    val start = System.nanoTime()

    val array = Array.fill(nums){( (1 to length).foldLeft(0L)((x,y) => x+y), time - start)};
    val t = time

    val meanTimeDiff = array.map( x => x._2.toDouble).sum / nums.toDouble
    val medianArray = (array.map( x => x._2.toDouble).sorted)
    val medianTimeDiff = medianArray(nums/2)
    val bestTimeDiff = medianArray.head
    val worstTimeDiff = medianArray.last


    val meanTime =  nums.toDouble / ( (t-start).toDouble / 1.0e9)

    println(s+"\n*******"+"\nNano delay/jitter (mean) "+meanTimeDiff + "\nNano delay/jitter (median) "+ medianTimeDiff +"\nMessages/s " + meanTime)
    println(">>> , "+evalType+" , "+pipelength+" , "+rounds+","+meanTimeDiff + " , "+ medianTimeDiff +" , " + meanTime.toInt+ ", " + bestTimeDiff + " , "+ worstTimeDiff)

    Reactive.shutdown
    System.exit(0)

  }

  def runTest1(a: TVar[Long], b: TVar[Long], s: String, nums: Int = 10000000) {
    val array: Array[(Long, Long)] = Array.fill(nums){(0.asInstanceOf[Long],0.asInstanceOf[Long])};
    var idx = 0;
    val start = time


    b.observe{ number =>
      val t = time

      if(idx >= nums) {
        val meanTimeDiff = array.map( x => x._1.toDouble).sum / nums.toDouble
        val medianArray = (array.map( x => x._1.toDouble).sorted)//.slice(0, (0.3*nums.toFloat).toInt);
        val medianTimeDiff = medianArray(medianArray.length/2)
        val bestTimeDiff = medianArray.head
        val worstTimeDiff = medianArray.last

        val meanTime =  nums.toDouble / ( (t-start).toDouble / 1.0e9)

        println(s+"\n*******"+"\nNano delay/jitter (mean) "+meanTimeDiff + "\nNano delay/jitter (median) "+ medianTimeDiff +"\nMessages/s " + meanTime+"\nBest: "+bestTimeDiff+"\nWorst: "+worstTimeDiff)
        println(">>> , "+evalType+" , "+pipelength+" , "+rounds+","+meanTimeDiff + " , "+ medianTimeDiff +" , " + meanTime.toInt + ", " + bestTimeDiff + " , "+ worstTimeDiff)

        //println(array.mkString(","))

        Reactive.shutdown
        System.exit(0)

      } else {
        array(idx) = (t-number, t-start)

        idx = idx + 1

      }


      true

    }

    (0 to nums).foreach({ x =>
      a() = time
    })

  }

  def time = {
    System.nanoTime()
  }
}
