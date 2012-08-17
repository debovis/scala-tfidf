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
	Scheduler.schedule(tfIdfManager, UpdateTfIdfCollection(), 60, 120, SECONDS)

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
		}~
		path("similarityDemo") {
			get { ctx: RequestContext =>
				ctx.complete (
					HttpResponse(status = StatusCodes.OK, headers = Nil, content = HttpContent(`text/html`,demoHtml))
				)
		  }
		}
	}
}

