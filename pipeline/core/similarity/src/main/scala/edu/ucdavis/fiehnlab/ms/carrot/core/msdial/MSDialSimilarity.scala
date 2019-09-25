package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.Similarity
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion

abstract class MSDialSimilarity extends Similarity {

  /**
    * find the summed intensity of ions matching the current focusedMz with the given tolerance
    * @param ions
    * @param startIdx
    * @param focusedMz
    * @param tolerance
    * @return the
    */
  def getMatchingIonIntensity(ions: Seq[Ion], startIdx: Int, focusedMz: Double, tolerance: Double): (Double, Int) = {

    // ignore previously used ions, and keep only those inside the tolerance range
    val matchingIons: Seq[(Ion, Int)] = ions
      .zipWithIndex
      .drop(startIdx)
      .dropWhile { case (ion, i) => ion.mass < focusedMz - tolerance }
      .takeWhile { case (ion, i) => focusedMz - tolerance <= ion.mass && ion.mass < focusedMz + tolerance }

    // sum the intensities of the matching ions
    val summedIntensity: Double = matchingIons.map { case (ion, i) => ion.intensity }.sum

    // update the start index of if necessary
    val newStartIdx =
      if (matchingIons.last._2 + 1 < ions.length) {
        matchingIons.last._2 + 1
      } else {
        startIdx
      }

    // return the summed intensity and the last index used
    (summedIntensity, newStartIdx)
  }
}
