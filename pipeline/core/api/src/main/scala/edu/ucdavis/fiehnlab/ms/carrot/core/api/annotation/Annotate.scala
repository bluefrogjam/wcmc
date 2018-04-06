package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.utilities.logging.{JSONAlgorithmLogging, JSONPhaseLogging, JSONSettingsLogging}

/**
  * Simple class to help us with annotations
  */
abstract class Annotate extends JSONPhaseLogging with JSONSettingsLogging {

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param unknown
    * @param target
    * @return
    */
  final def isMatch(unknown: Feature, target: Target): Boolean = {
    val result = doMatch(unknown, target)

    //a bit uggly but no better way right now todo so
    if (this.supportsJSONLogging) {

      val jsonLogger = new JSONPhaseLogging with JSONAlgorithmLogging with JSONTargetLogging with JSONSampleLogging with JSONFeatureLogging with JSONSettingsLogging {
        /**
          * which phase we require to log
          */
        override protected val phaseToLog: String = Annotate.this.phaseToLog
        /**
          * which target we require to log
          */
        override protected val targetToLog = target
        /**
          * which sample we require to log
          */
        override protected val sampleToLog = unknown.sample
        /**
          * which feature we require to log
          */
        override protected val featureToLog = unknown
        /**
          * by default we want to log the actual implementation
          */
        override protected val classUnderInvestigation = Annotate.this
        /**
          * references to all used settings
          */
        override protected val usedSettings = Annotate.this.usedSettings
      }

      jsonLogger.logJSON(Map("pass" -> result))
    }
    result
  }


  protected def doMatch(unknown: Feature, target: Target): Boolean
}

/**
  * this defines a sequential annotator, if all annotations pass
  * it will consider the annotation to be a success
  */
class SequentialAnnotate(val annotators: Seq[Annotate]) extends Annotate {

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param unknown
    * @param target
    * @return
    */
  override def doMatch(unknown: Feature, target: Target): Boolean = {
    if (annotators.nonEmpty) {
      annotators.forall(_.isMatch(unknown, target))
    }
    else {
      false
    }
  }

  override protected def supportsJSONLogging = false

  /**
    * which phase we require to log
    */
  override protected val phaseToLog = "none"
  /**
    * references to all used settings
    */
  override protected val usedSettings = Map[String, Any]()
}
