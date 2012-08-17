Analytics Engine
=============

Scala code base for the analytics engine built on top of akka and spray

Prerequisites
------------

* [SBT](https://github.com/harrah/xsbt) -- [install instructions](https://github.com/harrah/xsbt/wiki/Getting-Started-Setup)
* [Scala IDE (Eclipse)](http://www.scala-ide.org/) -- [install](http://download.scala-ide.org/)
* MongoDB to run locally

Getting Started
------------

Install

	$ ./install.sh - to install WordNet files and create mongoDB restore

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
	
	GET - http:// HOST /similarity
        PARAMETERS - { q : "document", apiKey : "apiKey"}
        RESPONSE - {"similarity":[{"id":"100052","similarityScore":1.0,"document":"document"}]}

    PUT - http:// HOST /similarity/?id
        PARAMETERS - { apiKey : "apiKey"}
        RESPONSE - {"created": "true"}

    DELETE = http:// HOST /similarity/?id
        PARAMETERS - { apiKey : "apiKey"}
        RESPONSE - {"deleted": "true"}

NamedEntity API

	data format accepted:
		- {"data": 
			{"data_set": [{"title":"doc1", "value":""},	{"title":"doc3", "value":""}]}
		  }

	Example:
		- From running: $ python src/test/python/testNer.py

    {"data": [
        {
            "title": "doc0",
            "value": "Can you explain Data Control Language (DCL)?"
        }]
	}
	{
	    "NamedEntities": [
	        {
	            "freq": 1,
	            "word": "Data Control Language"
	        }
    	]
	}

