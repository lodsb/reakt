/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2007-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.react

object Var {
	def apply[A](init: A) = new Var(init)
}

/**
 * A source signal that saves its value in a variable.
 */
class Var[A](protected[react] var _value: A) extends SourceSignal[A] {
	def update(newValue: A) = if (_value != newValue) {
		_value = newValue
		emit()
	}
}

case class Deferable[A](varDefer: VarDefering[A], value: A)

class VarDefering[A](protected val deferor: VarDeferor, protected[react] var _value: A) extends SourceSignal[A] {
	def updateCallback(newValue: A) = if (_value != newValue) {
		_value = newValue
		emit()
	}

	def update(newValue: A) = {
		deferor.defer(Deferable(this, newValue))
	}
}


