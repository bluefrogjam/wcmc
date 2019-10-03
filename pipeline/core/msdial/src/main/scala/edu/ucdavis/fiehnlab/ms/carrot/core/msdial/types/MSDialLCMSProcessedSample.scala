package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.lcms.MS2DeconvolutionResult

import scala.collection.JavaConverters._


object MSDialLCMSProcessedSample {

  def generateSample(ms2DecResults: java.util.List[MS2DeconvolutionResult], _fileName: String, mode: IonMode): Sample = {

    val _spectra = generateSpectra(ms2DecResults, _fileName, mode)
    new Sample {
      /**
        * a collection of spectra
        * belonging to this sample
        */
      override val spectra: Seq[_ <: Feature] = _spectra
      /**
        * the unique file name of the sample
        */
      override val fileName: String = _fileName
      /**
        * associated properties
        */
      override val properties: Option[SampleProperties] = None
    }
  }

  def generateSpectra(ms2DecResults: java.util.List[MS2DeconvolutionResult], fileName: String, mode: IonMode) = {
    ms2DecResults.asScala.map { x: MS2DeconvolutionResult =>

      val _fileName = fileName
      val _scanNumber = x.peakTopScan
      val _retentionTimeInSecods = x.peakTopRetentionTime
      val _massOfDetectedFeature = Option(Ion(x.peak.accurateMass, x.peak.intensityAtPeakTop))
      val _properties = Some(new SpectrumProperties {
        override val msLevel: Short = 1
        override val modelIons: Option[List[Double]] = None
        override val ions: Seq[Ion] = x.ms1Spectrum.asScala
        override val rawIons: Option[Seq[Ion]] = None
      })

      if (x.peak.ms2LevelDataPointNumber == -1) {

        val _metadata = Map(
          "baseChromatogram" -> Some(x.baseChromatogram.asScala),
          "modelMasses" -> Some(x.modelMasses.asScala),
          "ms1AccurateMass" -> Some(x.ms1AccurateMass),
          "mPlus1Height" -> Some(x.ms1IsotopicIonM1PeakHeight),
          "mPlus2Height" -> Some(x.ms1IsotopicIonM2PeakHeight),
          "peakHeight" -> Some(x.ms1PeakHeight),
          "peakRTmin" -> Some(x.peakTopRetentionTime),
          "uniqueMass" -> Some(x.uniqueMs),
          "peak" -> Map[String, AnyRef](
            "id" -> Some(x.peak.peakID),
            "accurateMass" -> Some(x.peak.accurateMass),
            "amplitudeOrderValue" -> Some(x.peak.amplitudeOrderValue),
            "amplitudeScoreValue" -> Some(x.peak.amplitudeScoreValue),
            "areaAboveBaseline" -> Some(x.peak.areaAboveBaseline),
            "areaAboveZero" -> Some(x.peak.areaAboveZero),
            "basePeakValue" -> Some(x.peak.basePeakValue),
            "charge" -> Some(x.peak.chargeNumber),
            "gaussianSimilarityValue" -> Some(x.peak.gaussianSimilarityValue),
            "idealSlopeValue" -> Some(x.peak.idealSlopeValue),
            "normalizedValue" -> Some(x.peak.normalizedValue)
          ))

        new MSSpectra {
          //        logger.info(s"creating MS peak")
          override val uniqueMass: Option[Double] = None
          override val signalNoise: Option[Double] = None
          override val ionMode: Option[IonMode] = Option(mode)
          override val purity: Option[Double] = None
          override val sample: String = _fileName
          override val retentionTimeInSeconds: Double = _retentionTimeInSecods
          override val scanNumber: Int = _scanNumber
          override val massOfDetectedFeature: Option[Ion] = _massOfDetectedFeature
          override val associatedScan: Option[SpectrumProperties] = _properties
          override val metadata: Map[String, AnyRef] = _metadata
        }
      } else {

        val _accurateMass = x.ms1AccurateMass
        val _precursorScan = Some(new SpectrumProperties {
          override val msLevel: Short = 1
          override val modelIons: Option[List[Double]] = None
          override val ions: Seq[Ion] = x.ms1Spectrum.asScala
          override val rawIons: Option[Seq[Ion]] = None
        })

        val _associatedScan = Some(new SpectrumProperties {
          override val msLevel: Short = 2
          override val modelIons: Option[List[Double]] = None
          override val ions: Seq[Ion] = x.ms2Spectrum.asScala.map(x => Ion(x.mz, x.intensity))
          override val rawIons: Option[Seq[Ion]] = Some(x.rawMS2Spectrum.asScala)
        })

        new MSMSSpectra {
          override val uniqueMass: Option[Double] = None
          override val signalNoise: Option[Double] = None
          override val precursorIon: Double = _accurateMass
          override val ionMode: Option[IonMode] = Option(mode)
          override val purity: Option[Double] = None
          override val sample: String = _fileName
          override val retentionTimeInSeconds: Double = _retentionTimeInSecods
          override val scanNumber: Int = _scanNumber
          override val massOfDetectedFeature: Option[Ion] = _massOfDetectedFeature
          override val precursorScan: Option[SpectrumProperties] = _precursorScan
          override val associatedScan: Option[SpectrumProperties] = _associatedScan
          /**
            * Contains random metadata associated to the object we mix this into
            */
          override val metadata: Map[String, AnyRef] = Map()
        }
      }
    }
  }
}

