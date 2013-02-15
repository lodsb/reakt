name := "reakt"

organization := "org.lodsb"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.2"


scalacOptions ++= Seq("-unchecked", "-deprecation", "-Dfile.encoding=ISO-8859-1") //, "-Xprint:typer")

scalacOptions <++= scalaVersion map { version =>
  val Version = """(\d+)\.(\d+)\..*"""r
  val Version(major0, minor0) = version map identity
  val (major, minor) = (major0.toInt, minor0.toInt)
  if (major < 2 || (major == 2 && minor < 10)) 
  	Seq("-Ydependent-method-types")
 	else Nil
}

libraryDependencies += "com.assembla.scala-incubator" % "graph-core_2.9.2" % "1.5.1"

unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist"))


