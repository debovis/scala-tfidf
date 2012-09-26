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
import scala.io.Source
import cc.spray.http.HttpMethods._

import java.util.concurrent.TimeUnit.SECONDS

import com.sparcedge.analytics.indexers.matrix.TfIdfGenerator
import com.sparcedge.analytics.similarity._
import collection.JavaConversions._

import com.sparcedge.analytics.mongodb.MongoCollectionWrapper

trait AnalyticsService extends Directives {
  
  	var simActors = List[ActorRef]()
  	for(i <- 1 to 50) {
  		simActors = Actor.actorOf[SimilarityHandler].start() :: simActors
  	}
	val simLoadBalancer = Routing.loadBalancerActor(new CyclicIterator(simActors))
	var configMap: Map[String,String]
	
	def similarityDatabase: SimilarityElementDatabase
	def tfIdfManager: ActorRef
	val demoHtml = Source.fromURL(getClass.getResource("/similarityDemo.html")).mkString
	
	val analyticsService = {
		pathPrefix("static") {
			cache {
				getFromResourceDirectory("static")
			}
		} ~
		(pathPrefix("similarity") & parameter("key")) { key =>
			get {
				parameter("q") { query => ctx: RequestContext =>
					simLoadBalancer ! similarityRequest(configMap,query, ctx, tfIdfManager)
				}
			} ~
			path (IntNumber) { id =>
				put {
					formFields("document")  { content => ctx: RequestContext =>
					  // Spray not filtering correctly
					  var httpMethod = ctx.request.method.toString()
					  if(httpMethod == "PUT"){
						tfIdfManager ! AddElement(TfIdfElement(id, "",content,null,false,true,false),key)
						var responseText = "{\"created\": true}"
						ctx.complete{
						  HttpResponse(status = StatusCodes.OK, headers = Nil, content = HttpContent(`application/json`,responseText))
						}
					  }
					}
				} ~
				delete { ctx: RequestContext =>
				  	// Spray not filtering correctly
					var httpMethod = ctx.request.method.toString()
					if(httpMethod == "DELETE"){
						tfIdfManager ! RemoveElement(id,key)
						var responseText = "{\"deleted\": true}"
						ctx.complete{
						  HttpResponse(status = StatusCodes.OK, headers = Nil, content = HttpContent(`application/json`,responseText))
						}
					}
				}
			}
		}~
		path("similarityDemo") {
			get { ctx: RequestContext =>
			 var production = configMap.get("production-environment").get.toBoolean
			 if(!production){
				completeCtx(ctx,demoHtml)
			 }
			 else{
			   completeCtx(ctx,"Not available in production")
			 }
		  }
		}
	}
	def completeCtx(ctx: RequestContext,text: String) = {
	  ctx.complete(   HttpResponse(status = StatusCodes.OK, headers = Nil, content = HttpContent(`text/html`,text)))
	}
}

