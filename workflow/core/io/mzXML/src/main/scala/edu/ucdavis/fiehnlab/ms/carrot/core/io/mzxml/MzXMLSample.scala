package edu.ucdavis.fiehnlab.ms.carrot.core.io.mzxml

import java.io.{BufferedInputStream, InputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Centroided, MSMSSpectra, MSSpectra, Profiled}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, Sample}

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 8/8/16.
  */
class MzXMLSample(inputStream: InputStream, override val fileName: String) extends Sample with LazyLogging {
  lazy override val spectra: Seq[_ <: MSSpectra] = new MzXMLReader(new BufferedInputStream(inputStream)).run().asScala.map {
    spectra: Spectra =>
      if (spectra.msLevel > 1) {
        if (spectra.centroided)
          new MSMSSpectra with Centroided {
            override val precursorIon: Double = spectra.precursor
            override val purity: Option[Double] = None
            override val ionMode: Option[IonMode] = Some(spectra.ionMode)
            override val scanNumber: Int = spectra.scanNumber
            override val ions: Seq[Ion] = spectra.ions
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = spectra.msLevel
            override val retentionTimeInSeconds: Double = spectra.retentionTime
          }
        else
          new MSMSSpectra with Profiled {
            override val precursorIon: Double = spectra.precursor
            override val purity: Option[Double] = None
            override val ionMode: Option[IonMode] = Some(spectra.ionMode)
            override val scanNumber: Int = spectra.scanNumber
            override val ions: Seq[Ion] = spectra.ions
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = spectra.msLevel
            override val retentionTimeInSeconds: Double = spectra.retentionTime
          }
      } else {
        if (spectra.centroided)
          new MSSpectra with Centroided {
            override val purity: Option[Double] = None
            override val ionMode: Option[IonMode] = Some(spectra.ionMode)
            override val scanNumber: Int = spectra.scanNumber
            override val ions: Seq[Ion] = spectra.ions
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = spectra.msLevel
            override val retentionTimeInSeconds: Double = spectra.retentionTime
          }
        else
          new MSSpectra with Profiled {
            override val purity: Option[Double] = None
            override val ionMode: Option[IonMode] = Some(spectra.ionMode)
            override val scanNumber: Int = spectra.scanNumber
            override val ions: Seq[Ion] = spectra.ions
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = spectra.msLevel
            override val retentionTimeInSeconds: Double = spectra.retentionTime
          }
      }
  }
}
