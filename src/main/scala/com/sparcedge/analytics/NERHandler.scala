package com.sparcedge.analytics

import akka.actor.Actor
import cc.spray.RequestContext
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes,HttpHeader}
import cc.spray.http.MediaTypes._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.util.LinkedHashMap

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.sparcedge.analytics.nerextractor.NERGenerator
import com.sparcedge.analytics.nerextractor.NERGenerator._

class NERHandler extends Actor{

	implicit val formats = Serialization.formats(NoTypeHints)

		def receive = {
				case NerRequest (requestData, ctx) =>
				  
					val documents 	= new LinkedHashMap[String,String]
					var nerList:List[nerResult] = List()
				  
					try {
				  
						// Extract documents and put into hashmap
						(requestData.get \ "data" \ "data_set").extract[List[dataSet1]].foreach(d => documents.put(d.title, d.value))
						var bag = NERGenerator.generate(NERType.OpenNLP,documents)
						
						bag.uniqueSet().toArray().toList.foreach{ u=>
						  nerList = new nerResult(u.toString(), bag.getCount(u.toString())) :: nerList
						}				
						

					  ctx.complete(
						HttpResponse (
								status = StatusCodes.OK,
								headers = Nil,
								content = HttpContent(
										`application/json`,
										compact(render( "NamedEntities" -> nerList.map{ e => ("word" -> e.word) ~ ("freq" -> e.freq) }))
										)))
									
				  }
				  catch{
		    		case e:Exception => 
		    		  
		    		  	println(e.toString())
		    		  	ctx.complete(
							HttpResponse (
								status = StatusCodes.OK,
								headers = Nil,
								content = HttpContent(
										`application/json`,
										compact(render("similarity_error" -> e.toString()))
						)))
				  }
				  
			// Wildcard case
			case _ =>
		}
	}

case class NerRequest(requestData:Some[net.liftweb.json.JsonAST.JValue], ctx: RequestContext)

class dataSet1(
		val title : String,
		val value : String
)

class nerResult(
		val word: String,
		val freq : Int
)