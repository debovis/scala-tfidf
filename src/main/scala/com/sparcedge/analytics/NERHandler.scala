package com.sparcedge.analytics

import akka.actor.Actor
import cc.spray.RequestContext
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes,StatusCode,HttpHeader}
import cc.spray.http.MediaTypes._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.util.LinkedHashMap

import org.apache.commons.collections15.Bag
import org.apache.commons.collections15.bag.HashBag

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.sparcedge.analytics.nerextractor.NERGenerator

class NERHandler extends Actor{

	implicit val formats = Serialization.formats(NoTypeHints)

		def receive = {
				case NerRequest (requestData, ctx) =>
				  
					val documents 	= new LinkedHashMap[String,String]
					var nerList:List[nerResult] = List()
				  
					try {
				  
						// Extract documents and put into hashmap
						(requestData.get \ "data").extract[List[dataSet]].foreach(d => documents.put(d.title, d.value))
						
						var ner = NERGenerator.getStanfordNlpNerClassifier()
						val bag = new HashBag[String]
						for(i <- documents.keySet().toArray.toList){
						  bag.addAll(ner.findEntities(documents.get(i)))
						}
						
						bag.uniqueSet().toArray().toList.foreach{ u=>
						  nerList = new nerResult(u.toString(), bag.getCount(u.toString())) :: nerList
						}		
					  ctx.complete(response(StatusCodes.OK,compact(render( "NamedEntities" -> nerList.map{ e => ("word" -> e.word) ~ ("freq" -> e.freq) }))))
									
				  }
				  catch{
		    		case e:Exception => 
		    		  	println(e.toString())
		    		  	ctx.complete(response(StatusCodes.OK, compact(render("similarity_error" -> e.toString()))))
				  }
			case _ =>
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

case class NerRequest(requestData:Some[net.liftweb.json.JsonAST.JValue], ctx: RequestContext)

class nerResult(
		val word: String,
		val freq : Int
)