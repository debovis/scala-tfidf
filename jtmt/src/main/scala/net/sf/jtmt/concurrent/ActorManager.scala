// Source: ActorManager.scala
package net.sf.jtmt.concurrent

import java.lang._
import java.util.concurrent.CountDownLatch
import scala.actors._
import scala.actors.Actor._

object ActorManager {

  val latch = new CountDownLatch(3)
  def decrementLatch(): Unit = {
    latch.countDown
  }

  def log(message:String): Unit = {
    //println(message)
  }

  def main(args: Array[String]): Unit = {
    // start the actors
    DownloadActor.start
    IndexActor.start
    WriteActor.start
    // seed the download actor with requests
    val start = System.currentTimeMillis
    for (i <- 1 until 1000) {
      val payload = "Requested " + i
      log(payload)
      DownloadActor ! payload
    }
    // ask them to stop
    DownloadActor ! StopMessage
    // wait for actors to stop
    latch.await
    println("elapsed = " + (System.currentTimeMillis - start))
  }
}

case class StopMessage()

object DownloadActor extends Actor {
  def act() {
    loop {
      react {
        case payload: String => {
          val newPayload = payload.replaceFirst("Requested ", "Downloaded ")
          ActorManager.log(newPayload)
          IndexActor ! newPayload
        }
        case StopMessage => {
          ActorManager.log("Stopping download")
          IndexActor ! StopMessage
          ActorManager.decrementLatch
          exit
        }
      }
    }
  }
}

object IndexActor extends Actor {
  def act() {
    loop {
      react {
        case payload: String => {
          val newPayload = payload.replaceFirst("Downloaded ", "Indexed ")
          ActorManager.log(newPayload)
          WriteActor ! newPayload
        }
        case StopMessage => {
          ActorManager.log("Stopping Index")
          WriteActor ! StopMessage
          ActorManager.decrementLatch
          exit
        }
      }
    }
  }
}

object WriteActor extends Actor {
  def act() {
    loop {
      react {
        case payload: String => {
          val newPayload = payload.replaceFirst("Indexed ", "Wrote ")
          ActorManager.log(newPayload)
        }
        case StopMessage => {
          ActorManager.log("Stopping Write")
          ActorManager.decrementLatch
          exit
        }
      }
    }
  }
}
