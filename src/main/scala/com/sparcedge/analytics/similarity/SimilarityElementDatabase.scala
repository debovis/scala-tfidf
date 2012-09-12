package com.sparcedge.analytics.similarity

import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.query._
import com.mongodb.casbah.Imports.BasicDBList
import com.sparcedge.analytics.mongodb.MongoCollectionWrapper
import org.apache.commons.collections15.bag.HashBag
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory

class SimilarityElementDatabase(connection: MongoCollectionWrapper) {
  
	val log = LoggerFactory.getLogger(getClass)

	def retrieveTextElements(apiKey: String): List[TfIdfElement] = {
	
		var elements = List[TfIdfElement]()
		val elementLimit = 10000
		val collection = connection.getCollection
		var wfCounter =0
	
		collection.ensureIndex(MongoDBObject(("id" -> 1), ("apiKey" -> 1)), "id_index", true)

		val q = MongoDBObject(("apiKey" -> apiKey))

		collection.find(q).limit(elementLimit).foreach { doc =>
			val obj = doc.asDBObject
			var documentText = ""
			var hasWordFrequencies=false
			var _id = obj.get("_id")
			
			if(obj.get("value") != null) {
				documentText = obj.as[String]("value")
			}
			var id = 0
			if(obj.get("id") != null) {
				id = obj.as[Int]("id")
			}
			var wordFrequenciesList = List[(String,Int)]()
			var wordFrequencies = new HashBag[String]
			
			if(obj.get("wordFrequencies") != null) {
			  var freqMap = obj.as[BasicDBList]("wordFrequencies").map { sl =>
			     val list = sl.asInstanceOf[BasicDBList]
			     (list(0).asInstanceOf[String], list(1).asInstanceOf[Int])
			  }.toMap
			  
			  if(freqMap.size>0) {
				  for(value <- freqMap){
				    val frequencyItem = value
				    wordFrequencies.add(frequencyItem._1,frequencyItem._2)
				  }
				  hasWordFrequencies=true
				  wfCounter += 1
			  }
			}
			elements = new TfIdfElement(id, _id,documentText,wordFrequencies,hasWordFrequencies,false,true) :: elements
		}
		log.debug("word frequencies were found for %d docs".format(wfCounter))
		elements
	}
	
	def updateWordFrequencies(elements: List[TfIdfElement]): Boolean = {
	  val collection = connection.getCollection
	  for(element <- elements){
	    val words = element.words.uniqueSet().toArray().map(word => (word, element.words.getCount(word.toString ) ))
	    val wordBagTuple = words.toList
	    collection.update(MongoDBObject("id" -> element.id), $set("wordFrequencies" -> wordBagTuple))
	    element.persistedToDB = true
	  }
	  log.debug("updated %d mongo objects with frequency".format(elements.size))
	  true
	}

	def insertTextElement(id: Int, value: String, apiKey: String): Boolean = {
		val element = MongoDBObject (
			("id" -> id),
			("value" -> value),
			("apiKey" -> apiKey),
			("wordFrequencies" -> List())
		)
		try {
			connection.getCollection += element
			connection.getCollection
			true
		} catch {
			case e: Exception =>
				e.printStackTrace
				false
		}
	}

	def deleteTextElement(id: Int, apiKey: String): Boolean = {
		val element = MongoDBObject (
			("id" -> id),
			("apiKey" -> apiKey)
		)
		try {
			connection.getCollection -= element
			true
		} catch {
			case e: Exception =>
				e.printStackTrace
				false
		}
	}
}