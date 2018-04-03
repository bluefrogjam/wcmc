package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{AccurateMassSupport, MSSpectra, SimilaritySupport}

/**
  * includes spectra, which have a certain ion to base peak ratio in a defined range
  *
  * @param ion
  * @param minRatio
  * @param maxRatio
  * @param massAccuracy
  */
class IncludeByIonRatio(val ion: Double, val minRatio: Double, val maxRatio: Double, val massAccuracy: Double = 0.0) extends Filter[MSSpectra] {

  logger.info(s"searching for ratio against ion ${ion}")

  def isNominal: Boolean = massAccuracy == 0.0

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = {

    val (ions, basePeak) = spectra match {
      case x: SimilaritySupport => (x.spectrum.get.ions, x.spectrum.get.basePeak)
      case _ => (spectra.associatedScan.get.ions, spectra.associatedScan.get.basePeak)
    }


    //logger.info(s"unfiltered: ${ions.sortBy(_.mass)}")

    val filteredIons = ions.filter { peak =>
      if (isNominal) {
        Math.floor(peak.mass + 0.2) == Math.floor(ion + 0.2)
      }
      else {
        peak.mass > (ion - massAccuracy) && peak.mass < (ion + massAccuracy)
      }
    }

    //logger.info(s"filtered: ${filteredIons.sortBy(_.mass)}")

    val exists = filteredIons.exists { peak =>
      val ratio =  peak.intensity/basePeak.intensity
      //logger.info(f"ratio between ${basePeak} and ${peak} is ${ratio}%1.4f, must be in range of ${minRatio} and ${maxRatio}")
      ratio >= minRatio && ratio <= maxRatio
    }

    //logger.info(s"ion ratio found: ${exists}")

    exists
  }
}
