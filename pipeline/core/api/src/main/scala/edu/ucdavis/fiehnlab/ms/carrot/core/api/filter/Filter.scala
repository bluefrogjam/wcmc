package edu.ucdavis.fiehnlab.ms.carrot.core.api.filter

import edu.ucdavis.fiehnlab.utilities.logging.{JSONAlgorithmLogging, JSONLogging, JSONPhaseLogging, JSONSettingsLogging}
import org.springframework.context.ApplicationContext

/**
  * Created by wohlg_000 on 4/22/2016.
  */
trait Filter[T] extends JSONLogging with JSONPhaseLogging with JSONSettingsLogging with JSONAlgorithmLogging {

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    * it supports the basic json logging model
    * to allow for easier discovery of annotation based bugs
    */
  final def include(spectra: T, applicationContext: ApplicationContext): Boolean = {
    val result = doIncludeWithDetails(spectra, applicationContext)

    if (this.supportsJSONLogging) {
      logJSON(Map("pass" -> result._1, "evaluating" -> spectra, "evaluationResult" -> result._2))
    }

    result._1
  }

  /**
    * a method which should be overwritten, if the filter can provide details why it failed
    * @param spectra
    * @param applicationContext
    * @return
    */
  protected def doIncludeWithDetails(spectra: T, applicationContext: ApplicationContext): (Boolean, Any) = {
    (doInclude(spectra, applicationContext), "no details available")
  }

  /**
    * this implementation should be overwritten if the filter cannot provide information why it failed
    * @param spectra
    * @param applicationContext
    * @return
    */
  protected def doInclude(spectra: T, applicationContext: ApplicationContext): Boolean = false

  /**
    * this returns true if the spectra should be excluded
    * or false if it should be included. Just a little convenient helper method
    *
    * @param sSpectra
    * @return
    */
  final def exclude(sSpectra: T, applicationContext: ApplicationContext): Boolean = !include(sSpectra, applicationContext)

  /**
    * by default we want to log the actual implementation
    */
  override protected val classUnderInvestigation: Any = this
}






















