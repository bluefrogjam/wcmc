package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Include the spectra, if any ion of it's ions has a mass between any of the required ions +/- the mass accuracy
 */
@Profile(Array("carrot.filters.intensity"))
@Component
class TargetIonHeightFilter @Autowired()(@Value("${carrot.filters.minIntensity:1000}") val minIntensity: Float = 0.0f) extends Filter[Target] {

  /**
   * this returns true, if the spectra should be included, false if it should be excluded
   */
  protected override def doInclude(spectrum: Target, applicationContext: ApplicationContext): Boolean = {
    spectrum.spectrum match {
      case Some(spec) =>
        spec.ions.exists {
          _.intensity >= minIntensity
        }
      case None => false
    }
  }
}
