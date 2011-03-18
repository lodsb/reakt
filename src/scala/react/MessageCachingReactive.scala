/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2007-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.react

trait MessageCachingReactive[+Msg, +Now] extends Dependency[Msg, Now] {
	val lock = new Object;

	import scala.actors.Actor._

	val messageCachingActor = actor {
		loop {
			react {
				case dep: Dependent => {
					val e = this.actOnMessage(dep)
					reply(e)
				}
			}
		}
	}

	this.messageCachingActor.start

	protected[react] def actOnMessage(dep: Dependent): Option[Msg] = {
		checkLevelNow()
		subscribe(dep)
		Engine.messageFor(this)
	}

	protected[react] def message(dep: Dependent): Option[Msg] = {
		(messageCachingActor !? dep).asInstanceOf[Option[Msg]]
		//this.actOnMessage(dep)
	}

	protected[this] def cacheMessage(msg: Msg) {
		Engine.setMessage(this, msg)
	}
}
