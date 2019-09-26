package edu.ucdavis.fiehnlab.ms.carrot.core.similarity.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.similarity.Similarity
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
    val filteredIons: Seq[(Ion, Int)] = ions
      .zipWithIndex
      .drop(startIdx)
      .dropWhile { case (ion, i) => ion.mass < focusedMz - tolerance }

    val matchingIons = filteredIons
      .takeWhile { case (ion, i) => focusedMz - tolerance <= ion.mass && ion.mass < focusedMz + tolerance }

    // sum the intensities of the matching ions
    val summedIntensity: Double = matchingIons.map { case (ion, i) => ion.intensity }.sum

    // update the start index of if necessary
    val newStartIdx =
      if (matchingIons.nonEmpty && matchingIons.last._2 + 1 < ions.length) {
        matchingIons.last._2 + 1
      } else if (filteredIons.nonEmpty) {
        filteredIons.head._2
      } else {
        startIdx
      }

    // return the summedIntensity intensity and the last index used
    (summedIntensity, newStartIdx)
  }


  def getPeakPenalty(ionCount: Int): Double = ionCount match {
    case 1 => 0.75
    case 2 => 0.88
    case 3 => 0.94
    case 4 => 0.97
    case _ => 1
  }
}
