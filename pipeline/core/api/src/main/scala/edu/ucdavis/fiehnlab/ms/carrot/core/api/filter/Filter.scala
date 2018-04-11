package edu.ucdavis.fiehnlab.ms.carrot.core.api.filter

import edu.ucdavis.fiehnlab.utilities.logging.{JSONLogging, JSONPhaseLogging, JSONSettingsLogging}
import org.springframework.context.ApplicationContext

/**
  * Created by wohlg_000 on 4/22/2016.
  */
trait Filter[T] extends JSONLogging with JSONPhaseLogging with JSONSettingsLogging {

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    * it supports the basic json logging model
    * to allow for easier discovery of annotation based bugs
    */
  final def include(spectra: T, applicationContext: ApplicationContext): Boolean = {
    val result = doInclude(spectra, applicationContext)

    if (this.supportsJSONLogging) {
      logJSON(Map("pass" -> result))
    }

    result
  }

  protected def doInclude(spectra: T, applicationContext: ApplicationContext): Boolean

  /**
    * this returns true if the spectra should be excluded
    * or false if it should be included. Just a little convenient helper method
    *
    * @param sSpectra
    * @return
    */
  final def exclude(sSpectra: T, applicationContext: ApplicationContext): Boolean = !include(sSpectra, applicationContext)
}






















