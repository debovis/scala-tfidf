package com.sparcedge.analytics

import akka.config.Supervision._
import akka.actor.{Supervisor, Actor}
import Actor._
import cc.spray._
import cc.spray.can.HttpServer
import org.slf4j.LoggerFactory
import com.sparcedge.analytics.mongodb.MongoCollectionWrapper
import com.sparcedge.analytics.similarity._
import akka.actor.Scheduler
import java.util.concurrent.TimeUnit.SECONDS
import net.liftweb.json._

object Boot extends App {
	
	val log = LoggerFactory.getLogger(getClass)
	
	val configStr = args.headOption.getOrElse {
		throw new Exception("No Configuration Supplied")
	}
	log.debug("config file passed ", configStr)

	val mainModule = new AnalyticsService {
		val connection = new MongoCollectionWrapper(configStr, "sparciq")
		override val similarityDatabase = new SimilarityElementDatabase(connection)
	
		val elements = similarityDatabase.retrieveTextElements("sparcin")
		
		var resourceLocationJValue = parse(scala.io.Source.fromFile(configStr).mkString) \ "resource-location"
		override val resourceLocation =  resourceLocationJValue.asInstanceOf[JString].values.toString()
		
		override val tfIdfManager = Actor.actorOf(new TfIdfCollectionManager(resourceLocation,elements)).start
		Scheduler.schedule(tfIdfManager, UpdateTfIdfCollection(), 60, 120, SECONDS)
	}

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