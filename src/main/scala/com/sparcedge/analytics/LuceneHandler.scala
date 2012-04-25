package com.sparcedge.analytics

import akka.actor.Actor
import cc.spray.RequestContext
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes}
import cc.spray.http.MediaTypes._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.util.HashMap;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.sparcedge.analytics.indexers.matrix.TfGenerator
import com.sparcedge.analytics.indexers.matrix.IdfIndexer
import com.sparcedge.analytics.similarity.matrix.CosineSimilarity

class LuceneHandler extends Actor {
	implicit val formats = Serialization.formats(NoTypeHints)
  
	def receive = {
		case LuceneRequest(requestData, ctx) =>
		  val documents = new HashMap[String,String];
		  val idfClass = new IdfIndexer();
		  val cosineSim = new CosineSimilarity();
		  
		  (requestData.get \ "data" \ "data_set").extract[List[dataSet]].foreach(d => documents.put(d.title, d.value))
		  val res:RealMatrix = TfGenerator.generateMatrix(documents)
		  val idfRes:RealMatrix = idfClass.transform(res)
		  val howSimilar = cosineSim.computeSimilarity(idfRes.getColumnMatrix(0),idfRes.getColumnMatrix(1))
		  
			ctx.complete(
				HttpResponse (
					status = StatusCodes.OK,
					headers = Nil,
					content = HttpContent(
							`application/json`,
							compact(render("similarity" -> howSimilar))
					)))
		case _ =>
	}
}

class dataSet(
		val title : String,
		val value : String
)

case class LuceneRequest(requestData:Some[net.liftweb.json.JsonAST.JValue], ctx: RequestContext)


// {"data":{"data_set":[{"title":"doc1","value":"once upon a time"},{"title":"doc2","value":"true life"}]}}

//{'data':
//		{'data_set': [
//				{'key1': 'string1' },
//				{'key2': 'string2' }
//			]
//		},
//		{'comparision': 'string'}
//	}

