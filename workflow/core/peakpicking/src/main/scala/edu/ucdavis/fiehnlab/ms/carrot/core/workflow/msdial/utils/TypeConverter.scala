package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils

import java.util.{ArrayList => JArrayList, List => JList}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution.MsDialPeak
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.{DeconvolutedSample, DeconvolutedSpectrum}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.{MS1DecResult, RawSpectrum, Ion => MsDialIon}
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._


/**
  * Created by diego on 10/26/2016.
  */
@Component
class TypeConverter() {

  def asCarrotSpectrum(peaks: JList[MS1DecResult], rawSpectra: JList[RawSpectrum]): Seq[MSSpectra] = {

    val decRes = peaks.asScala.map { peak =>
      val raw = rawSpectra.get(peak.scanNumber)
      val rawIonMode = if (raw.polarity == '+') Some(PositiveMode()) else Some(NegativeMode())

      if (raw.centroided)
        if (raw.msLevel == 1)
          new MSSpectra with Centroided with DeconvolutedSpectrum {
            override val purity: Option[Double] = Some(peak.modelPeakPurity)
            override val ionMode: Option[IonMode] = rawIonMode
            override val scanNumber: Int = raw.rawScanNum
            override val ions: Seq[Ion] = asCarrotIons(peak.spectrum)
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = 1
            override val retentionTimeInSeconds: Double = peak.retentionTime * 60
          }
        else
          new MSMSSpectra with Centroided with DeconvolutedSpectrum {
            override val precursorIon = raw.precursorMz
            override val purity: Option[Double] = Some(peak.modelPeakPurity)
            override val ionMode: Option[IonMode] = rawIonMode
            override val scanNumber: Int = raw.rawScanNum
            override val ions: Seq[Ion] = asCarrotIons(peak.spectrum)
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = 1
            override val retentionTimeInSeconds: Double = peak.retentionTime * 60
          }
      else if (raw.msLevel == 1)
        new MSSpectra with Profiled with DeconvolutedSpectrum {
          override val purity: Option[Double] = Some(peak.modelPeakPurity)
          override val ionMode: Option[IonMode] = rawIonMode
          override val scanNumber: Int = raw.rawScanNum
          override val ions: Seq[Ion] = asCarrotIons(peak.spectrum)
          override val modelIons: Option[Seq[Double]] = None
          override val msLevel: Short = raw.msLevel.asInstanceOf[Short]
          override val retentionTimeInSeconds: Double = peak.retentionTime * 60
        }
      else
        new MSMSSpectra with Profiled with DeconvolutedSpectrum {
          override val precursorIon = raw.precursorMz
          override val purity: Option[Double] = Some(peak.modelPeakPurity)
          override val ionMode: Option[IonMode] = rawIonMode
          override val scanNumber: Int = raw.rawScanNum
          override val ions: Seq[Ion] = asCarrotIons(peak.spectrum)
          override val modelIons: Option[Seq[Double]] = None
          override val msLevel: Short = raw.msLevel.asInstanceOf[Short]
          override val retentionTimeInSeconds: Double = peak.retentionTime * 60
        }
    }.asInstanceOf[Seq[_ <: MSSpectra]]

    decRes
  }

//  def asCarrotIons(ions: JList[MsDialIon]): Seq[Ion] = {
//    ions.asScala.map(ion => Ion(ion.mass, ion.intensity.toFloat))
//  }

  def asCarrotIons(ions: JList[MsDialPeak]): Seq[Ion] = {
    ions.asScala.map(ion => Ion(ion.mass, ion.intensity.toFloat))
  }

  def asCarrotDeconvolutedSample(peaks: JList[MS1DecResult], rawSpectra: JList[RawSpectrum], sample: Sample): DeconvolutedSample = {
    new DeconvolutedSample {
      override val spectra: Seq[_ <: MSSpectra] = asCarrotSpectrum(peaks, rawSpectra)
      override val fileName: String = sample.fileName
    }
  }
}
