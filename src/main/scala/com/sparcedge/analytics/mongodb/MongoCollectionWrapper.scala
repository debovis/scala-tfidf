package com.sparcedge.analytics.mongodb

import com.mongodb.casbah.{MongoConnection,MongoCollection, MongoDB}
import com.mongodb.casbah.Imports.MongoDBObject
import com.mongodb.DBObject
import com.mongodb.Mongo

import org.apache.commons.lang.StringUtils;

import net.liftweb.json._

class MongoCollectionWrapper(item:String) {
  
	val mongoStoreConfig:MongoDBStore = MongoDBStore.apply(item)
	val dbName = mongoStoreConfig.dbName
	val collName = mongoStoreConfig.collection

	def getCollection: MongoCollection = {
		val mongo = MongoConnection(mongoStoreConfig.server, mongoStoreConfig.port)
		if(!StringUtils.isEmpty(mongoStoreConfig.username) && !StringUtils.isEmpty(mongoStoreConfig.password)){
			if(mongo.getDB(dbName).authenticate(mongoStoreConfig.username, mongoStoreConfig.password)){
				println("username - password auth used, connection established")
			}
		}
		mongo(dbName)(collName)
	}
	def getConnection: MongoConnection = {
		MongoConnection(mongoStoreConfig.server,mongoStoreConfig.port)
	}
}

class MongoDBStore (
	val server: String,
	val port: Int = 27017,
	val dbName: String,
	val collection: String,
	val username:String,
	val password:String
)

object MongoDBStore {
	
	def apply(item:String): MongoDBStore = {
		val mongoEntry = parse(scala.io.Source.fromFile("analytics-engine-config.json").mkString) \ item
		val host = JsonHelper.toString(mongoEntry \ "server" \ "host")
		val port = JsonHelper.toInt(mongoEntry \ "server" \ "port")
		val dbName = JsonHelper.toString(mongoEntry \ "database")
		val collection = JsonHelper.toString(mongoEntry \ "collection")
		val username = JsonHelper.toString(mongoEntry \ "username")
		val password = JsonHelper.toString(mongoEntry \ "password")

		return new MongoDBStore (
			server = host,
			port = port,
			dbName = dbName,
			collection = collection,
			username = username,
			password = password
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