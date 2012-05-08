organization := "com.sparcedge"

name := "analytics-engine"

version := "0.1"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Spray Repo" at "http://repo.spray.cc"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

seq(ScctPlugin.scctSettings: _*)


libraryDependencies ++= Seq (
	"cc.spray" % "spray-server" % "0.9.0" % "compile",
	"cc.spray" %  "spray-can" % "0.9.3" % "compile",
	"net.liftweb" %% "lift-json" % "2.4-M4",
	"net.databinder" %% "dispatch-http" % "0.8.7",
	"com.mongodb.casbah" % "casbah_2.9.0-1" % "2.1.5.0",
	"se.scalablesolutions.akka" % "akka-actor" % "1.3.1",
	"se.scalablesolutions.akka" % "akka-slf4j" % "1.3.1",
	"org.slf4j" % "slf4j-api" % "1.6.1",
	"ch.qos.logback" % "logback-classic" % "0.9.29",
	"org.scalatest" %% "scalatest" % "1.6.1" % "test",
	"org.mockito" % "mockito-all" % "1.9.0" % "test",
	"org.apache.lucene" % "lucene-core" % "3.5.0",
	"org.apache.commons" % "commons-math3" % "3.0",
	"net.sourceforge.collections" % "collections-generic" % "4.01",
	"com.ibm.icu" % "icu4j" % "3.8",
	"commons-io" % "commons-io" % "2.2",
	"commons-logging" % "commons-logging" % "1.1.1",
	"commons-lang" % "commons-lang" % "2.3",
	"edu.mit" % "jwi" % "2.2.2",
	"joda-time" % "joda-time" % "2.1",
	"org.joda" % "joda-convert" % "1.2"
) 
