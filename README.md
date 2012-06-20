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


	To test out some sparcIN similarity:
		$ python src/test/python/testSim.py 

NamedEntity API

	data format accepted:
		- {"data": 
			{"data_set": [{"title":"doc1", "value":""},	{"title":"doc3", "value":""}]}
		  }

	Example:
		- From running: $ python src/test/python/testNer.py

    {"data": {
        "data_set": [
            {
                "title": "doc0",
                "value": "Can you explain Data Control Language (DCL)?"
            },
            {
                "title": "doc1",
                "value": "Can you explain Data Manipulation Language (DML)?"
            },
            {
                "title": "doc2",
                "value": "Can you explain Data Definition Language (DDL)?"
            },
            {
                "title": "doc3",
                "value": "What is the difference between Oracle, SQL, and SQL Server?"
            },
            {
                "title": "doc4",
                "value": "What is a CASE statement?"
            },
            {
                "title": "doc5",
                "value": "What type of join would be used to include rows that do not have matching values?"
            },
            {
                "title": "doc6",
                "value": "What are foreign keys?"
            },
            {
                "title": "doc7",
                "value": "Can you explain the meaning of the word 'join'? What are some examples of different types of joins?"
            },
            {
                "title": "doc8",
                "value": "What is a Primary Key?"
            },
            {
                "title": "doc9",
                "value": "Can you give an example of a basic SQL statement to read data out of a table?"
            },
            {
                "title": "doc10",
                "value": "In SQL Server, what\u2019s the difference between Mixed-Mode Authentication and Windows Authentication?"
            },
            {
                "title": "doc11",
                "value": "What is SQL?"
            }]
    	}
	}
	{
	    "NamedEntities": [
	        {
	            "freq": 1,
	            "word": "Data Definition Language"
	        },
	        {
	            "freq": 2,
	            "word": "Oracle"
	        },
	        {
	            "freq": 1,
	            "word": "Data Control Language"
	        },
	        {
	            "freq": 1,
	            "word": "Data Manipulation Language"
	        }
    	]
	}

