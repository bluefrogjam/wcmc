package edu.ucdavis.fiehnlab.ms.carrot.core.similarity

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.apache.logging.log4j.scala.Logging

abstract class Similarity extends Logging {

  /**
    * computes a similarity between two spectra and return a value between 0 and 1
    * @param unknown
    * @param reference
    * @return
    */
  def compare(unknown: Feature, reference: Feature, tolerance: Double = 0.5): Double = {
    if (unknown.associatedScan.isEmpty || unknown.associatedScan.get.ions.isEmpty ||
        reference.associatedScan.isEmpty || reference.associatedScan.get.ions.isEmpty) {
      0
    } else {
      doCompare(unknown, reference, tolerance)
    }
  }


  def doCompare(unknown: Feature, reference: Feature, tolerance: Double): Double
}

abstract class NominalMassSimilarity extends Similarity {

  /**
    * simplify the method definition as tolerance values are not needed for nominal mass similarity
    * @param unknown
    * @param reference
    * @param tolerance
    * @return
    */
  def doCompare(unknown: Feature, reference: Feature, tolerance: Double): Double = doCompare(unknown, reference)

  def doCompare(unknown: Feature, reference: Feature): Double
}