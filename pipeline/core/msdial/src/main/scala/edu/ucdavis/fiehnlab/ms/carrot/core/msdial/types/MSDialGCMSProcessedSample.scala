package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types

import java.util

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra, SimilaritySupport, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms.MS1DeconvolutionResult

import scala.collection.JavaConverters._


class MSDialGCMSProcessedSample(ms1DecResults: util.List[MS1DeconvolutionResult], mode: IonMode, override val fileName: String) extends ProcessedSample with Logging {

  override val properties: Option[SampleProperties] = None

  override val spectra: Seq[_ <: Feature] = ms1DecResults.asScala.map { x: MS1DeconvolutionResult =>
    new MSSpectra with SimilaritySupport{
      override val uniqueMass: Option[Double] = None
      override val signalNoise: Option[Double] = None

      /**
        * specified ion mode for the given feature
        */
      override val ionMode: Option[IonMode] = Option(mode)

      /**
        * how pure this spectra is
        */
      override val purity: Option[Double] = Option(x.modelPeakPurity)

      /**
        * the associated sample
        */
      override val sample: String = MSDialGCMSProcessedSample.this.fileName

      /**
        * the retention time of this spectra. It should be provided in seconds!
        */
      override val retentionTimeInSeconds: Double = x.retentionTime * 60 *1000

      /**
        * the local scan number
        */
      override val scanNumber: Int = x.scanNumber

      /**
        * accurate mass of this feature, if applicable
        */
      override val massOfDetectedFeature: Option[Ion] = Option(Ion(x.basepeakMz, x.basepeakHeight.toFloat))

      override val associatedScan: Option[SpectrumProperties] = Some(new SpectrumProperties {

        override val msLevel: Short = 1

        override val modelIons: Option[List[Double]] = Option(x.modelMasses.asScala.map(_.toDouble).toList)

        override val ions: Seq[Ion] = x.spectrum.asScala.map(p => Ion(p.mz, p.intensity))
      })
      /**
        * associated spectrum propties if applicable
        */
      override val spectrum: Option[SpectrumProperties] = associatedScan
      /**
        * Contains random metadata associated to the object we mix this into
        */
      override val metadata: Map[String, AnyRef] = Map()
    }
  }
}
