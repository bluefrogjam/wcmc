package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection

/**
  * Created by sajjan on 7/18/16.
  */
trait Peak {
  val scanNumber: Int
  val retentionTimeInMinutes: Double
  val maxMass: Double
  val sumIntensity: Double
}

case class FocusedPeak(scanNumber: Int,
                       retentionTimeInMinutes: Double,
                       maxMass: Double,
                       sumIntensity: Double
                      ) extends Peak

//case class SaturatedPeak(scanNumber: Int,
//                        retentionTimeInMinutes: Double,
//                        maxMass: Double,
//                        sumIntensity: Double
//                       ) extends Peak


//case class ExcludedMass(annotation: String,
//                        excludedMass: Double,
//                        massTolerance: Double
//                       )

//case class ChromatographicPeak(scanNumber: Int,
//                               retentionTimeInMinutes: Double,
//                               maxMass: Double,
//                               sumIntensity: Double,
//                               peakQuality: String,
//                               isIsotopeFragment: Boolean
//                              ) extends Peak

trait Detected {
  val peakId: Int

  val scanNumAtLeftPeakEdge: Int
  val scanNumAtRightPeakEdge: Int
  val scanNumAtPeakTop: Int

  val intensityAtLeftPeakEdge: Double
  val intensityAtRightPeakEdge: Double
  val intensityAtPeakTop: Double

  val areaAboveZero: Double
  val areaAboveBaseline: Double

  val rtAtLeftPeakEdge: Double
  val rtAtRightPeakEdge: Double
  val rtAtPeakTop: Double

  val peakPureValue: Double
  val shapenessValue: Double
  val gaussianSimilarityValue: Double
  val idealSlopeValue: Double
  val basePeakValue: Double
  val symmetryValue: Double
  val amplitudeScoreValue: Double
  val amplitudeOrderValue: Double
}

case class PeakCandidate(peak: Peak,
                         firstDiff: Double,
                         secondDiff: Double) extends Peak {

  override val scanNumber: Int = peak.scanNumber
  override val retentionTimeInMinutes: Double = peak.retentionTimeInMinutes
  override val maxMass: Double = peak.maxMass
  override val sumIntensity: Double = peak.sumIntensity
}

case class DetectedPeak(peak: Peak,

                        peakId: Int,

                        scanNumAtLeftPeakEdge: Int,
                        scanNumAtRightPeakEdge: Int,
                        scanNumAtPeakTop: Int,

                        intensityAtLeftPeakEdge: Double,
                        intensityAtRightPeakEdge: Double,
                        intensityAtPeakTop: Double,

                        areaAboveZero: Double,
                        areaAboveBaseline: Double,

                        rtAtLeftPeakEdge: Double,
                        rtAtRightPeakEdge: Double,
                        rtAtPeakTop: Double,

                        peakPureValue: Double,
                        shapenessValue: Double,
                        gaussianSimilarityValue: Double,
                        idealSlopeValue: Double,
                        basePeakValue: Double,
                        symmetryValue: Double,
                        amplitudeScoreValue: Double,
                        amplitudeOrderValue: Double
                       ) extends Peak with Detected {

  override val scanNumber: Int = peak.scanNumber
  override val retentionTimeInMinutes: Double = peak.retentionTimeInMinutes
  override val maxMass: Double = peak.maxMass
  override val sumIntensity: Double = peak.sumIntensity
}

case class DetectedPeakArea(detectedPeak: DetectedPeak,

                            normalizedValue: Double,

                            ms1IsotopicIonM1PeakHeight: Double,
                            ms1IsotopicIonM2PeakHeight: Double,

                            metaboliteName: String,
                            adductIonName: String,
                            adductParent: Int,
                            adductIonAccurateMass: Double,
                            adductIonXmer: Int,
                            adductIonChargeNumber: Int,
                            isotopeWeightNumber: Int,
                            isotopeParentPeakID: Int,
                            accurateMass: Double,
                            accurateMassSimilarity: Double,
                            rtSimilarityValue: Double,
                            totalSimilarityValue: Double,
                            alignedRetentionTime: Double,
                            isotopeSimilarityValue: Double,
                            massSpectraSimilarityValue: Double,
                            reverseSearchSimilarityValue: Double,
                            presenseSimilarityValue: Double,
                            ms1LevelDatapointNumber: Int,
                            ms2LevelDatapointNumber: Int,
                            libraryID: Int,
                            postIdentificationLibraryId: Int,
                            deconvolutionID: Int,

                            amplitudeRatioSimilarityValue: Double,
                            peakTopDifferentialValue: Double,
                            peakShapeSimilarityValue: Double
                           ) extends Peak with Detected {

  override val scanNumber: Int = detectedPeak.scanNumber
  override val retentionTimeInMinutes: Double = detectedPeak.retentionTimeInMinutes
  override val maxMass: Double = detectedPeak.maxMass
  override val sumIntensity: Double = detectedPeak.sumIntensity

  override val peakId: Int = detectedPeak.peakId

  override val scanNumAtLeftPeakEdge: Int = detectedPeak.scanNumAtLeftPeakEdge
  override val scanNumAtRightPeakEdge: Int = detectedPeak.scanNumAtRightPeakEdge
  override val scanNumAtPeakTop: Int = detectedPeak.scanNumAtPeakTop

  override val intensityAtLeftPeakEdge: Double = detectedPeak.intensityAtLeftPeakEdge
  override val intensityAtRightPeakEdge: Double = detectedPeak.intensityAtRightPeakEdge
  override val intensityAtPeakTop: Double = detectedPeak.intensityAtPeakTop

  override val areaAboveZero: Double = detectedPeak.areaAboveZero
  override val areaAboveBaseline: Double = detectedPeak.areaAboveBaseline

  override val rtAtLeftPeakEdge: Double = detectedPeak.rtAtLeftPeakEdge
  override val rtAtRightPeakEdge: Double = detectedPeak.rtAtRightPeakEdge
  override val rtAtPeakTop: Double = detectedPeak.rtAtPeakTop

  override val peakPureValue: Double = detectedPeak.peakPureValue
  override val shapenessValue: Double = detectedPeak.shapenessValue
  override val gaussianSimilarityValue: Double = detectedPeak.gaussianSimilarityValue
  override val idealSlopeValue: Double = detectedPeak.idealSlopeValue
  override val basePeakValue: Double = detectedPeak.basePeakValue
  override val symmetryValue: Double = detectedPeak.symmetryValue
  override val amplitudeScoreValue: Double = detectedPeak.amplitudeScoreValue
  override val amplitudeOrderValue: Double = detectedPeak.amplitudeOrderValue
}
