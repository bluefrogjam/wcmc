package edu.ucdavis.fiehnlab.ms.carrot.core

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.apache.logging.log4j.scala.Logging

abstract class Similarity extends Logging {

  /**
    * computes a similarity between two spectra and return a value between 0 and 1
    * @param unknown
    * @param reference
    * @return
    */
  def compare(unknown: Feature, reference: Feature): Double = {
    if (unknown.associatedScan.isEmpty || unknown.associatedScan.get.ions.isEmpty ||
        reference.associatedScan.isEmpty || reference.associatedScan.get.ions.isEmpty) {
      0
    } else {
      doCompare(unknown, reference)
    }
  }


  def doCompare(unknown: Feature, reference: Feature): Double
}