package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types

import java.util

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra, MetadataSupport, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.lcms.MS2DeconvolutionResult

import scala.collection.JavaConverters._


class MSDialLCMSProcessedSample(ms2DecResults: util.List[MS2DeconvolutionResult], mode: IonMode, override val fileName: String) extends ProcessedSample with Logging {

  override val properties: Option[SampleProperties] = None

  override val spectra: Seq[_ <: Feature] = ms2DecResults.asScala.map { x: MS2DeconvolutionResult =>
    if (x.peak.ms2LevelDataPointNumber == -1) {
      new Feature with MetadataSupport {
        override val uniqueMass: Option[Double] = None
        override val signalNoise: Option[Double] = None

        /**
          * specified ion mode for the given feature
          */
        override val ionMode: Option[IonMode] = Option(mode)

        /**
          * how pure this spectra is
          */
        override val purity: Option[Double] = None

        /**
          * the associated sample
          */
        override val sample: String = MSDialLCMSProcessedSample.this.fileName

        /**
          * the retention time of this spectra. It should be provided in seconds!
          */
        override val retentionTimeInSeconds: Double = x.peakTopRetentionTime * 60

        /**
          * the local scan number
          */
        override val scanNumber: Int = x.peakTopScan

        /**
          * accurate mass of this feature, if applicable
          */
        override val massOfDetectedFeature: Option[Ion] = Option(Ion(x.peak.accurateMass, x.peak.intensityAtPeakTop))

        override val associatedScan: Option[SpectrumProperties] = Some(new SpectrumProperties {

          override val msLevel: Short = 1

          override val modelIons: Option[List[Double]] = None

          override val ions: Seq[Ion] = x.ms1Spectrum.asScala
        })

        /**
          * add metadata from peak picking and deconvolution
          */
        override val metadata: Map[String, AnyRef] = Map(
            "baseChromatogram" -> Some(x.baseChromatogram.asScala),
            "modelMasses" -> Some(x.modelMasses.asScala),
            "ms1AccurateMass" -> Some(x.ms1AccurateMass),
            "mPlus1Height" -> Some(x.ms1IsotopicIonM1PeakHeight),
            "mPlus2Height" -> Some(x.ms1IsotopicIonM2PeakHeight),
            "peakHeight" -> Some(x.ms1PeakHeight),
            "peakRTmin" -> Some(x.peakTopRetentionTime),
            "uniqueMass" -> Some(x.uniqueMs),
            "peak" -> Map[String, AnyRef] (
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
      }
    } else {
      new MSMSSpectra {
        override val uniqueMass: Option[Double] = None
        override val signalNoise: Option[Double] = None

        /**
          * the observed pre cursor ion
          */
        override val precursorIon: Double = x.ms1AccurateMass

        /**
          * specified ion mode for the given feature
          */
        override val ionMode: Option[IonMode] = Option(mode)

        /**
          * how pure this spectra is
          */
        override val purity: Option[Double] = None

        /**
          * the associated sample
          */
        override val sample: String = MSDialLCMSProcessedSample.this.fileName

        /**
          * the retention time of this spectra. It should be provided in seconds!
          */
        override val retentionTimeInSeconds: Double = x.peakTopRetentionTime * 60

        /**
          * the local scan number
          */
        override val scanNumber: Int = x.peakTopScan

        /**
          * accurate mass of this feature, if applicable
          */
        override val massOfDetectedFeature: Option[Ion] = Option(Ion(x.peak.accurateMass, x.peak.intensityAtPeakTop))

        override val associatedScan: Option[SpectrumProperties] = Some(new SpectrumProperties {

          override val msLevel: Short = 1

          override val modelIons: Option[List[Double]] = None

          override val ions: Seq[Ion] = x.ms1Spectrum.asScala
        })

        override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {

          override val msLevel: Short = 2

          override val modelIons: Option[List[Double]] = None

          override val ions: Seq[Ion] = x.ms2Spectrum.asScala.map(x => Ion(x.mz, x.intensity))
        })
      }
    }
  }
}
