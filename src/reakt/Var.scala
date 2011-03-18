
package reakt

class Var[T](_value: T) extends Val[T](_value) {
	def update(newValue: T) = {
		this.onUpdateValue(newValue)
		this.emit(newValue)
	}

	def <~[B >: T](that: Signal[B]) : Signal[B] = {
		Reactive.connect(that, this)

		that
	}
}
