package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.{Filter, MassFilter}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.springframework.context.ApplicationContext

/**
  * Include the spectra, if any ion of it's ions has a mass between any of the required ions +/- the mass accuracy
  */
class IncludesByPeakHeight(val peaks: Seq[Double], val phaseToLog: String, val massAccuracy: Double = 0.0005, val minIntensity: Float = 0.0f) extends MassFilter[Feature](massAccuracy) {


  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doInclude(spectra: Feature, applicationContext: ApplicationContext): Boolean = {
    spectra.associatedScan.get.ions.exists { spectraIon =>
      peaks.exists { peak =>
        if (sameMass(peak, spectraIon.mass)) {
          spectraIon.intensity > minIntensity
        }
        else {
          false
        }
      }
    }
  }

  override protected val usedSettings: Map[String, Any] = Map("basePeaks" -> peaks, "massAccuracyInDalton" -> massAccuracy, "nominal" -> isNominal, "minIntensity" -> minIntensity)

}
