package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk

import java.io.{File, FileInputStream, FileOutputStream}
import java.util.zip.GZIPInputStream

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{SpectrumProperties, _}
import edu.ucdavis.fiehnlab.ms.carrot.core.exception.UnsupportedSampleException
import io.github.msdk.datamodel.{MsScan, MsSpectrumType, PolarityType, RawDataFile}
import io.github.msdk.io.mzdata.MzDataFileImportMethod
import io.github.msdk.io.mzml.MzMLFileImportMethod
import io.github.msdk.io.mzxml.MzXMLFileImportMethod
import io.github.msdk.io.netcdf.NetCDFFileImportMethod
import org.apache.commons.io.IOUtils

import scala.collection.JavaConverters._

/**
  * this provides us with an easy way to utilized MSDK based data classes in our simplified schema
  */
class MSDKSample(name: String, delegate: RawDataFile) extends Sample with Logging with RawData {

  override val properties: Option[SampleProperties] = Some(SampleProperties(name, None))

  /**
    * a collection of spectra
    * belonging to this sample
    */
    //TODO Watch for race conditions if reading samples in parallel
  override lazy val spectra: Seq[_ <: Feature] = try {
    var precursor: MSDKMSSpectra = null
    delegate.getScans.asScala.filter(_.getIntensityValues.nonEmpty).map {

      //test all ms scans
      spectra: MsScan =>
        val polarity = if (spectra.getPolarity == PolarityType.NEGATIVE) NegativeMode() else PositiveMode()

        if (spectra.getMsLevel == 0) {
          throw new RuntimeException("Invalid MS Level!")
        } else if (spectra.getMsLevel == 1 || spectra.getIsolations.isEmpty) {

          //discover which mixins we need
          precursor = spectra.getSpectrumType match {
            case MsSpectrumType.CENTROIDED => new MSDKMSSpectra(spectra, Some(polarity), this.fileName) with Centroided
            case MsSpectrumType.PROFILE => new MSDKMSSpectra(spectra, Some(polarity), this.fileName) with Profiled
            case _ => {
              logger.warn("Unrecognized spectrum type, setting to profiled")
              new MSDKMSSpectra(spectra, Some(polarity), this.fileName) with Profiled
            }
          }

          precursor
        } else {
          new MSDKMSMSSpectra(spectra, Some(polarity), this.fileName, precursor.associatedScan)
        }

    }
  } finally {
    delegate.dispose()
  }

  /**
    * the unique file name of the sample
    */
  override val fileName: String = name

  /**
    * disposing of the delegate
    */
}


object MSDKSample extends Logging {
  /**
    * read from a rawdata file directly
    *
    * @param rawDataFile
    * @return
    */
  def apply(name: String, rawDataFile: RawDataFile): MSDKSample = new MSDKSample(name, rawDataFile)

  /**
    * simple factory to find the rawdata delegate for us
    *
    * @param file
    * @return
    */
  def apply(originalName: String, file: File): MSDKSample = {


    var output = file

    if (file.getName.endsWith(".gz")) {
      logger.debug(s"${originalName}, located at ${file.getAbsolutePath} needs to be uncompressed")
      val name = file.getName.replaceFirst(".gz", "")
      val dir = new File(System.getProperty("java.io.tmpdir"))

      output = new File(dir, name)

      if (!output.exists()) {
        val in = new GZIPInputStream(new FileInputStream(file))
        val out = new FileOutputStream(output)
        IOUtils.copy(in, out)
        out.flush()
        out.close()
        in.close()
        output.deleteOnExit()
      }

    }

    logger.debug(s"attempting to load ${originalName} from ${output.getAbsolutePath}")
    val name = output.getName.toLowerCase.substring(file.getName.indexOf(".") + 1)

    new MSDKSample(
      originalName,

      //find our internal implementation
      name match {
        case "mzxml" =>
          logger.debug("using mzXML implementation")
          new MzXMLFileImportMethod(output).execute()
        case "mzml" =>
          logger.debug("using mzML implementation")
          new MzMLFileImportMethod(output).execute()
        case "mzdata" =>
          logger.debug("using mzData implementation")
          new MzDataFileImportMethod(output).execute()
        case "mzdata.xml" =>
          logger.debug("using mzData implementation")
          new MzDataFileImportMethod(output).execute()
        case "cdf" =>
          logger.debug("using cdf implementation")
          new NetCDFFileImportMethod(output).execute()
        case _ =>
          throw new UnsupportedSampleException(s"sorry this file format is not yet supported: ${file}/${output}, extension ${name}")
      }
    )

  }

  /**
    * generates the ion scan, based on the given spectra
    *
    * @param spectra
    * @return
    */
  def build(spectra: MsScan): Seq[Ion] = {
    val data = spectra.getMzValues.zip(spectra.getIntensityValues).map {
      ion: (Double, Float) => Ion(ion._1, ion._2)
    }
    assert(data.nonEmpty)
    data
  }

}


/**
  * an msdk ms spectra
  *
  * @param spectra
  */
class MSDKMSSpectra(spectra: MsScan, mode: Option[IonMode], val sample: String) extends MSSpectra {
  override val retentionTimeInSeconds: Double = spectra.getRetentionTime.toDouble
  override val uniqueMass: Option[Double] = None
  override val signalNoise: Option[Double] = None

  override val scanNumber: Int = spectra.getScanNumber
  override val purity: Option[Double] = None
  override val ionMode: Option[IonMode] = mode
  override val massOfDetectedFeature: Option[Ion] = None

  /**
    * associated spectrum propties if applicable
    */
  override val associatedScan: Option[SpectrumProperties] = Some(new SpectrumProperties {
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[List[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override lazy val ions: Seq[Ion] = MSDKSample.build(spectra)

    override val msLevel: Short = 1
  })
}

/**
  * an msdk msms spectra
  *
  * @param spectra
  */
class MSDKMSMSSpectra(spectra: MsScan, mode: Option[IonMode], val sample: String, val precursor: Option[SpectrumProperties]) extends MSMSSpectra {
  override val precursorIon: Double = if (spectra.getIsolations.isEmpty) {
    0.0 //this is just bad, but seems to be a real value in some files
  } else {
    spectra.getIsolations.get(0).getPrecursorMz
  }
  override val uniqueMass: Option[Double] = None
  override val signalNoise: Option[Double] = None

  override val retentionTimeInSeconds: Double = spectra.getRetentionTime.toDouble
  override val scanNumber: Int = spectra.getScanNumber
  override val purity: Option[Double] = None
  override val ionMode: Option[IonMode] = mode
  override val massOfDetectedFeature: Option[Ion] = MSDKSample.build(spectra).find { x =>
    if (spectra.getIsolations.isEmpty) {
      false
    }
    else {
      x.mass == spectra.getIsolations.get(0).getPrecursorMz
    }
  }

  override val associatedScan: Option[SpectrumProperties] = Some(new SpectrumProperties {
    override val modelIons: Option[List[Double]] = None
    override lazy val ions: Seq[Ion] = MSDKSample.build(spectra)
    override val msLevel: Short = 2
  })

  override val precursorScan: Option[SpectrumProperties] = precursor
}
