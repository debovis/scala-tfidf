package com.sparcedge.analytics.similarity

import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.query._
import com.sparcedge.analytics.mongodb.MongoCollectionWrapper

object SimilarityElementCollector {

	def retrieveTextElements(apiKey: String): List[TfIdfElement] = {
	
		var elements = List[TfIdfElement]()
		val elementLimit = 2000

		val connection = new MongoCollectionWrapper("similarity")
		val collection = connection.getCollection
	  
		val q = MongoDBObject(("apiKey" -> apiKey))

		collection.find(q).limit(elementLimit).foreach { doc =>
			val obj = doc.asDBObject
			var value = ""
			if(obj.get("value") != null) {
				value = obj.as[String]("value")
			}
			var id = 0
			if(obj.get("id") != null) {
				id = obj.as[String]("id").toInt
			}
			elements = TfIdfElement(id, value) :: elements
		}
		elements
	}
}







