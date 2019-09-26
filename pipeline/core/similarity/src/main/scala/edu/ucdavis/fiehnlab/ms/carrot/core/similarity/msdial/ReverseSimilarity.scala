package edu.ucdavis.fiehnlab.ms.carrot.core.similarity.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

import scala.collection.mutable.ArrayBuffer

class ReverseSimilarity extends MSDialSimilarity {

  /**
    * computes the reverse similarity of two spectra
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

    // index of last used ion, for optimization
    var unknownIdx: Int = 0
    var referenceIdx: Int = 0

    // mass lists
    val unknownMassList: ArrayBuffer[Ion] = ArrayBuffer()
    val referenceMassList: ArrayBuffer[Ion] = ArrayBuffer()

    // values used for calculating cosine similarity
    var unknownBase: Double = 0
    var referenceBase: Double = 0

    // use a shifting focused mass to collect all ions
    var focusedMz: Double = minMz

    while (focusedMz <= maxMz) {

      // find reference ions near the focused mass
      val (sumRef, newRefIdx) = getMatchingIonIntensity(referenceIons, referenceIdx, focusedMz, tolerance)
      referenceIdx = newRefIdx

      referenceMassList.append(Ion(focusedMz, sumRef.toFloat))
      referenceBase = math.max(sumRef, referenceBase)

      // look for unknown ions near the focused mass
      val (sumUnknown, newUnknownIdx) = getMatchingIonIntensity(unknownIons, unknownIdx, focusedMz, tolerance)
      unknownIdx = newUnknownIdx

      unknownMassList.append(Ion(focusedMz, sumUnknown.toFloat))
      unknownBase = math.max(sumUnknown, unknownBase)


      if (focusedMz + tolerance > maxMz) {
        // alternative to breaking
        focusedMz = focusedMz + tolerance
      } else {
        focusedMz = referenceIons(referenceIdx).mass
      }
    }


    if (unknownBase == 0 || referenceBase == 0) {
      0
    } else {
      // calculate summed intensity and count non-noise peaks
      var unknownIonCounter: Int = 0
      var referenceIonCounter: Int = 0

      val unknownSum: Double = unknownMassList.map { x =>
        if (x.intensity / unknownBase > 0.1) {
          unknownIonCounter += 1
        }

        x.intensity / unknownBase
      }.sum

      val referenceSum: Double = referenceMassList.map { x =>
        if (x.intensity / referenceBase > 0.1) {
          referenceIonCounter += 1
        }

        x.intensity / referenceBase
      }.sum

      // calculate cosine similarity
      val cutoff = 0.01

      val filteredSums = unknownMassList.map(_.intensity / unknownBase)
        .zip(referenceMassList.map(_.intensity / referenceBase))
        .filter(_._2 >= cutoff)

      val unknownScalar = filteredSums.map(x => x._1 * x._1).sum
      val referenceScalar = filteredSums.map(x => x._2 * x._2).sum
      val covariance = filteredSums.map(x => math.sqrt(x._1 * x._2) * x._1).sum

      if (unknownScalar == 0 || referenceScalar == 0) {
        0
      } else {
        getPeakPenalty(referenceIonCounter) * covariance * covariance / unknownScalar / referenceScalar
      }
    }
  }
}