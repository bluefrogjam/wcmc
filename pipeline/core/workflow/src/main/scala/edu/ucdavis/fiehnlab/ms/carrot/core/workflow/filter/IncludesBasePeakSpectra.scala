package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.MassFilter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import org.springframework.context.ApplicationContext

/**
  * includes all spectra, having the base peak defined in the list of basePeaks, with the accuracy between Peak +/- accuracy
  *
  * @param basePeaks
  * @param accuracy
  */
class IncludesBasePeakSpectra(val basePeaks: Seq[Double], val accuracy: Double = 0.00005) extends MassFilter[MSSpectra](accuracy) with LazyLogging {

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
}
