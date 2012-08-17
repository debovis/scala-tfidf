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
import com.sparcedge.analytics.indexers.matrix.{TfIdfGenerator,IdfIndexer}
import com.sparcedge.analytics.similarity.matrix.CosineSimilarity
import com.sparcedge.analytics.tokenizers.NGramAnalyzer
import com.sparcedge.analytics.nerextractor.AbstractNer.NamedEntityType
import com.sparcedge.analytics.nerextractor.OpenNlpNer

class CartigramHandler extends Actor{
	implicit val formats = Serialization.formats(NoTypeHints)

	def receive = {
		case cartRequest (requestData, ctx) =>
			//  if they give us a date, lets use it otherwise now
			var requestedDate = Some("")
			val fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
			var date1 = fmt.parseDateTime("04/09/2012")
			
			if(requestData.get("date") != None){
				requestedDate = Some(requestData.get("date").get)
			}
			
			var date2 = date1.plusDays(7) //$lt date2.getMillis() 
			
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
			
			var i = 0
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
				
				// analyze spracts with shingleAnalyzer
				val scet =  new Sparcet(reason, thanks,labels)
				// TODO: use TFGenerator and try own wordngram analysis 
				sparcetMap.put("doc%d".format(i),scet.toString())
				i+=1
			}
			connection.getConnection.close()

			val res = new TfIdfGenerator(sparcetMap,true)

			ctx.complete (
				HttpResponse (
					status = StatusCodes.OK,
					headers = Nil,
					content = HttpContent(`application/json`, compact(render( "similarity" -> "sdfdf"))))
				)
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
		
case class Sparcet(reason: String, personalThanks: String, labels: List[Any]){
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
