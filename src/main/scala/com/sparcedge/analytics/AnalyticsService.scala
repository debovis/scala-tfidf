package com.sparcedge.analytics

import cc.spray._
import cc.spray.http._
import cc.spray.http.MediaTypes._
import cc.spray.http.HttpHeaders.{`Cache-Control`, `Connection`}
import cc.spray.http.CacheDirectives.`no-cache`
import cc.spray.directives.{IntNumber,Remaining}
import akka.actor.{PoisonPill, Actor, Scheduler, ActorRef}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.collection.mutable.Map

trait AnalyticsService extends Directives {

	val analyticsService = {
		path("fibs" / IntNumber) { num =>
			get { ctx: RequestContext =>
				val fibActor = Actor.actorOf[FibonacciHandler].start
				fibActor ! FibonacciRequest(num, ctx)
			}
		}~
		path("ping") {
      		content(as[Option[String]]) { body =>
        		completeWith("PONG! " + body.getOrElse(""))
      		}
    	}~
    	path("pong") {
      		(get | post) { ctx:RequestContext => 
      		  val content = ctx.request.content
      		  val headers = Map[String, String]()
      		  ctx.request.headers.foreach(x => 
      		    headers+=(x.name -> x.value)
      		    )
      			ctx.complete(
	      			HttpResponse (
						status = StatusCodes.OK,
						headers = Nil,
						content = HttpContent(
								`application/json`,
								compact(render("headers" -> content.as[String].right.getOrElse(headers("user-agent"))) )
						)))
      		}
    	}~
    	path("lucene") { 
			post { ctx: RequestContext =>
			  val data = ctx.request.content.as[String].right.get toString
			  
			  try{
			    val requestData = Some(parse(data))
			    val LuceneActor = Actor.actorOf[LuceneHandler].start
				LuceneActor ! LuceneRequest(requestData, ctx)
				
				
			  }
			  catch{
			    case e:net.liftweb.json.JsonParser.ParseException => ctx.complete(
			    		HttpResponse (
							status = StatusCodes.BadRequest,
							headers = Nil,
							content = HttpContent(
									`application/json`,
									compact(render("error" -> "Problem parsing dataset")) )
						))
			  }
			}
		}
	}	
}


