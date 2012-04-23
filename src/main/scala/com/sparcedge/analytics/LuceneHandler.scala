package com.sparcedge.analytics

import akka.actor.Actor
import cc.spray.RequestContext
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes}
import cc.spray.http.MediaTypes._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class LuceneHandler extends Actor {
	def receive = {
		case LuceneRequest(requestData, ctx) =>
		  val jsonified = compact(render(requestData.get))
		  
			ctx.complete(
				HttpResponse (
					status = StatusCodes.OK,
					headers = Nil,
					content = HttpContent(
							`application/json`,
							compact(render("lucene" -> jsonified))
					)))
		case _ =>
	}
}

case class LuceneRequest(requestData:Some[net.liftweb.json.JsonAST.JValue], ctx: RequestContext)