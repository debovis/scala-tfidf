package com.sparcedge.analytics.similarity

import akka.actor.Actor
import akka.dispatch.Future
import collection.JavaConversions._
import org.apache.commons.collections15.bag.HashBag
import org.slf4j.LoggerFactory

import com.sparcedge.analytics.indexers.matrix.TfObject
import com.sparcedge.analytics.Timer

import com.sparcedge.analytics.mongodb.MongoCollectionWrapper

class TfIdfCollectionManager(var elements: List[TfIdfElement], configPath: String, configMap: Map[String,String]) extends Actor {

	var tfIdf = new TfIdfGenerator(elements,true,configMap)
	val connection = new MongoCollectionWrapper(configPath, "sparciq")
	val similarityDatabase = new SimilarityElementDatabase(connection)
	similarityDatabase.updateWordFrequencies(elements)
	var updatedElements = false
	val log = LoggerFactory.getLogger(getClass)
	val timer = new Timer()

	def receive = {
		case AddElement(element,key) =>
		  	log.debug("added document")
		  	similarityDatabase.insertTextElement(element.id, element.value, key)
			updatedElements = true
			elements = element :: elements
		case RemoveElement(elementId,key) =>
			updatedElements = true
			similarityDatabase.deleteTextElement(elementId, key)
			elements = elements.filterNot(_.id == elementId)
		case UpdateTfIdfCollection() =>
			if(updatedElements) {
					val future = Future {
					 	log.debug("found updated elements")
						val newTfIdf = TfIdfGenerator.addAndRemoveDocuments(elements,tfIdf)
						self ! ReplaceTfIdfCollection(newTfIdf)
						similarityDatabase.updateWordFrequencies(elements)
					}
					updatedElements = false
				} 
		case ReplaceTfIdfCollection(newTfIdf) => 
		  	log.debug("replacing collection")
			tfIdf = newTfIdf
		case TfIdfGeneratorRequest() =>
			self.reply(tfIdf)
		case _ =>
	}
}

case class UpdateTfIdfCollection()

case class ReplaceTfIdfCollection(tfIdf: TfIdfGenerator)

case class AddElement(element: TfIdfElement,key: String)

case class RemoveElement(id: Int,key: String)

case class TfIdfGeneratorRequest()

case class SimpleTFElement(
    id:Int,
    var value:String,
    var words:HashBag[String]
)

// Todo: replace value with actually type needed by generator
case class TfIdfElement(
    id: Int, 
    _id: Object, 
    value: String, 
    var words:HashBag[String], 
    var hasWordFrequencies: Boolean, 
    var recentlyAdded: Boolean,
    var persistedToDB: Boolean){
  def repr() = {
    "id - %d, value - %s".format(id,value)
  }
}
