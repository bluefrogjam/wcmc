package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.MassAccuracy
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{AccurateMassSupport, CorrectedSpectra, Feature, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter.{IncludeByMassRange, IncludeByRetentionIndexWindow}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(Array("carrot.processing.replacement.mzrt"))
class MzRtZeroReplacement extends ZeroReplacement {

  @Autowired(required = false)
  val writer: ObjectMapper = null

  /**
    * replaces the given value, with the best possible value
    * based on the provided configuration settings
    *
    * @param needsReplacement
    * @param quantSample
    * @param correctedRawData
    * @return
    */
  override def replaceValue(needsReplacement: QuantifiedTarget[Double], quantSample: QuantifiedSample[Double], correctedRawData: CorrectedSample): GapFilledTarget[Double] = {

    val filterByMass = new IncludeByMassRange(needsReplacement, zeroReplacementProperties.failsafeMassAccuracyDa)

    val filterByRetentionIndexNoise = new IncludeByRetentionIndexWindow(needsReplacement.retentionIndex, zeroReplacementProperties.noiseWindowInSeconds)

    val filterByRetentionIndex = new IncludeByRetentionIndexWindow(needsReplacement.retentionIndex, zeroReplacementProperties.retentionIndexWindowForPeakDetection)

    val desperateFilterByMass = new IncludeByMassRange(needsReplacement, zeroReplacementProperties.failsafeMassAccuracyDa)

    // getting EIC
    val replacementValueSpectra = correctedRawData.spectra.filter { spectra =>
      //find the closest possible mass
      includeMass(needsReplacement, filterByMass, spectra)
    }
    //    logger.debug(s"found ${replacementValueSpectra.size} replacement values")

    //first calculate noise for this ion trace
    val data = replacementValueSpectra.map {
      s => MassAccuracy.findClosestIon(s, needsReplacement.precursorMass.get, needsReplacement).get.intensity.toDouble
    }
    //    logger.debug(s"found ${data.size} filtered intensities for noise calculation")

    val noise: Int = getNoiseValue(needsReplacement, data)

    val filteredByTime: Seq[Feature with CorrectedSpectra] = replacementValueSpectra.filter { spectra =>
      filterByRetentionIndex.include(spectra, applicationContext)
    }
    //    logger.debug(s"found ${filteredByTime.size} spectra, after retention index filter for target ${needsReplacement}")

    val value: (Feature with CorrectedSpectra) = {
      if (filteredByTime.isEmpty) {
        //        logger.info(s"worst case ${needsReplacement.name.get}")
        //        logger.info("replacementValueSpectra filter found no values for replacement")
        val intensity: Float = {
          if (zeroReplacementProperties.estimateByNearestFourIons && replacementValueSpectra.nonEmpty) {
            // estimate the replacement value using the nearest four ions of matching mass
            val leftIntensities = replacementValueSpectra
                .filter(x => needsReplacement.retentionIndex > x.retentionIndex)
                .sortBy(x => Math.abs(x.retentionIndex - needsReplacement.retentionIndex))
                .take(2)

            val rightIntensities = replacementValueSpectra
                .filter(x => needsReplacement.retentionIndex < x.retentionIndex)
                .sortBy(x => Math.abs(x.retentionIndex - needsReplacement.retentionIndex))
                .take(2)

            val nearestIntensities = (leftIntensities ++ rightIntensities)
                .map(x => MassAccuracy.findClosestIon(x, needsReplacement.precursorMass.get, needsReplacement).get.intensity)

            val intensity = nearestIntensities.sum / nearestIntensities.length
            //            logger.warn(s"\tFound failsafe intensity value from nearest ${nearestIntensities.length} " +
            //                s"ions with ${intensity} intensity")
            intensity
          }
          else if (zeroReplacementProperties.estimateByChromatogramNoise && replacementValueSpectra.nonEmpty) {
            // estimate the replacement value by averaging the intensities of the smallest 5% of ions
            val intensities = replacementValueSpectra
                .map(x => MassAccuracy.findClosestIon(x, needsReplacement.precursorMass.get, needsReplacement).get.intensity)
                .sorted
                .take((replacementValueSpectra.length / 20.0 + 1).toInt)

            val intensity = intensities.sum / intensities.length
            //            logger.warn(s"\tFound failsafe intensity value from smallest ${intensities.length} " +
            //                s"ions with ${intensity} intensity")
            intensity
          }
          else if (zeroReplacementProperties.estimateByBiggerMassWindow) {
            val desperateValues = correctedRawData.spectra.filter { spectra =>
              includeMass(needsReplacement, desperateFilterByMass, spectra)
            }
            //            logger.info(s"\tfound ${desperateValues.size} failsafe mass filtered replacement values")

            //            logger.info(writer.writeValueAsString(desperateValues.head))

            val tmp = desperateValues.map(_.associatedScan.get.ions).collect {
              case s if s.nonEmpty => s.filter(ion => ion.mass - needsReplacement.precursorMass.get <= zeroReplacementProperties.failsafeMassAccuracyDa)
                  .sortBy(_.intensity)
                  .reverse.head
            }
                .minBy {
                  _.intensity
                }
                .intensity

            val intensity = if (tmp > noise)
              tmp - noise
            else
              noise
            //            logger.warn(s"\tFound failsafe intensity value using 0.05 Da mass window (${intensity})")
            intensity
          }
          else {
            //            logger.warn(s"\tFound failsafe intensity value from noise (${noise})")
            noise
          }
        }

        val corrRI = quantSample.regressionCurve.computeY(needsReplacement.retentionIndex)

        val newAssociatedScan = correctedRawData.spectra.filter(spec => spec.retentionIndex - corrRI <= zeroReplacementProperties.retentionIndexWindowForPeakDetection)
            .minBy(spec => spec.retentionIndex - corrRI).associatedScan

        //        logger.debug(f"nr RI: ${needsReplacement.retentionIndex}%.4f --- corrRI: ${corrRI}%.4f")

        // failsafe feature to use for replacement
        new Feature with CorrectedSpectra {
          override val uniqueMass: Option[Double] = None
          override val signalNoise: Option[Double] = None
          override val ionMode: Option[IonMode] = Some(needsReplacement.ionMode)
          override val purity: Option[Double] = Some(1.0)
          override val sample: String = quantSample.fileName
          override val retentionIndex: Double = corrRI
          override val retentionTimeInSeconds: Double = needsReplacement.retentionTimeInSeconds
          override val scanNumber: Int = 0
          override val associatedScan: Option[SpectrumProperties] = newAssociatedScan
          override val massOfDetectedFeature: Option[Ion] = Some(Ion(needsReplacement.accurateMass.get, intensity))

        }
      }
      else { // filterByTime not empty
        if (needsReplacement.name.get.startsWith("FAHFA (26:2)"))
          logger.info(s"${needsReplacement.name.get}")

        //        logger.info(s"replacementValueSpectra filter found ${filteredByTime.size} values for replacement")
        val filtered = filteredByTime.maxBy { spectra =>
          MassAccuracy.findClosestIon(spectra, needsReplacement.precursorMass.get, needsReplacement, zeroReplacementProperties.failsafeMassAccuracyDa).get.intensity
        }

        logger.debug(s"closest: ${filtered}")

        new Feature with CorrectedSpectra {
          override val uniqueMass: Option[Double] = filtered.uniqueMass
          override val signalNoise: Option[Double] = filtered.signalNoise
          override val ionMode: Option[IonMode] = filtered.ionMode
          override val purity: Option[Double] = filtered.purity
          override val sample: String = filtered.sample
          override val retentionTimeInSeconds: Double = filtered.retentionTimeInSeconds
          override val scanNumber: Int = filtered.scanNumber
          override val associatedScan: Option[SpectrumProperties] = filtered.associatedScan
          override val massOfDetectedFeature: Option[Ion] = filtered.massOfDetectedFeature
          override val retentionIndex: Double = filtered.retentionIndex
        }

      }
    }

    val ion = MassAccuracy.findClosestIon(value, needsReplacement.precursorMass.get, needsReplacement).getOrElse(Ion(needsReplacement.precursorMass.get, 0))

    //    logger.debug(s"found best spectra for replacement: RI ${value.retentionIndex}, mass,int: ${ion} ")

    val noiseCorrectedValue: Float = if (noise <= ion.intensity) {
      ion.intensity - noise
    } else {
      //      logger.warn(s"selected ion's intensity is lower than noise, skipping noise subtraction")
      ion.intensity
    }

    /**
      * build target object
      */
    new ZeroreplacedTarget(value, noiseCorrectedValue, needsReplacement, fileUsedForReplacement = correctedRawData.fileName, ion)
  }

  def getNoiseValue(needsReplacement: QuantifiedTarget[Double], data: Seq[Double]): Int = {
    try {
      val h = Distribution(100, data.toList).histogram
      val noiseInts = h.map { it => (it.size, it) }.sortBy(-_._1).take(2)
      val summed = noiseInts.foldLeft((0, 0.0)) { case ((accA, accB), (a, b)) => (accA + a, accB + b.sum) }
      val noise = (summed._2 / summed._1).toInt

      logger.debug(s"noise is: ${noise} for target: ${needsReplacement}")
      noise
    } catch {
      case _: Exception => 0
    }
  }

  /**
    *
    * @param receivedTarget
    * @param filterByMass
    * @param spectra
    * @return
    */
  private def includeMass(receivedTarget: QuantifiedTarget[Double], filterByMass: Filter[AccurateMassSupport], spectra: Feature with CorrectedSpectra) = {
    val ion = MassAccuracy.findClosestIon(spectra, receivedTarget.precursorMass.get, receivedTarget)

    if (ion.isDefined) {
      filterByMass.include(new AccurateMassSupport {
        override def accurateMass: Option[Double] = Some(ion.get.mass)
      }, applicationContext)
    }
    else {
      false
    }
  }
}

case class Distribution(binWidth: Int, data: List[Double]) {
  require(0 < binWidth, "BinWidth must be > 0")

  val (max: Double, min: Double) = data match {
    case d if d.nonEmpty => (data.max, data.min)
    case d => (0, 0)
  }

  val binCount: Double = (max - min) / binWidth
  val bounds: List[Double] = (1 to binWidth).map { x => 0.0 + binWidth * x }.toList

  def histo(bounds: List[Double], data: List[Double]): List[List[Double]] =
    bounds match {
      case h if h.isEmpty => List(data)
      case h :: t => val (l, r) = data.partition(_ < h); l :: histo(t, r)
    }

  val histogram: List[List[Double]] = histo(bounds, data)
}
