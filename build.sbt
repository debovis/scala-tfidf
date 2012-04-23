organization := "com.sparcedge"

name := "analytics-engine"

version := "0.1"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Spray Repo" at "http://repo.spray.cc"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

seq(ScctPlugin.scctSettings: _*)

seq(Twirl.settings: _*)

libraryDependencies ++= Seq (
	"cc.spray" % "spray-server" % "0.9.0" % "compile",
	"cc.spray" %  "spray-can" % "0.9.3" % "compile",
	"net.liftweb" %% "lift-json" % "2.4-M4",
	"se.scalablesolutions.akka" % "akka-actor" % "1.3.1",
	"se.scalablesolutions.akka" % "akka-slf4j" % "1.3.1",
	"org.slf4j" % "slf4j-api" % "1.6.1",
	"ch.qos.logback" % "logback-classic" % "0.9.29",
	"org.scalatest" %% "scalatest" % "1.6.1" % "test",
	"org.mockito" % "mockito-all" % "1.9.0" % "test",
	"org.apache.lucene" % "lucene-core" % "3.5.0"
) 