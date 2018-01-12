package edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics

import java.util.Date

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, Target}

/**
  * simple diagnostics helper to simplify logging for us
  */
object JSONLogging {

  val objectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)

}

trait JSONLogging extends LazyLogging {

  /**
    * based on mixins and other defined methods, this will log a json string to the console for debugging
    * as well as return
    */
  final def logJSON(map: Map[String, Any] = Map()): String = {
    if (supportsJSONLogging) {
      val message = JSONLogging.objectMapper.writeValueAsString(buildMessage() ++ map)
      logger.debug(s"isMatch JSON:${message}")
      message
    }
    else {
      ""
    }
  }

  protected def buildMessage(): Map[String, Any] = Map("date" -> new Date())

  protected def supportsJSONLogging: Boolean = true
}

/**
  * logs settings and variables used for this log message
  */
trait JSONSettingsLogging extends JSONLogging {

  /**
    * references to all used settings
    */
  protected val usedSettings: Map[String, Any]

  override def buildMessage(): Map[String, Any] = super.buildMessage() + ("settings" -> usedSettings)
}

/**
  * logs with a target
  */
trait JSONTargetLogging extends JSONLogging {

  /**
    * which target we require to log
    */
  protected val targetToLog: Target

  override def buildMessage(): Map[String, Any] = super.buildMessage() + ("target" -> Map("name" -> targetToLog.name.getOrElse("unknown"), "rt" -> targetToLog.retentionTimeInSeconds, "ri" -> targetToLog.retentionIndex, "mass" -> targetToLog.accurateMass.getOrElse(0.0)))

}

trait JSONPhaseLogging extends JSONLogging {

  /**
    * which phase we require to log
    */
  protected val phaseToLog: String

  override def buildMessage(): Map[String, Any] = super.buildMessage() + ("phase" -> phaseToLog)

}

/**
  * logs with a feature
  */
trait JSONFeatureLogging extends JSONLogging {

  /**
    * which feature we require to log
    */
  protected val featureToLog: Feature

  override def buildMessage(): Map[String, Any] = {

    var buildFeature = Map("rt" -> featureToLog.retentionTimeInSeconds, "mass" -> featureToLog.accurateMass.getOrElse(0.0), "scan" -> featureToLog.scanNumber)

    featureToLog match {
      case spectra: CorrectedSpectra =>
        buildFeature = buildFeature + ("ri" -> spectra.retentionIndex)
      case _ =>

    }
    super.buildMessage() + ("feature" -> buildFeature)
  }
}

/**
  * logs with a sample
  */
trait JSONSampleLogging extends JSONLogging {

  /**
    * which sample we require to log
    */
  protected val sampleToLog: Sample

  /**
    * adds the provided sample information
    *
    * @return
    */
  override def buildMessage(): Map[String, Any] = {
    super.buildMessage() + ("sample" -> sampleToLog.fileName)
  }
}

trait JSONAlgorithmLogging extends JSONLogging {

  /**
    * by default we want to log the actual implementation
    */
  protected val classUnderInvestigation: Any = this

  override def buildMessage(): Map[String, Any] = {
    super.buildMessage() + ("algorithm" -> classUnderInvestigation.getClass.getSimpleName)
  }

}