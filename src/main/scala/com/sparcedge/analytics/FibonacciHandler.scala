package com.sparcedge.analytics

import akka.actor.Actor
import cc.spray.RequestContext
import cc.spray.http.{HttpResponse,HttpContent,StatusCodes}
import cc.spray.http.MediaTypes._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class FibonacciHandler extends Actor {
	def receive = {
		case FibonacciRequest(num, ctx) =>
			val fibs = calcFibs(num)
			ctx.complete(
				HttpResponse (
					status = StatusCodes.OK,
					headers = Nil,
					content = HttpContent(
							`application/json`,
							compact(render("fibs" -> fibs))
					)
				)
			)
		case _ =>
	}

	def calcFibs(num: Int): List[Int] = {
		fibFrom(1,1).take(num).toList
	}

	def fibFrom(a: Int, b: Int): Stream[Int] = a #:: fibFrom(b, a + b)
}

case class FibonacciRequest(num: Int, ctx: RequestContext)