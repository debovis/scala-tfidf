package com.sparcedge.analytics

import akka.config.Supervision._
import akka.actor.{Supervisor, Actor}
import Actor._
import cc.spray._
import cc.spray.can.HttpServer
import org.slf4j.LoggerFactory

object Boot extends App {
	
	LoggerFactory.getLogger(getClass) // initialize SLF4J early

	val mainModule = new AnalyticsService {}

	val httpService    = actorOf(new HttpService(mainModule.analyticsService))
	val rootService    = actorOf(new SprayCanRootService(httpService))
	val sprayCanServer = actorOf(new HttpServer())

	Supervisor(
		SupervisorConfig(
			OneForOneStrategy(List(classOf[Exception]), 3, 100),
			List(
				Supervise(httpService, Permanent),
				Supervise(rootService, Permanent),
				Supervise(sprayCanServer, Permanent)
			)
		)
	)
}