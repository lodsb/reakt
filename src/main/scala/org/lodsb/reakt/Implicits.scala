package org.lodsb.reakt

object Implicits {

  implicit def something2ConstantSignal[SC](something: SC) = new ConstantSignal(something)


  /*
    *AAARRRRGGGG*
    there is no way to explicitly convert the boxed types of java to those of scala, this leads to
    signal types of e.g. Signal[java.lang.Float] when using these in Java Objects (Property<Float>),
    this again creates problems with all the overloaded
    and implicit methods. so it is necessary to add some additional implicit typing etc...
    *ARRRRGGGHHH*
   */
 	implicit def f2ConstantF2Signal(something: Float):TSignalet[java.lang.Float] =
                                  new ConstantSignal[java.lang.Float](something)
  implicit def d2ConstantD2Signal(something: Double):TSignalet[java.lang.Double] =
                                  new ConstantSignal[java.lang.Double](something)
  implicit def i2ConstantI2Signal(something: java.lang.Integer):TSignalet[java.lang.Integer] =
                                  new ConstantSignal[java.lang.Integer](something)


  implicit val javaFloat2Numeric = new Numeric[java.lang.Float] {
        def plus(x: java.lang.Float, y: java.lang.Float): java.lang.Float = x.floatValue() + y
        def minus(x: java.lang.Float, y: java.lang.Float): java.lang.Float = x.floatValue() - y
        def times(x: java.lang.Float, y: java.lang.Float): java.lang.Float = x.floatValue() * y
        def negate(x: java.lang.Float): java.lang.Float = -x.floatValue()
        def fromInt(x: Int): java.lang.Float = x
        def toInt(x: java.lang.Float): Int = x.floatValue().toInt
        def toLong(x: java.lang.Float): Long = x.floatValue().toLong
        def toFloat(x: java.lang.Float): Float = x.floatValue()
        def toDouble(x: java.lang.Float): Double = x.doubleValue()

        def compare(x: java.lang.Float, y: java.lang.Float) = java.lang.Float.compare(x, y)
      }

  implicit val javaDouble2Numeric = new Numeric[java.lang.Double] {
        def plus(x: java.lang.Double, y: java.lang.Double): java.lang.Double = x.doubleValue() + y
        def minus(x: java.lang.Double, y: java.lang.Double): java.lang.Double = x.doubleValue() - y
        def times(x: java.lang.Double, y: java.lang.Double): java.lang.Double = x.doubleValue() * y
        def negate(x: java.lang.Double): java.lang.Double = -x.doubleValue()
        def fromInt(x: Int): java.lang.Double = x
        def toInt(x: java.lang.Double): Int = x.doubleValue().toInt
        def toLong(x: java.lang.Double): Long = x.doubleValue().toLong
        def toFloat(x: java.lang.Double): Float = x.floatValue()
        def toDouble(x: java.lang.Double): Double = x.doubleValue()

        def compare(x: java.lang.Double, y: java.lang.Double) = java.lang.Double.compare(x, y)
      }
  
  implicit val javaInt2Numeric = new Numeric[java.lang.Integer] {
        def plus(x: java.lang.Integer, y: java.lang.Integer): java.lang.Integer = x.intValue() + y
        def minus(x: java.lang.Integer, y: java.lang.Integer): java.lang.Integer = x.intValue() - y
        def times(x: java.lang.Integer, y: java.lang.Integer): java.lang.Integer = x.intValue() * y
        def negate(x: java.lang.Integer): java.lang.Integer = -x.intValue()
        def fromInt(x: Int): java.lang.Integer = x
        def toInt(x: java.lang.Integer): Int = x.intValue()
        def toLong(x: java.lang.Integer): Long = x.intValue().toLong
        def toFloat(x: java.lang.Integer): Float = x.floatValue()
        def toDouble(x: java.lang.Integer): Double = x.doubleValue()

        def compare(x: java.lang.Integer, y: java.lang.Integer) = x.compareTo(y)
      }

}
