package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

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
  final def isMatch(unknown: Feature, target: Target): Boolean = {
    val result = doMatch(unknown, target)

    result
  }


  protected def doMatch(unknown: Feature, target: Target): Boolean
}

/**
  * this defines a sequential annotator, if all annotations pass
  * it will consider the annotation to be a success
  */
class SequentialAnnotate(val annotators: Seq[Annotate]) extends Annotate {

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param unknown
    * @param target
    * @return
    */
  override def doMatch(unknown: Feature, target: Target): Boolean = {
    if (annotators.nonEmpty) {
      annotators.forall(_.isMatch(unknown, target))
    }
    else {
      false
    }
  }
}
