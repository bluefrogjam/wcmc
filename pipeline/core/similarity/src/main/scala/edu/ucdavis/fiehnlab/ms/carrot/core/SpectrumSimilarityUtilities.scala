package edu.ucdavis.fiehnlab.ms.carrot.core

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

object SpectrumSimilarityUtilities {

  /**
    * rounds m/z values based on the 80/20 rule
    */
  def roundMZ(mz: Double): Int = math.round(mz + 0.2).toInt

  /**
    * rounds m/z values based on the 80/20 rule
    */
  def roundMZ(ion: Ion): Ion = Ion(roundMZ(ion.mass), ion.intensity)

  /**
    *
    * @param spectrum
    * @return
    */
  def convertToNominal(spectrum: Feature): Map[Int, Ion] = {
    spectrum.associatedScan.get.ions
      .groupBy(x => roundMZ(x.mass))
      .map { case(mass, ions) => (mass, Ion(mass, ions.map(_.intensity).sum)) }
  }
}
