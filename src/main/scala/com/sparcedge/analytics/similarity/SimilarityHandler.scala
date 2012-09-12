package com.sparcedge.analytics.similarity

import akka.actor.{Actor,ActorRef}
import cc.spray.RequestContext
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes,HttpHeader,StatusCode}
import cc.spray.http.MediaTypes._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.util.LinkedHashMap
import collection.JavaConversions._

import org.slf4j.{Logger, LoggerFactory}

import org.apache.commons.math3.linear.{OpenMapRealMatrix,RealMatrix,RealVector,ArrayRealVector}
import org.apache.commons.collections15.Bag
import org.apache.commons.collections15.bag.HashBag

import com.sparcedge.analytics.indexers.matrix.WordFrequencyWrapper
import com.sparcedge.analytics.indexers.matrix.WordFrequencyWrapper._
import com.sparcedge.analytics.similaritytypes.CosineSimilarity
import com.sparcedge.analytics.similarity._


class SimilarityHandler extends Actor {
	implicit val formats = Serialization.formats(NoTypeHints)
	val log = LoggerFactory.getLogger(getClass)
  
	def receive = {
		case similarityRequest	(configMap,value, ctx, tfManager) =>
			val tfIdfGen = (tfManager ? TfIdfGeneratorRequest()).as[TfIdfGenerator].get
			val minimumSimilarityQualifier = .3
		  
			try {
				val corpusWords = tfIdfGen.wordSet.toList
				val comparisonWordSet = WordFrequencyWrapper.getWordFrequencies(FrequencyType.WORDNET, value, configMap)
				val comparisonWordKeySet = comparisonWordSet.uniqueSet()
				val comparisonVect = new ArrayRealVector(tfIdfGen.wordSet.size)

				var i = 0
				for(word <- corpusWords) {
					if(comparisonWordKeySet.contains(word.toString())) {
						var corpusWordFreq = tfIdfGen.corpusWordFrequency.getEntry(i)
						val documentsCount = tfIdfGen.tfMatrix.getColumnDimension()
						val comparisonWordCount = comparisonWordSet.getCount(word.toString())
						val finalTfIdf = (comparisonWordCount) * (1 + Math.log(documentsCount) - Math.log(corpusWordFreq))
						comparisonVect.setEntry(i,finalTfIdf)
					}
					i+=1
				}

				val similarityVect = new CosineSimilarity().similarity(tfIdfGen.tfidfMatrix,comparisonVect).toArray().toList
				val keys = tfIdfGen.documents.map(doc => doc.id).toList
				val documentMap = tfIdfGen.documents.map(doc => (doc.id,doc.value)).toMap
				var simList: List[similarityResult] = List()
			  
				// zip to make (key,similarity)
				keys.zip(similarityVect).foreach { x => 
					if(x._2.toDouble > minimumSimilarityQualifier) {
						val documentId = x._1
						val simScore = Math.round(x._2.toDouble * 10000).toDouble / 10000
						simList = new similarityResult(documentId, simScore, documentMap.get(documentId).get) :: simList
					}
				}
				// Sort similarity list
				simList = simList.sortWith(_.similarity > _.similarity)
			  
				ctx.complete (
					response(StatusCodes.OK, compact(render("similarity" -> simList.map {w => ("id" -> w.id) ~ ("similarityScore" -> w.similarity) ~ ("document" -> w.document)})))
				)	  
			} catch {
				case e:Exception => 
					println(e.printStackTrace())
					ctx.complete (
						response(StatusCodes.OK, compact(render("similarity" -> "[]")))
					)
			}
		case _ =>
	}
	def response(status:StatusCode, jsonResponse: String): HttpResponse = {
		HttpResponse (
			status = status,
			headers = Nil,
			content = HttpContent(`application/json`,jsonResponse)
		)
	}
}

class dataSet (
	val title : String,
	val value : String
)

class similarityResult (
	val id: Int,
	val similarity : Double,
	val document : String
)

case class similarityRequest(configmap: Map[String,String],value: String, ctx: RequestContext, tfManager: ActorRef)
