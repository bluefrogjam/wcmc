package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.{Filter, MassFilter}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import org.springframework.context.ApplicationContext

/**
  * includes all spectra, having the base peak defined in the list of basePeaks, with the accuracy between Peak +/- accuracy
  *
  * @param basePeaks
  * @param accuracy
  */
class IncludesBasePeakSpectra(val basePeaks: Seq[Double],val phaseToLog:String, val accuracy: Double = 0.00005) extends MassFilter[MSSpectra](accuracy) {

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doInclude(spectra: MSSpectra,applicationContext: ApplicationContext): Boolean = {
    basePeaks.exists { peak =>
      logger.debug(s"basePeak of spectra is ${spectra.associatedScan.get.basePeak.mass} compared to ${peak}")

      val result = sameMass(peak,spectra.associatedScan.get.basePeak.mass)

      logger.debug(s"it's considered to be accepted: ${result} with an accuracy of ${accuracy}")
      result
    }
  }

  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map("basePeaks" -> basePeaks,"massAccuracyInDalton" -> accuracy,"nominal" -> isNominal)
}
