package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import edu.ucdavis.fiehnlab.utilities.logging.{JSONPhaseLogging, JSONSettingsLogging}
import org.springframework.context.ApplicationContext

/**
  * excludes all spectra with the specific base peak
  *
  * @param basePeaks
  * @param accuracy
  */
class ExcludeBasePeakSpectra(override val basePeaks: Seq[Double], override val phaseToLog: String, override val accuracy: Double = 0.00005) extends IncludesBasePeakSpectra(basePeaks = basePeaks, accuracy = accuracy, phaseToLog = phaseToLog) with JSONPhaseLogging with JSONSettingsLogging {
  protected override def doInclude(spectra: MSSpectra, applicationContext: ApplicationContext): Boolean = {
    basePeaks.exists { peak =>

      val result = !(peak > (spectra.associatedScan.get.basePeak.mass - accuracy) && peak < (spectra.associatedScan.get.basePeak.mass + accuracy))

      logger.debug(s"it's considered to be accepted: ${result} with an accuracy of ${accuracy}")
      result
    }
  }

  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map("basePeaks" -> basePeaks, "massAccuracyInDalton" -> accuracy)
}
