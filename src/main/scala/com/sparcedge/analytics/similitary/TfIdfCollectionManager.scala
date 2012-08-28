package com.sparcedge.analytics.similarity

import akka.actor.Actor
import akka.dispatch.Future
import collection.JavaConversions._

import com.sparcedge.analytics.indexers.matrix.TfIdfGenerator

class TfIdfCollectionManager(var elements: List[TfIdfElement], configMap: Map[String,String]) extends Actor {

	var tfIdf = new TfIdfGenerator(convertElementListToMap(elements),true,configMap)
	var updatedElements = false

	def receive = {
		case AddElement(element) =>
			updatedElements = true
			elements = element :: elements
		case RemoveElement(elementId) =>
			updatedElements = true
			elements = elements.filterNot(_.id == elementId)
		case UpdateTfIdfCollection() =>
			println("checking for added documents")
			if(updatedElements) {
				val future = Future {
					val newTfIdf = TfIdfGenerator.addAndRemoveDocuments(convertElementListToMap(elements),tfIdf)
					self ! ReplaceTfIdfCollection(newTfIdf)
				}
				updatedElements = false
			}
		case ReplaceTfIdfCollection(newTfIdf) => 
			tfIdf = newTfIdf
		case TfIdfGeneratorRequest() =>
			self.reply(tfIdf)
		case _ =>
	}

	def convertElementListToMap(elements: List[TfIdfElement]): Map[String,String] = {
		elements.map(el => (el.id.toString, el.value)).toMap
	}

}

case class UpdateTfIdfCollection()

case class ReplaceTfIdfCollection(tfIdf: TfIdfGenerator)

case class AddElement(element: TfIdfElement)

case class RemoveElement(id: Int)

case class TfIdfGeneratorRequest()

// Todo: replace value with actually type needed by generator
case class TfIdfElement(id: Int, value: String)