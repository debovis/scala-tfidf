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
	
	val analyticsService = {
		pathPrefix("static") {
			cache {
				getFromResourceDirectory("static")
			}
		} ~
		pathPrefix("similarity") {
			get {
				parameter("q") { query => ctx: RequestContext =>
					var key = getUsername(ctx.request.headers)
					var authenticated = authenticateBasicAuth(ctx.request.headers)
					if(authenticated && key != "" ){
						simLoadBalancer ! similarityRequest(configMap,query, ctx, tfIdfManager)
					}
					else completeUnauthorizedResponse(ctx)
				}
			} ~
			path (IntNumber) { id => 
				put {
					formFields("document")  { content => ctx: RequestContext =>
						var key = getUsername(ctx.request.headers)
						var httpMethod = ctx.request.method.toString()
						if(httpMethod == "PUT"){
							var authenticated = authenticateBasicAuth(ctx.request.headers)
							if( authenticated && key != "" ){
								tfIdfManager ! AddElement(TfIdfElement(id, "",content,null,false,true,false),key)
								completeCtxJson(ctx,"{\"created\": true}")
							} else completeUnauthorizedResponse(ctx)
						}
					}		    	
				} ~
				delete { ctx: RequestContext =>
					var key = getUsername(ctx.request.headers)
					var httpMethod = ctx.request.method.toString()
					if(httpMethod == "DELETE"){
						var authenticated = authenticateBasicAuth(ctx.request.headers)
						if( authenticated && key != "" ){
							tfIdfManager ! RemoveElement(id,key)
							completeCtxJson(ctx,"{\"deleted\": true}")
						}
						else completeUnauthorizedResponse(ctx)
				  }	
				}
			}
		}~
		path("compare") {
		  get { 
			parameters("doc1","doc2") { (doc1,doc2) => ctx: RequestContext =>
				if( authenticateBasicAuth(ctx.request.headers) ){
					  simLoadBalancer ! twoStringSimilarityRequest(doc1,doc2, ctx, tfIdfManager,configMap)
				}
				else completeUnauthorizedResponse(ctx)
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
	  val response = HttpResponse(status = StatusCodes.Unauthorized, headers = Nil, content = HttpContent(`application/json`,responseText))
	  ctx.complete(response )
	}
	def requestHeaderDecoded(authorizationHeader: String): String = {
	  var byteArray = Base64.decodeBase64(authorizationHeader.getBytes())
	  new String((byteArray).map(_.toChar))
	}
	def authenticateBasicAuth(headers: List[cc.spray.http.HttpHeader]) = {
		var authenticated = false
		try{
			val authHeaderValueEncoded = headers.filter(_.name == "Authorization")(0).value.split(" ")(1)
			val authHeaderValueDecoded = requestHeaderDecoded(authHeaderValueEncoded)
			
			val username = authHeaderValueDecoded.split(":")(0)

			// get the bcrypt value on file for the username passed in auth header
			val bcryptValue = tenantKeyMap.get(username).get.asInstanceOf[String]
			
			if(new Password(authHeaderValueDecoded).isBcrypted(bcryptValue)){
				authenticated = true
			} else authenticated = false
	
		} catch {
			  case e => 
				authenticated = false
		}
		
		authenticated
	}

	def getUsername(headers: List[cc.spray.http.HttpHeader]): String = {
	  var username = ""
	  try {
			val authHeaderValueEncoded = new String(headers.filter(_.name == "Authorization")(0).value).split(" ")(1)
			username = requestHeaderDecoded(authHeaderValueEncoded).split(":").head

	  } catch {
			case e =>
				println(e.toString)
		}
		username
	}
}



