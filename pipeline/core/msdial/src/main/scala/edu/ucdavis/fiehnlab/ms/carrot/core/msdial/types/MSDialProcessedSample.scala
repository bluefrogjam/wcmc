package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, ProcessedSample, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra, SpectrumProperties}

import scala.collection.JavaConverters._


class MSDialProcessedSample(ms2DecResults: util.List[MS2DecResult], mode: IonMode, override val fileName: String) extends ProcessedSample with LazyLogging {

  override val spectra: Seq[_ <: Feature] = ms2DecResults.asScala.map { x: MS2DecResult =>
    if (x.peak.ms2LevelDataPointNumber == -1) {
      new Feature {

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
        override val sample: String = MSDialProcessedSample.this.fileName

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
      }
    } else {
      new MSMSSpectra {

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
        override val sample: String = MSDialProcessedSample.this.fileName

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
