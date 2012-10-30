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
import org.apache.commons.codec.binary.Base64
import ua.t3hnar.bcrypt._

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
	val compareHtml = Source.fromURL(getClass.getResource("/twoStringComparison.html")).mkString

	val acceptedTenantJson = Source.fromURL(getClass.getResource("/acceptedTenants.json")).mkString	
	var tenantKeyMap = (parse(acceptedTenantJson) \ "tenants").asInstanceOf[JObject].values
	val sparcinKey = tenantKeyMap.get("sparcin").get.asInstanceOf[String]
	
	val analyticsService = {
		pathPrefix("static") {
			cache {
				getFromResourceDirectory("static")
			}
		} ~
		(pathPrefix("similarity") & parameter("key")) { key => 
			get {
				parameter("q") { query => ctx: RequestContext =>
				  	var authenticated = authenticateBasicAuth(ctx.request.headers)
				  	if(authenticated){
				  		simLoadBalancer ! similarityRequest(configMap,query, ctx, tfIdfManager)
				  	}
				  	else completeUnauthorizedResponse(ctx)
				}
			} ~
			path (IntNumber) { id => 
				put {
					formFields("document")  { content => ctx: RequestContext =>
					  var httpMethod = ctx.request.method.toString()
					  if(httpMethod == "PUT"){
					    if( authenticateBasicAuth(ctx.request.headers) ){
						    tfIdfManager ! AddElement(TfIdfElement(id, "",content,null,false,true,false),key)
							completeCtxJson(ctx,"{\"created\": true}")
					    }
					    else completeUnauthorizedResponse(ctx)
					  }
					}		    	
				} ~
				delete { ctx: RequestContext =>
					var httpMethod = ctx.request.method.toString()
					if(httpMethod == "DELETE"){
						// authenticated?
						if( authenticateBasicAuth(ctx.request.headers) ){
							tfIdfManager ! RemoveElement(id,key)
							completeCtxJson(ctx,"{\"deleted\": true}")
						}
						else completeUnauthorizedResponse(ctx)
				  }	
				}
			}
		}~
		path("compare") {
		  get { ctx: RequestContext =>
		  	if( authenticateBasicAuth(ctx.request.headers) ){
			    parameters("doc1","doc2") { (doc1,doc2) => ctx:RequestContext =>
			      simLoadBalancer ! twoStringSimilarityRequest(doc1,doc2, ctx, tfIdfManager,configMap)
			    }
			}
		  }
		}~
		path("compareDemo"){
		  get { ctx:RequestContext =>
		    var production = configMap.get("production-environment").get.toBoolean
		    if(!production){
		      completeCtxHtml(ctx,compareHtml)
		    }
		    else {
		      completeCtxHtml(ctx,"Not available in production")
		    }
		    
		  }
		}~
		path("similarityDemo") {
			get { ctx: RequestContext =>
			 var production = configMap.get("production-environment").get.toBoolean
			 if(!production){
				completeCtxHtml(ctx,demoHtml)
			 }
			 else{
			   completeCtxHtml(ctx,"Not available in production")
			 }
		  }
		}
	}
	def completeCtxHtml(ctx: RequestContext,text: String) = {
	  ctx.complete(   HttpResponse(status = StatusCodes.OK, headers = Nil, content = HttpContent(`text/html`,text)))
	}
	def completeCtxJson(ctx: RequestContext, text: String) = {
	  ctx.complete(   HttpResponse(status = StatusCodes.OK, headers = Nil, content = HttpContent(`application/json`,text)))
	}
	def completeUnauthorizedResponse(ctx: RequestContext) = {
	  val responseText = "{\"Authorization\": false}"
	  ctx.complete( HttpResponse(status = StatusCodes.Unauthorized, headers = Nil, content = HttpContent(`application/json`,responseText)))
	}
	def requestHeaderDecoded(authorizationHeader: String) = {
	  var byteArray = Base64.decodeBase64(authorizationHeader.getBytes())
	  new String((byteArray).map(_.toChar))
	}
	def authenticateBasicAuth(headers: List[cc.spray.http.HttpHeader]) = {
		var authenticated = false
		try{
			val authHeaderValueEncoded = headers.filter(_.name == "Authorization")(0).value.split(" ")(1)
			val authHeaderValueDecoded = requestHeaderDecoded(authHeaderValueEncoded)
			new Password(authHeaderValueDecoded).isBcrypted(sparcinKey)
			
			if(new Password(authHeaderValueDecoded).isBcrypted(sparcinKey)){
				authenticated = true
			} else authenticated = false
	
		} catch {
		  case e => 
		    authenticated = false
		}
		
		authenticated
	}
}



