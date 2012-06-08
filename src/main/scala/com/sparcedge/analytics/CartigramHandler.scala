package com.sparcedge.analytics

import akka.actor.Actor
import cc.spray.RequestContext
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes}
import cc.spray.http.MediaTypes._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import org.joda.time._
import org.joda.time.format._
import org.joda

import scala.util.Random
import java.util.LinkedHashMap
import org.apache.commons.lang.StringUtils
import java.io.StringReader
import java.util.Date
import java.text.SimpleDateFormat
import java.util.LinkedHashMap

import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.query._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.{MongoDBObject,ObjectId,BasicDBList}

import org.apache.lucene.util._
import org.apache.lucene.analysis._
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper
import org.apache.lucene.analysis.tokenattributes._

import com.sparcedge.analytics.mongodb.MongoCollectionWrapper
import com.sparcedge.analytics.indexers.matrix.TfGenerator
import com.sparcedge.analytics.indexers.matrix.IdfIndexer
import com.sparcedge.analytics.similarity.matrix.CosineSimilarity
import com.sparcedge.analytics.tokenizers.NGramAnalyzer

class CartigramHandler extends Actor{
	implicit val formats = Serialization.formats(NoTypeHints)

	def receive = {
		case cartRequest (requestData, ctx) =>

		//  if they give us a date, lets use it otherwise now
		var requestedDate = Some("")
		val fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
		//var date1 = new DateTime()
		var date1 = fmt.parseDateTime("04/09/2012")
		if(requestData.get("date") != None){
			requestedDate = Some(requestData.get("date").get)
			//date1 = fmt.parseDateTime(requestedDate.get)
		}
		
		var date2 = date1.plusDays(7) //$lt date2.getMillis() 
		
		// 1334781223000 
		// 1346731200000
		//println("%d %d".format(date1.getMillis(), date2.getMillis()))
		//val q:MongoDBObject =  ("ts" $gte date1.getMillis() $lt date2.getMillis())// ++ ("r" -> "sparc-bldg-power")
		//val ts = MongoDBObject("ts" -> MongoDBObject("$gte" -> date1.getMillis(), "$lte" -> date2.getMillis()))
		//val ts = MongoDBObject("ts"-> date1.getMillis().toLong)
		
		val documents 			= new LinkedHashMap[String,String]
		val connection = new MongoCollectionWrapper("sparcet-local")
		val collection = connection.getCollection
		var sparcets:List[Sparcet] = List()
		var sparcetMap = new LinkedHashMap[String,String]
		var limit = 1000
		val idfClass 				= new IdfIndexer()
		val cosineSim 			= new CosineSimilarity()
		if(requestData.get("limit") != None) 
			limit = Integer.parseInt(requestData.get("limit").get)
			
		val randomGen = Random
		
		var i=0
		collection.find().limit(limit).foreach{ u =>
			val obj = u.asDBObject
			var labels:List[Any] = List()
			if(obj.get("Labels") != null){
			   labels = obj.as[BasicDBList]("Labels").toList
			}
			var reason = ""
			var thanks = ""
			if(!StringUtils.isEmpty(obj.get("PersonalThanks").toString())){
			  reason = obj.get("PersonalThanks").toString()
			}
			if(!StringUtils.isEmpty(obj.get("Reason").toString())){
			  thanks = obj.get("Reason").toString()
			}
			
			//documents.put("doc%s".format(randomGen.nextInt(100000)),reason + " " + thanks)
			
			// analyze spracts with shingleAnalyzer
			val scet =  new Sparcet(reason, thanks,labels)
//			sparcets = scet :: sparcets
//			
//			val ngram = new ShingleAnalyzerWrapper(Version.LUCENE_35,2,3)
//			val tokenStream = ngram.tokenStream("content", scet.toStringReader())
			println(scet.toStringtest())
//			val offsetAttribute = tokenStream.addAttribute(classOf[OffsetAttribute]);
//			val charTermAttribute = tokenStream.addAttribute(classOf[CharTermAttribute])
//			while (tokenStream.incrementToken()){
//				println(charTermAttribute.toString()) //.replaceAll(" _ ",""))
//			}
			
			// TODO: use TFGenerator and try own wordngram analysis 
			sparcetMap.put("doc%d".format(i),scet.toString())
			i+=1
		}
		connection.getConnection.close()
		
		val res = TfGenerator.generateMatrix(sparcetMap)
		val idfRes = idfClass.transform(res);
		val comparisonVect = idfRes.getColumnVector(idfRes.getColumnDimension()-1);
		var idfSubMatrix = idfRes.getSubMatrix(0,idfRes.getRowDimension()-1, 0, idfRes.getColumnDimension()-2);
		
		var simList:List[similarityResult] = List()
		
//		var randomInt = randomGen.nextInt()
//		def testRandomInt { while(true) {if(randomInt < idfRes.getColumnDimension()) return else randomInt = randomGen.nextInt() }}
		
		  val keys = documents.keySet().toArray().toList
		  val howSimilar = cosineSim.similarity(idfSubMatrix,comparisonVect).toArray().toList
		  
		  // zip lists to return
		  keys.zip(howSimilar).foreach{x => 
		    if(x._2.toDouble >0){
		    	simList = new similarityResult(x._1.toString(), x._2.toDouble) :: simList
		    }
		  }
		  // Sort it
		  simList = simList.sortWith(_.similarity > _.similarity)
			  
		
		
//		val idfClass 				= new IdfIndexer()
//		val cosineSim 			= new CosineSimilarity()
//		val tfMatrix = TfGenerator.generateMatrix(documents)
//		val idfResult = idfClass.transform(tfMatrix)
//		println("rows - %s columns - %s".format(idfResult.getRowDimension(), idfResult.getColumnDimension()))

		ctx.complete(
				HttpResponse (
						status = StatusCodes.OK,
						headers = Nil,
						content = HttpContent(
								`application/json`,
								compact(render( "similarity" -> simList.map {w => ("title" -> w.title) ~ ("similarity" -> w.similarity)}))
								)))

		case _ =>
	}
}

case class cartRequest(requestData: Map[String,String], ctx: RequestContext)

case class Event (
		domain: String,
		timestamp: Long,
		category: String,
		tenant: String,
		resource: Option[String] = None,
		data: Option[JObject] = None
		)
		
case class Sparcet(
		reason: String,
		personalThanks: String,
		labels: List[Any]
){
  def toStringtest():String = {
    "reason - %s, personalThanks - %s, labels - %s".format(reason,personalThanks,labels)
  }
  override def toString():String = {
    "%s %s".format(reason,personalThanks)
  }
  def toStringReader():StringReader = {
    new StringReader(toString)
  }
}

		//	private val defaultClientMap:Map[String,String] = Map[String,String]("t" -> "va", "d" -> "sparc","private_key" -> scala.io.Source.fromFile("keys/analytics_rsa_private.pem").mkString)
		//
		//	private def getClient = {
		//	  val clientMap = defaultClientMap
		//	  new SparcPlatformClient("core.dev.sparcloud.net", "3000",clientMap.get("d").get, clientMap.get("private_key").get,Some(clientMap.get("t").get))
		//	}
