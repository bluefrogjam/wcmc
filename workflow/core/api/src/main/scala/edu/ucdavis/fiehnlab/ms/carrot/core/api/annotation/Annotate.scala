package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedMSSpectra, Feature, MSLibrarySpectra, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import org.springframework.beans.factory.annotation.Autowired

/**
  * Simple class to help us with annotations
  */
abstract class Annotate {

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param unknown
    * @param target
    * @return
    */
  def isMatch(unknown: Feature, target: Target): Boolean
}

/**
  * this defines a sequential annotator, if all annotations pass
  * it will consider the annotation to be a success
  */
class SequentialAnnotate(val annotators: List[Annotate]) extends Annotate with LazyLogging{

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param unknown
    * @param target
    * @return
    */
  override def isMatch(unknown: Feature, target: Target): Boolean = {
    logger.debug(s"Evaluating ${unknown} vs ${target}")
    if(annotators.nonEmpty) {
      annotators.foreach { annotator =>
        val result = annotator.isMatch(unknown, target)

        logger.debug(s"\t=> running annotate: ${annotator}")
        logger.debug(s"\t\t=> result was: ${result}")
        if (!result) {
          logger.debug("\t\t\t=> spectra rejected!")
          return false
        }
      }

      logger.debug("\t\t\t=> spectra accepted")
      true
    }
    else{
      false
    }
  }
}
