package com.sparcedge.analytics.similarity

import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.query._
import com.sparcedge.analytics.mongodb.MongoCollectionWrapper

object SimilarityElementDatabase {

	val connection = new MongoCollectionWrapper("sparciq")

	def retrieveTextElements(apiKey: String): List[TfIdfElement] = {
	
		var elements = List[TfIdfElement]()
		val elementLimit = 2000
		val collection = connection.getCollection
	
		collection.ensureIndex(MongoDBObject(("id" -> 1), ("apiKey" -> 1)), "id_index", true)

		val q = MongoDBObject(("apiKey" -> apiKey))

		collection.find(q).limit(elementLimit).foreach { doc =>
			val obj = doc.asDBObject
			var value = ""
			if(obj.get("value") != null) {
				value = obj.as[String]("value")
			}
			var id = 0
			if(obj.get("id") != null) {
				id = obj.as[Int]("id")
			}
			elements = TfIdfElement(id, value) :: elements
		}
		elements
	}

	def insertTextElement(id: Int, value: String, apiKey: String): Boolean = {
		val element = MongoDBObject (
			("id" -> id),
			("value" -> value),
			("apiKey" -> apiKey)
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