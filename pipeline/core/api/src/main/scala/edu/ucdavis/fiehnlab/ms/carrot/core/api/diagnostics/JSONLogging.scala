package edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, Target}
import edu.ucdavis.fiehnlab.utilities.logging.JSONLogging


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
