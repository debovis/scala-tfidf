package com.sparcedge.analytics.mongodb

import com.mongodb.casbah.{MongoConnection,MongoCollection}
import com.mongodb.casbah.Imports.MongoDBObject
import com.mongodb.DBObject
import com.mongodb.Mongo

import net.liftweb.json._

class MongoCollectionWrapper {
  
	val mongoStoreConfig:MongoDBStore = MongoDBStore.apply
	val dbName = mongoStoreConfig.dbName
	val collName = mongoStoreConfig.collection

	def getCollection: MongoCollection = {
	    return MongoConnection(mongoStoreConfig.server, mongoStoreConfig.port)(dbName)(collName)
	}
	def getConnection: MongoConnection = {
	  return MongoConnection(mongoStoreConfig.server,mongoStoreConfig.port)
	}
}

class MongoDBStore (
	val server: String,
	val port: Int = 27017,
	val dbName: String,
	val collection: String 
)

object MongoDBStore {

	val mongoEntry = parse(scala.io.Source.fromFile("analytics-engine-config.json").mkString) \ "analytics-engine"
	
	def apply: MongoDBStore = {
		val host = JsonHelper.toString(mongoEntry \ "server" \ "host")
		val port = JsonHelper.toInt(mongoEntry \ "server" \ "port")
		val dbName = JsonHelper.toString(mongoEntry \ "database")
		val collection = JsonHelper.toString(mongoEntry \ "collection")

		return new MongoDBStore (
			server = host,
			port = port,
			dbName = dbName,
			collection = collection
		)
	}
}

object JsonHelper {
	def toString(jStringValue: JValue): String = {
		jStringValue.asInstanceOf[JString].values
	}

	def toInt(jIntValue: JValue): Int = {
		jIntValue.asInstanceOf[JInt].values.toInt
	}
}