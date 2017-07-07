package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.actors

import java.io.File

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef}
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.SampleFactory
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.listeners.SampleScannerListener
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * actually reads and parses the files
  *
  */
class ProcessingActor(writingActor:ActorRef) extends Actor with LazyLogging {
  override def receive: Receive = {
    case file: File =>
      try {

        logger.info(s"received processing job: ${file}")
        val sample = SampleFactory.build(file)
        writingActor ! sample
        logger.info("finished processing")
      }
      catch {
        case x: Exception =>
          logger.info(s"processing failed: ${file}")
          logger.debug(x.getMessage, x)
      }

  }
}

/**
  * forwards parsed samples to the different event listeners. Due to possible io issues this has to be done synchronous
  * @param listeners
  * @param monitoringActor
  */
class ListenerCallingActor(listeners: List[SampleScannerListener],monitoringActor: ActorRef) extends Actor{
  override def receive: Receive = {
    case sample:Sample =>
      listeners.foreach(_.found(sample))
      monitoringActor ! new TaskFinished
  }
}