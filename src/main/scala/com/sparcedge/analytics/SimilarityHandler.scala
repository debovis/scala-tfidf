package com.sparcedge.analytics

import akka.actor.Actor
import cc.spray.RequestContext
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes,HttpHeader,StatusCode}
import cc.spray.http.MediaTypes._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.util.LinkedHashMap

import org.slf4j.{Logger, LoggerFactory}

import org.apache.commons.math3.linear.{OpenMapRealMatrix,RealMatrix,RealVector,ArrayRealVector}
import org.apache.commons.collections15.Bag
import org.apache.commons.collections15.bag.HashBag

import com.sparcedge.analytics.indexers.matrix.WordFrequencyWrapper
import com.sparcedge.analytics.indexers.matrix.WordFrequencyWrapper._
import com.sparcedge.analytics.similarity.matrix.CosineSimilarity
import com.sparcedge.analytics.indexers.matrix.TfIdfGenerator
import com.sparcedge.analytics.similitarycollector._


class SimilarityHandler extends Actor {
	implicit val formats = Serialization.formats(NoTypeHints)
	val log = LoggerFactory.getLogger(getClass)
  
	def receive = {
		case similarityRequest	(requestData, ctx,tfMatrix) =>
			val minimumSimilarityQualifier = .3
		  
			try {
				val comp = (requestData.get \ "data" \ "document").extract[dataSet]
				val corpusWords = tfMatrix.wordSet.toArray().toList
				val comparisonWordSet = WordFrequencyWrapper.getWordFrequencies(FrequencyType.WORDNET, comp.value)
				val comparisonWordKeySet = comparisonWordSet.uniqueSet()
				val comparisonVect = new ArrayRealVector(tfMatrix.wordSet.size())

				var i = 0
				for(word <- corpusWords) {
					if(comparisonWordKeySet.contains(word.toString())) {
						var corpusWordFreq = tfMatrix.corpusWordFrequency.getEntry(i)
						val documentsCount = tfMatrix.tfmatrix.getColumnDimension()
						val comparisonWordCount = comparisonWordSet.getCount(word.toString())
						val finalTfIdf = (comparisonWordCount) * (1 + Math.log(documentsCount) - Math.log(corpusWordFreq))
						comparisonVect.setEntry(i,finalTfIdf)
					}
					i+=1
				}

				val similarityVect = new CosineSimilarity().similarity(tfMatrix.tfidfMatrix,comparisonVect).toArray().toList
				val keys = tfMatrix.documents.keySet().toArray().toList
				var simList: List[similarityResult] = List()
			  
				// zip lists to return
				keys.zip(similarityVect).foreach { x => 
					if(x._2.toDouble > minimumSimilarityQualifier) {
						val simScore = Math.round(x._2.toDouble * 10000).toDouble / 10000
						simList = new similarityResult(x._1.toString(), simScore, tfMatrix.documents.get(x._1.toString())) :: simList
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
						response(StatusCodes.OK, compact(render("similarity" -> List().toString())))
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
	val id: String,
	val similarity : Double,
	val document : String
)

case class similarityRequest(requestData:Some[net.liftweb.json.JsonAST.JValue], ctx: RequestContext, tf:TfIdfGenerator)

