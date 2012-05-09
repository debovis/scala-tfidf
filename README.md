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
	$ ./install_wordnet.sh - to run test cases and similarity API

Generate Test Coverage Report

	$ sbt clean
	$ sbt coverage:compile
	$ sbt coverage:test

Package (Create Jar)

	$ sbt one-jar

Run Server

	$ java -jar <generated jar>

Similarity API
	
	data format accepted:
		- {"data": 
			{"data_set": [{"title":"doc1", "value":""},	{"title":"doc3", "value":""}],
			"comparison_document": {"title":"doc2", "value":""}
			}
		  }
	
	Test Example:
	curl -d '{"data": {"data_set": [{"title":"doc1", "value":"Maggie was an enthusiastic and creative team member leading SPARC520 to the Buy a Beam Victory!"},{"title":"doc3", "value":"Cara did a great job getting the food for our annual meeting."}, {"title":"doc4" ,"value": "Laney spent an entire night pealing and sticking... sticker on Nerf Darts and Kool-Aid packets to support SPARC and SPARCET swag...  You rock Laney! Thank you! :D"},{"title":"doc5", "value":"for bringing in a giant box o paint for the SPARCin buy a beam contribution"}], "comparison_document": {"title":"doc2", "value":"Rick was an enthusiastic and creative team member leading SPARC520 to the Buy a Beam Victory! "}}}' http://localhost:8080/similarity
	
	Same Example with readable syntax:
	curl -d '{"data": 
				{"data_set": [
					{"title":"doc1", "value":"Maggie was an enthusiastic and creative team member leading SPARC520 to the Buy a Beam Victory!"},
					{"title":"doc3", "value":"Cara did a great job getting the food for our annual meeting."}, 
					{"title":"doc4" ,"value": "Laney spent an entire night pealing and sticking... sticker on Nerf Darts and Kool-Aid packets to support SPARC and SPARCET swag...  You rock Laney! Thank you! :D"},
					{"title":"doc5", "value":"for bringing in a giant box o paint for the SPARCin buy a beam contribution"}
				], 
				"comparison_document": {"title":"doc2", "value":"Rick was an enthusiastic and creative team member leading SPARC520 to the Buy a Beam Victory! "}
				}
			}' http://localhost:8080/similarity
