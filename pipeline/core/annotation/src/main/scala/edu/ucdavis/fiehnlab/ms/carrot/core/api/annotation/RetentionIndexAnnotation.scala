package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target

/**
  * Created by wohlg_000 on 6/10/2016.
  */
class RetentionIndexAnnotation(retentionIndexWindow: Double,val phase:String) extends Annotate with Logging {

  override protected val usedSettings = Map("window" -> retentionIndexWindow)
  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def doMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {

    val time: Double = correctedSpectra.asInstanceOf[CorrectedSpectra].retentionIndex
    val min: Double = librarySpectra.retentionIndex - retentionIndexWindow
    val max: Double = librarySpectra.retentionIndex + retentionIndexWindow

    val result = time > min && time < max

    logger.trace(s"min = ${min}, max = ${max}, rt: ${correctedSpectra.retentionTimeInSeconds}, library: ${librarySpectra.retentionIndex} result:$result")

    result
  }

  /**
    * which phase we require to log
    */
  override protected val phaseToLog = phase
}
