package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import org.springframework.context.ApplicationContext

/**
  * includes if the tic of the given spectra is larger than the minIntensity
  * @param minIntensity
  */
class IncludeByTicFilter(minIntensity: Float) extends Filter[MSSpectra] {

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doIncludeWithDetails(spectra: MSSpectra, applicationContext: ApplicationContext): (Boolean,Any) = {

    if (spectra.associatedScan.isDefined) {
      (spectra.associatedScan.get.tic > minIntensity,spectra.associatedScan.get.tic)
    }
    else {
      (false,"no associated scan")
    }
  }
}
