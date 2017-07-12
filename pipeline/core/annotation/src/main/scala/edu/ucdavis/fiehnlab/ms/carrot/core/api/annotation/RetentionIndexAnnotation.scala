package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSpectra, Target}

/**
  * Created by wohlg_000 on 6/10/2016.
  */
class RetentionIndexAnnotation(retentionIndexWindow: Double) extends Annotate with LazyLogging {

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def isMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {

    val time: Double = correctedSpectra.asInstanceOf[CorrectedSpectra].retentionIndex
    val min: Double = librarySpectra.retentionTimeInSeconds - retentionIndexWindow
    val max: Double = librarySpectra.retentionTimeInSeconds + retentionIndexWindow

    val result = time > min && time < max

    logger.trace(s"min = ${min}, max = ${max}, rt: ${correctedSpectra.retentionTimeInSeconds}, library: ${librarySpectra.retentionTimeInSeconds} result:$result")

    result
  }
}
