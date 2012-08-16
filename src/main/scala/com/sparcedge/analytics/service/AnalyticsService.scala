package com.sparcedge.analytics

import cc.spray._
import cc.spray.http._
import cc.spray.http.MediaTypes._
import cc.spray.http.HttpHeaders.{`Cache-Control`, `Connection`}
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes,HttpHeader}
import cc.spray.http.CacheDirectives.`no-cache`
import cc.spray.directives.{IntNumber,Remaining,JavaUUID}
import akka.actor.{PoisonPill, Actor, Scheduler, ActorRef}
import akka.routing.{CyclicIterator,Routing}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.collection.mutable.Map
import scala.io.Source

import java.util.concurrent.TimeUnit.SECONDS

import com.sparcedge.analytics.indexers.matrix.TfIdfGenerator
import com.sparcedge.analytics.similarity._

trait AnalyticsService extends Directives {
  
  	var simActors = List[ActorRef]()
  	for(i <- 1 to 50) {
  		simActors = Actor.actorOf[SimilarityHandler].start() :: simActors
  	}
	val simLoadBalancer = Routing.loadBalancerActor(new CyclicIterator(simActors))

	val elements = SimilarityElementDatabase.retrieveTextElements("sparcin")
	val tfIdfManager = Actor.actorOf(new TfIdfCollectionManager(elements)).start
	Scheduler.schedule(tfIdfManager, UpdateTfIdfCollection(), 60, 60, SECONDS)

	val demoHtml = Source.fromURL(getClass.getResource("/similarityDemo.html")).mkString
	
	val analyticsService = {
		pathPrefix("static") {
			cache {
				getFromResourceDirectory("static")
			}
		} ~
		(pathPrefix("similarity") & parameter("apiKey")) { apiKey =>
			get {
				parameter("q") { query => ctx: RequestContext =>
					simLoadBalancer ! similarityRequest(query, ctx, tfIdfManager)
				}
			} ~
			path (IntNumber) { id =>
				put {
					content(as[String]) { content =>
						//Insert Mongo
						tfIdfManager ! AddElement(TfIdfElement(id, content))
						completeWith {
							"{\"created\": \"true\"}"
						}
					}
				} ~
				delete {
					// Delete Mongo
					tfIdfManager ! RemoveElement(id)
					completeWith {
						"{\"deleted\": \"true\"}"
					}
				}
			}
		}

		/* ~
		path("similarityDemo") {
			get { ctx: RequestContext =>
				ctx.complete (
					HttpResponse(status = StatusCodes.OK, headers = Nil, content = HttpContent(`text/html`,demoHtml))
				)
		  }
		} ~
		path("cartigram") {
			get { jsonpWithParameter("callback") { ctx: RequestContext =>
				val query = ctx.request.queryParams
		  
				try {
					val cartActor = Actor.actorOf[CartigramHandler].start
					cartActor ! cartRequest(query,ctx)
				} catch {
					case e: Exception => 
						ctx.complete(
							response(StatusCodes.BadRequest, compact(render("error" -> "Having a bad day?")))
						)
				}
			}}
		} ~
		path("ner") {
			post { jsonpWithParameter("callback") { ctx : RequestContext => 
				val data = ctx.request.content.as[String].right.get toString
			  
				try {
					// Is it JSON?
					val requestData = Some(parse(data))
					val NERActor = Actor.actorOf[NERHandler].start
					NERActor ! NerRequest(requestData, ctx)
				} catch {
					// Not valid JSON
					case e:net.liftweb.json.JsonParser.ParseException => 
					println(e)
					ctx.complete(
						response(StatusCodes.BadRequest, compact(render("error" -> "Problem parsing dataset")))
					)
				}
			}}
		} */
	}
	def response(status:StatusCode, jsonResponse: String): HttpResponse = {
		HttpResponse (
			status = status,
			headers = Nil,
			content = HttpContent(`application/json`, jsonResponse)
		)
	}
}


