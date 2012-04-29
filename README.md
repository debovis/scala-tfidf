Analytics Engine
=============

Scala code base for the analytics engine built on top of akka and spray

Prerequisites
------------

* [SBT](https://github.com/harrah/xsbt) -- [install instructions](https://github.com/harrah/xsbt/wiki/Getting-Started-Setup)
* [Scala IDE (Eclipse)](http://www.scala-ide.org/) -- [install](http://download.scala-ide.org/)
* Install turbine-common in local ivy repository (sbt publish-local)

Getting Started
------------

Download Project References
	
	$ sbt update

Generate Eclipse Project

	$ sbt eclipse

Compile

	$ sbt compile
	
Test
	
	$ sbt test

Generate Test Coverage Report

	$ sbt clean
	$ sbt coverage:compile
	$ sbt coverage:test

Package (Create Jar)

	$ sbt one-jar

Run Server

	$ java -jar <generated jar>

Similarity API

	data format:
		- {"data": 
			{"data_set": [{"title":"doc1", "value":""},	{"title":"doc3", "value":""}],
			"comparison_document": [{"title":"doc2", "value":""}]
			}
		  }'