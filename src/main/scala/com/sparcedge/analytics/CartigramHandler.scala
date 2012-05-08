package com.sparcedge.analytics

import akka.actor.Actor
import cc.spray.RequestContext
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes}
import cc.spray.http.MediaTypes._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import java.util.Date
import java.text.SimpleDateFormat
import org.joda.time._
import org.joda.time.format._
import org.joda

import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.query._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.{MongoDBObject,ObjectId}

import com.sparcedge.analytics.mongodb.MongoCollectionWrapper

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
			
			val q:MongoDBObject =  ("ts" $gte date1.getMillis() $lt date2.getMillis())// ++ ("r" -> "sparc-bldg-power")
			
			val collection = new MongoCollectionWrapper().getCollection
			//val ts = MongoDBObject("ts" -> MongoDBObject("$gte" -> date1.getMillis(), "$lte" -> date2.getMillis()))
			//val ts = MongoDBObject("ts"-> date1.getMillis().toLong)

			val u = for(x <- collection.find(q)) yield println(x)

			println(u.size)

			ctx.complete(
					HttpResponse (
							status = StatusCodes.OK,
							headers = Nil,
							content = HttpContent(
									`application/json`,
									compact(render("similarity" -> ""))
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

		//	private val defaultClientMap:Map[String,String] = Map[String,String]("t" -> "va", "d" -> "sparc","private_key" -> scala.io.Source.fromFile("keys/analytics_rsa_private.pem").mkString)
		//
		//	private def getClient = {
		//	  val clientMap = defaultClientMap
		//	  new SparcPlatformClient("core.dev.sparcloud.net", "3000",clientMap.get("d").get, clientMap.get("private_key").get,Some(clientMap.get("t").get))
		//	}
