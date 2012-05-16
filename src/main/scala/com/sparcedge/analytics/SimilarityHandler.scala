package com.sparcedge.analytics

import akka.actor.Actor
import cc.spray.RequestContext
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes,HttpHeader}
import cc.spray.http.MediaTypes._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.util.LinkedHashMap;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.sparcedge.analytics.indexers.matrix.TfGenerator
import com.sparcedge.analytics.indexers.matrix.IdfIndexer
import com.sparcedge.analytics.similarity.matrix.CosineSimilarity


class SimilarityHandler extends Actor {
	implicit val formats = Serialization.formats(NoTypeHints)
  
	def receive = {
		case similarityRequest	(requestData, ctx) =>
		  
		  try{
		  
			  val documents 			= new LinkedHashMap[String,String]
			  val comparisonDocument 	= new LinkedHashMap[String,String]
			  var comparisonDocumentTitle = new String
			  val idfClass 				= new IdfIndexer()
			  val cosineSim 			= new CosineSimilarity()
			  
			  // Extract documents and put into hashmap
			  (requestData.get \ "data" \ "data_set").extract[List[dataSet]].foreach(d => documents.put(d.title, d.value) )
			  
			  // Comparision document
			  val comp = (requestData.get \ "data" \ "comparison_document").extract[dataSet] 
			  comparisonDocumentTitle = comp.title
			  documents.put(comp.title, comp.value)
			  comparisonDocument.put(comp.title, comp.value)
			  
			  // Generate TF matrix, IDF it, and how similar it is?
			  val res:RealMatrix = TfGenerator.generateMatrix(documents)
			  val idfRes:RealMatrix = idfClass.transform(res);
			  val comparisonVect = idfRes.getColumnVector(idfRes.getColumnDimension()-1);
			  var idfSubMatrix = idfRes.getSubMatrix(0,idfRes.getRowDimension()-1, 0, idfRes.getColumnDimension()-2);
			  
			  // Debug
			  //System.out.println("before: " + idfRes.toString() + "\n after: " + idfSubMatrix.toString() + "\n comparison vect " + comparisonVect.toString());
			 
			  // remove comparision document from document hashmap, needed to to be in set for IDF and TFGeneration
			  comparisonDocument.remove(comparisonDocumentTitle)
			  var simList:List[similarityResult] = List()
			  
			  val keys = documents.keySet().toArray().toList
			  val howSimilar = cosineSim.similarity(idfSubMatrix,comparisonVect).toArray().toList
			  // zip lists to return
			  keys.zip(howSimilar).foreach{x => 
			    if(x._2.toDouble >0){
			    	simList = new similarityResult(x._1.toString(), x._2.toDouble) :: simList
			    }
			  }
			  
				ctx.complete(
					HttpResponse (
						status = StatusCodes.OK,
						headers = List(HttpHeader("Connection", "Keep-Alive")),
						content = HttpContent(
								`application/json`,
								compact(render("similarity" -> simList.map {w => ("title" -> w.title) ~ ("similarity" -> w.similarity)}))
						)))
		  } catch {
		    		case e:Exception => println(e.toString())
		  }
		case _ =>
	}
}

class dataSet(
		val title : String,
		val value : String
)

class similarityResult(
		val title: String,
		val similarity : Double
)

case class similarityRequest(requestData:Some[net.liftweb.json.JsonAST.JValue], ctx: RequestContext)

