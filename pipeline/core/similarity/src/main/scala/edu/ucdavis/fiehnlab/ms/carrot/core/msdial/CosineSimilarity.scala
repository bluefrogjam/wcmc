package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

import scala.collection.mutable.ArrayBuffer

class CosineSimilarity extends MSDialSimilarity {

  /**
    * computes the cosine similarity of two spectra
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
    val minMz: Double = math.min(unknownIons.head.mass, referenceIons.head.mass)
    val maxMz: Double = math.max(unknownIons.last.mass, referenceIons.last.mass)

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

      // look for unknown ions near the focused mass
      val (sumUnknown, newUnknownIdx) = getMatchingIonIntensity(unknownIons, unknownIdx, focusedMz, tolerance)
      unknownIdx = newUnknownIdx

      unknownMassList.append(Ion(focusedMz, sumUnknown.toFloat))
      unknownBase = math.max(sumUnknown, unknownBase)

      // find reference ions near the focused mass
      val (sumRef, newRefIdx) = getMatchingIonIntensity(referenceIons, referenceIdx, focusedMz, tolerance)
      referenceIdx = newRefIdx

      referenceMassList.append(Ion(focusedMz, sumRef.toFloat))
      referenceBase = math.max(sumRef, referenceBase)


      if (focusedMz + tolerance > maxMz) {
        // alternative to breaking
        focusedMz = focusedMz + tolerance
      } else if (focusedMz + tolerance > referenceIons(referenceIdx).mass && focusedMz + tolerance <= unknownIons(unknownIdx).mass) {
        focusedMz = unknownIons(unknownIdx).mass
      } else if (focusedMz + tolerance <= referenceIons(referenceIdx).mass && focusedMz + tolerance > unknownIons(unknownIdx).mass) {
        focusedMz = referenceIons(referenceIdx).mass
      } else {
        focusedMz = math.min(unknownIons(unknownIdx).mass, referenceIons(referenceIdx).mass)
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
        .filter(_._1 >= cutoff)

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