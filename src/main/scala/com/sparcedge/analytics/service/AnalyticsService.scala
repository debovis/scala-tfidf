package com.sparcedge.analytics

import cc.spray._
import cc.spray.http._
import cc.spray.http.MediaTypes._
import cc.spray.http.HttpHeaders.{`Cache-Control`, `Connection`}
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes,HttpHeader}
import cc.spray.http.CacheDirectives.`no-cache`
import cc.spray.directives.{IntNumber,Remaining}
import akka.actor.{PoisonPill, Actor, Scheduler, ActorRef}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.collection.mutable.Map

import com.sparcedge.analytics.similitarycollector._

trait AnalyticsService extends Directives {
  
	val coll = new collectionCollector
	var questions = coll.getQuestions
	
	val tf = new cachedTF(questions)

	val analyticsService = {
		path("fibs" / IntNumber) { num =>
			get { ctx: RequestContext =>
				val fibActor = Actor.actorOf[FibonacciHandler].start
				fibActor ! FibonacciRequest(num, ctx)
			}
		}~
		path("ping") {
      		content(as[Option[String]]) { body =>
        		//completeWith("PONG! " + body.getOrElse(""))
        		completeWith("size " + tf.words.size.toString)
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
      		    		response(StatusCodes.OK,compact(render("headers" -> content.as[String].right.getOrElse(headers("user-agent"))) ))
      		    	)
      		}
    	}~
    	path("similarity") { 
			post { jsonpWithParameter("callback") { ctx: RequestContext =>
			  val data = ctx.request.content.as[String].right.get toString
			  
			  try{
			    val requestData = Some(parse(data))
			    val SimActor = Actor.actorOf[SimilarityHandler].start
				SimActor ! similarityRequest(requestData, ctx, tf)
				
				
			  }
			  catch{
			    case e:net.liftweb.json.JsonParser.ParseException => 
			      println(e)
			      ctx.complete(
			    		  response(StatusCodes.BadRequest,compact(render("error" -> "Problem parsing dataset")))
			    		  )
			  }
			  }
			}
		}~
		path("cartigram") {
		  get { jsonpWithParameter("callback") { ctx: RequestContext =>
		    val query = ctx.request.queryParams
		  
		    try{
		      val cartActor = Actor.actorOf[CartigramHandler].start
		      cartActor ! cartRequest(query,ctx)
		    }
		    catch{
		      case e:Exception => 
		        ctx.complete(
		            response(StatusCodes.BadRequest,compact(render("error" -> "Having a bad day?")))
		         )
		    }}
		  }
		}~
		path("ner") {
		  post { jsonpWithParameter("callback") { ctx : RequestContext => 
		    val data = ctx.request.content.as[String].right.get toString
			  
			  try{
			    // Is it JSON?
			    val requestData = Some(parse(data))
			    val NERActor = Actor.actorOf[NERHandler].start
				NERActor ! NerRequest(requestData, ctx)
				
			  }
		     // Not valid JSON
			  catch{
			    case e:net.liftweb.json.JsonParser.ParseException => 
			      println(e)
			      ctx.complete(
			          response(StatusCodes.BadRequest,compact(render("error" -> "Problem parsing dataset")))
			      )
			  }
		    }
		  }
		}
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


