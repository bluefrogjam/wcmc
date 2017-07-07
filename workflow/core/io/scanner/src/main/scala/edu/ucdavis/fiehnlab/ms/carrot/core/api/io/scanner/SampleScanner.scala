package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.Files.copy
import java.nio.file.{CopyOption, Paths}
import java.util.zip.GZIPInputStream

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinPool
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.actors.{ListenerCallingActor, MonitoringActor, ProcessingActor, TaskScheduled}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.listeners.SampleScannerListener
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * This is a simple utility class to discover samples on the file system
  * and whenever one is found, will notify the client about it
  */
class SampleScanner(actorSystem: ActorSystem, listeners: List[SampleScannerListener], extensions: Array[String] = Array("mzData", "mzML", "mzXML", "cdf", "txt", "msdial"), failOnError: Boolean = false, cpuCount: Int = Runtime.getRuntime.availableProcessors()) extends LazyLogging {

  /**
    * utilized for monitoring and when done to shutdown the actor
    */
  val monitoringActor: ActorRef = actorSystem.actorOf(Props(new MonitoringActor()), "monitor")

  /**
    * notifies all the listeners that a simple was processed and he ready for further work
    */
  val notifierActor: ActorRef = actorSystem.actorOf(Props(new ListenerCallingActor(listeners, monitoringActor)))
  /**
    * utilized for load balancing and ensuring the system is not overloaded
    */
  val router: ActorRef = actorSystem.actorOf(RoundRobinPool(cpuCount).props(Props.create(classOf[ProcessingActor], notifierActor)))

  logger.info(s"utilizing ${cpuCount} cpus for the file scanner")

  /**
    * scans the defined directory
    * and tries to parse samples in it
    *
    * @param file
    */
  def scan(file: File): Unit = {
    logger.debug(s"checking file: ${file}")
    if (file.isDirectory) {
      logger.debug("it's a directory")
      try {
        file.listFiles().foreach {
          f: File => scan(f)
        }
      }
      catch {
        case x: NullPointerException =>
          logger.warn(s"exception at ${file}, skipped!")
          logger.debug(x.getMessage, x)
      }
    }
    else if (file.isFile) {
      logger.debug("==> it's a file")
      try {

        var supported: Boolean = false

        extensions.foreach { extension: String =>
          if (!supported) {
            logger.debug(s"===> checking extension ${extension}")
            supported = file.getName.toLowerCase().endsWith(extension.toLowerCase())

            if (supported) {
              logger.debug("====> extension was valid!")
            }
            else {
              logger.debug("====> extension was invalid")
            }
          }
        }
        if (supported) {
          //send the file to the processing system in the backend
          logger.info(s"scheduling for processing: ${file}")

          monitoringActor ! new TaskScheduled()
          router ! file
        }
        else {
          logger.debug("===> skipped the sample, do to it extensions not being supported")
        }

      }
      catch {
        case e: Exception =>
          if (failOnError) {
            throw e
          }
          else {
            logger.debug(s"ignored error: ${e.getMessage}", e)
          }
      }
    }
    else {
      logger.info("this is a not supported file!")
    }
  }
}


/**
  * simple factory to support our different file types
  */
object SampleFactory {
  def build(file: File): Sample = {
//    println(s"file: ${file}")
    if (file.getName.toLowerCase().matches(".*.txt[.gz]*")) {
      //leco
      null
    }
    else if (file.getName.toLowerCase().matches(".*.msdial[.gz]*")) {
      MSDialSample(file)
    }
    else {
        MSDKSample(file)

    }
  }
}