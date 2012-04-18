package com.sparcedge.analytics

import cc.spray._
import cc.spray.http._
import cc.spray.http.MediaTypes._
import cc.spray.http.HttpHeaders.{`Cache-Control`, `Connection`}
import cc.spray.http.CacheDirectives.`no-cache`
import cc.spray.directives.{IntNumber,Remaining}
import akka.actor.{PoisonPill, Actor, Scheduler, ActorRef}

trait AnalyticsService extends Directives {

	val analyticsService = {
		path("fibs" / IntNumber) { num =>
			get { ctx: RequestContext =>
				val fibActor = Actor.actorOf[FibonacciHandler].start
				fibActor ! FibonacciRequest(num, ctx)
			}
		}
	}	
}