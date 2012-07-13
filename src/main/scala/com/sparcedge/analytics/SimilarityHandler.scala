package com.sparcedge.analytics

import akka.actor.Actor
import cc.spray.RequestContext
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes,HttpHeader,StatusCode}
import cc.spray.http.MediaTypes._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.util.LinkedHashMap

import org.slf4j.{Logger, LoggerFactory}

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;

import com.sparcedge.analytics.indexers.matrix.TfGenerator
import com.sparcedge.analytics.indexers.matrix.IdfIndexer
import com.sparcedge.analytics.similarity.matrix.CosineSimilarity

import com.sparcedge.analytics.similitarycollector._


class SimilarityHandler extends Actor {
	implicit val formats = Serialization.formats(NoTypeHints)
	val log = LoggerFactory.getLogger(getClass)
  
	def receive = {
		case similarityRequest	(requestData, ctx,tfMatrix) =>
		  
		  try{
			  val comp = (requestData.get \ "data" \ "document").extract[dataSet]
			  
			  val comparisonWordSet = TfGenerator.getWordFrequencies(comp.value)
			  val comparisonWordKeySet = comparisonWordSet.uniqueSet()
			  val comparisonVect = new ArrayRealVector(tfMatrix.words.size)

			  var i=0
			  for(word <- tfMatrix.words){
			    if(comparisonWordKeySet.contains(word.toString())){
			      var corpusWordFreq = tfMatrix.corpusWordOccurenceVect.getEntry(i)
			      val documentsCount = tfMatrix.tfMatrix.getColumnDimension()
			      val comparisonWordCount = comparisonWordSet.getCount(word.toString())
			      val finalTfIdf = (comparisonWordCount) * (1 + Math.log(documentsCount) - Math.log(corpusWordFreq))
			      comparisonVect.setEntry(i,finalTfIdf)
			    }
			    i+=1
			  }

			  val cosineSimilarity = new CosineSimilarity()
			  val similarityVect = cosineSimilarity.similarity(tfMatrix.tfIdfMatrix,comparisonVect).toArray().toList
			  val keys = tfMatrix.documents.keySet().toArray().toList
			  var simList:List[similarityResult] = List()
			  
			  // zip lists to return
			  keys.zip(similarityVect).foreach{x => 
			    if(x._2.toDouble >0.3){
			    	simList = new similarityResult(x._1.toString(), x._2.toDouble,tfMatrix.documents.get(x._1.toString())) :: simList
			    }
			  }
			  // Sort it
			  simList = simList.sortWith(_.similarity > _.similarity)
			  
			  ctx.complete(
			      response(StatusCodes.OK, compact(render("similarity" -> simList.map {w => ("title" -> w.title) ~ ("similarityScore" -> w.similarity) ~ ("document" -> w.document)})))
			      )
			  
		  } catch {
		    		case e:Exception => 
		    		  	println(e.toString())
		    		  	ctx.complete(
		    		  	    response(StatusCodes.OK, compact(render("similarity" -> e.toString())))
		    		  	   )
		  }
		case _ =>
	}
	def response(status:StatusCode, jsonResponse: String): HttpResponse = {
			HttpResponse (
								status = status,
								headers = Nil,
								content = HttpContent(
										`application/json`,jsonResponse
						))
		}
}

class dataSet(
		val title : String,
		val value : String
)

class similarityResult(
		val title: String,
		val similarity : Double,
		val document : String
)

case class similarityRequest(requestData:Some[net.liftweb.json.JsonAST.JValue], ctx: RequestContext, tfMatrix:cachedTF)

