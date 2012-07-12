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
			      val finalTfIdf = (tfMatrix.corpusWordOccurenceVect.getEntry(i)) * (1 + Math.log(tfMatrix.tfMatrix.getColumnDimension()) - Math.log(comparisonWordSet.getCount(word.toString())))
			      // matrix.setEntry(i, j, matrix.getEntry(i,j) * (1 + Math.log(n) - Math.log(dm)));
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
			    if(x._2.toDouble >0){
			    	simList = new similarityResult(x._1.toString(), x._2.toDouble,tfMatrix.documents.get(x._1.toString())) :: simList
			    }
			  }
			  // Sort it
			  simList = simList.sortWith(_.similarity > _.similarity)
			  
			  ctx.complete(
			      response(StatusCodes.OK, compact(render("similarity" -> simList.map {w => ("title" -> w.title) ~ ("similarityScore" -> w.similarity) ~ ("document" -> w.document)})))
			      )
			  
			  
//			  val comparisonDocument 	= new LinkedHashMap[String,String]
//			  comparisonDocument.put(comp.title,comp.value)
			  //val res:RealMatrix = TfGenerator.generateMatrix(comparisonDocument)
			  
			  //println("new generated matrix = " + res.getRowDimension() + " " + res.getColumnDimension())
			  
			  
			  
		  
//			  val documents 			= new LinkedHashMap[String,String]
//			  var comparisonDocumentTitle = new String
//			  val idfClass 				= new IdfIndexer()
//			  val cosineSim 			= new CosineSimilarity()
//			  
//			  // Extract documents and put into hashmap
//			  (requestData.get \ "data" \ "data_set").extract[List[dataSet]].foreach(d => documents.put(d.title, d.value) )
//			  
//			  
//			  comparisonDocumentTitle = comp.title
//			  documents.put(comp.title, comp.value)
//			  comparisonDocument.put(comp.title, comp.value)
//			  
//			  // Generate TF matrix, IDF it, and how similar it is?
//			  //val res:RealMatrix = TfGenerator.generateMatrix(documents)
//			  val idfRes:RealMatrix = idfClass.transform(res);
//			  val comparisonVect = idfRes.getColumnVector(idfRes.getColumnDimension()-1);
//			  var idfSubMatrix = idfRes.getSubMatrix(0,idfRes.getRowDimension()-1, 0, idfRes.getColumnDimension()-2);
//			  
//			  // Debug
//			  //System.out.println("before: " + idfRes.toString() + "\n after: " + idfSubMatrix.toString() + "\n comparison vect " + comparisonVect.toString());
//			 
//			  // remove comparision document from document hashmap, needed to to be in set for IDF and TFGeneration
//			  comparisonDocument.remove(comparisonDocumentTitle)
//			  var simList:List[similarityResult] = List()
//			  
//			  val keys = documents.keySet().toArray().toList
//			  val howSimilar = cosineSim.similarity(idfSubMatrix,comparisonVect).toArray().toList
//			  
//			  // zip lists to return
//			  keys.zip(howSimilar).foreach{x => 
//			    if(x._2.toDouble >0){
//			    	simList = new similarityResult(x._1.toString(), x._2.toDouble) :: simList
//			    }
//			  }
//			  // Sort it
//			  simList = simList.sortWith(_.similarity > _.similarity)
//			  
//			  ctx.complete(
//			      response(StatusCodes.OK, compact(render("similarity" -> simList.map {w => ("title" -> w.title) ~ ("similarity" -> w.similarity)})))
//			      )
			  //ctx.complete(response(StatusCodes.OK,"help me please"))
			  
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

