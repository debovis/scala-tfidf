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
import scala.collection.mutable.HashMap
import collection.JavaConversions._
import scala.collection.breakOut

object Boot extends App {
	
	val log = LoggerFactory.getLogger(getClass)
	var configurationMap:Map[String,String] = Map()
	val configStr = args.headOption.getOrElse {
		throw new Exception("No Configuration Supplied")
	}
	log.debug("config file passed ", configStr)
	
	var configurationJson = (parse(scala.io.Source.fromFile(configStr).mkString) \ "config-values").asInstanceOf[JObject].values
	configurationJson.foreach {
	  	case (key, value) => configurationMap +=  (key -> value.toString()) 
	}

	val mainModule = new AnalyticsService {
		val connection = new MongoCollectionWrapper(configStr, "sparciq")
		override val similarityDatabase = new SimilarityElementDatabase(connection)
		override var configMap = configurationMap
	
		val elements = similarityDatabase.retrieveTextElements("sparcin")
		
		override val tfIdfManager = Actor.actorOf(new TfIdfCollectionManager(elements,configStr,configurationMap)).start
		Scheduler.schedule(tfIdfManager, UpdateTfIdfCollection(), 60, 60, SECONDS)
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