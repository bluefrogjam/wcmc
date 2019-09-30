package edu.ucdavis.fiehnlab.ms.carrot.core.similarity.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

class PresenceSimilarity extends MSDialSimilarity {

  /**
    * computes the similarity of two spectra based on the percentage of matched ions
    *
    * @param unknown
    * @param library
    * @return
    */
  override def doCompare(unknown: Feature, reference: Feature, tolerance: Double): Double = {

    // get sorted ions
    val unknownIons: Seq[Ion] = unknown.associatedScan.get.ions.sortBy(_.mass)
    val referenceIons: Seq[Ion] = reference.associatedScan.get.ions.sortBy(_.mass)

    // properties of the spectra for comparison
    val minMz: Double = referenceIons.head.mass
    val maxMz: Double = referenceIons.last.mass
    val maxReferenceIntensity: Float = referenceIons.map(_.intensity).max

    // matching ion counters
    var unknownCounter: Int = 0
    var referenceCounter: Int = 0

    // index of last used ion, for optimization
    var unknownIdx: Int = 0
    var referenceIdx: Int = 0

    // use a shifting focused mass to collect all ions
    var focusedMz: Double = minMz

    while (focusedMz <= maxMz) {

      // find reference ions near the focused mass
      val (sumRef, newRefIdx) = getMatchingIonIntensity(referenceIons, referenceIdx, focusedMz, tolerance)
      referenceIdx = newRefIdx

      if (sumRef > 0.01 * maxReferenceIntensity) {
        referenceCounter += 1

        // if a reference ion was find, look for an ion in the unknown spectrum
        val (sumUnknown, newUnknownIdx) = getMatchingIonIntensity(unknownIons, unknownIdx, focusedMz, tolerance)
        unknownIdx = newUnknownIdx

        if (sumUnknown > 0) {
          unknownCounter += 1
        }
      }

      if (focusedMz + tolerance > maxMz) {
        // alternative to breaking
        focusedMz = focusedMz + tolerance
      } else {
        focusedMz = referenceIons(referenceIdx).mass
      }
    }

    if (referenceCounter == 0) {
      0
    } else {
      unknownCounter.toDouble / referenceCounter
    }
  }
}
