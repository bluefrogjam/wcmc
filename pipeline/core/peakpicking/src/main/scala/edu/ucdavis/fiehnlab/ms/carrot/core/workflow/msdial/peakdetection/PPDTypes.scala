package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection

import java.util.{List => JList}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.RawSpectrum

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 10/14/16.
  */
class PeakDetectedSample(sample: Sample, peaks: JList[_ <: MSSpectra], rawData: JList[RawSpectrum]) extends Sample {
  val rawSpectra: JList[RawSpectrum] = rawData
  override val fileName: String = sample.fileName
  override lazy val spectra: Seq[_ <: MSSpectra] = peaks.asScala.asInstanceOf[Seq[_ <: MSSpectra]]
}

trait DeconvolutedSample extends Sample

trait DeconvolutedSpectrum extends MSSpectra