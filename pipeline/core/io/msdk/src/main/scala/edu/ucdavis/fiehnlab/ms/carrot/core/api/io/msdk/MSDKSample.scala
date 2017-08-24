package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.Files._
import java.nio.file.Paths
import java.util.zip.GZIPInputStream

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import io.github.msdk.datamodel.datastore.DataPointStoreFactory
import io.github.msdk.datamodel.msspectra.MsSpectrumType
import io.github.msdk.datamodel.rawdata.{MsScan, PolarityType, RawDataFile}
import io.github.msdk.io.mzdata.MzDataFileImportMethod
import io.github.msdk.io.mzml.MzMLFileImportMethod
import io.github.msdk.io.mzxml.MzXMLFileImportMethod
import io.github.msdk.io.netcdf.NetCDFFileImportMethod
import org.apache.commons.io.IOUtils

import scala.collection.JavaConverters._

/**
  * this provides us with an easy way to utilized MSDK based data classes in our simplified schema
  */
class MSDKSample(name: String, delegate: RawDataFile) extends Sample with LazyLogging {


  /**
    * a collection of spectra
    * belonging to this sample
    */
  override val spectra: Seq[_ <: Feature] = try {
    delegate.getScans.asScala.filter(_.getIntensityValues.nonEmpty).map {

      //test all ms scans
      spectra: MsScan =>

        logger.debug(s"reading scan: ${spectra}")
        if (spectra.getMsFunction.getMsLevel == 0) {
          throw new RuntimeException("Invalid MS Level!")
        }
        else if (spectra.getMsFunction.getMsLevel == 1) {

          //discover which mixins we need
          spectra.getPolarity match {
            case PolarityType.NEGATIVE =>
              spectra.getSpectrumType match {
                case MsSpectrumType.CENTROIDED => new MSDKMSSpectra(spectra, Some(NegativeMode())) with Centroided
                case MsSpectrumType.PROFILE => new MSDKMSSpectra(spectra, Some(NegativeMode())) with Profiled
                case _ => {
                  logger.warn("Unrecognized spectrum type, setting to profiled")
                  new MSDKMSSpectra(spectra, Some(NegativeMode())) with Profiled
                }
              }
            case PolarityType.POSITIVE =>
              new MSDKMSSpectra(spectra, Some(PositiveMode()))
            case _ =>
              new MSDKMSSpectra(spectra, None)
          }
        }
        else {
          //discover which mixins we need
          spectra.getPolarity match {
            case PolarityType.NEGATIVE =>
              new MSDKMSMSSpectra(spectra, Some(NegativeMode()))
            case PolarityType.POSITIVE =>
              new MSDKMSMSSpectra(spectra, Some(PositiveMode()))
            case _ =>
              new MSDKMSMSSpectra(spectra, None)
          }
        }

    }
  }
  finally {
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


object MSDKSample extends LazyLogging {
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

    logger.debug(s"checking if ${originalName}, which is located at ${file.getAbsolutePath} needs to be uncompressed")

    var output = file

    if (file.getName.endsWith(".gz")) {
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

    logger.debug(s"attempting to load ${originalName} which is located at ${output.getAbsolutePath}")
    val name = output.getName.toLowerCase.substring(file.getName.indexOf(".") + 1)

    new MSDKSample(
      originalName,

      //find our internal implementation
      name match {
        case "mzxml" =>
          logger.debug("using mzXML implementation")
          new MzXMLFileImportMethod(output, DataPointStoreFactory.getMemoryDataStore).execute()
        case "mzml" =>
          logger.debug("using mzML implementation")
          new MzMLFileImportMethod(output).execute()
        case "mzdata" =>
          logger.debug("using mzData implementation")
          new MzDataFileImportMethod(output, DataPointStoreFactory.getMemoryDataStore).execute()
        case "mzdata.xml" =>
          logger.debug("using mzData implementation")
          new MzDataFileImportMethod(output, DataPointStoreFactory.getMemoryDataStore).execute()
        case "cdf" =>
          logger.debug("using cdf implementation")
          new NetCDFFileImportMethod(output, DataPointStoreFactory.getMemoryDataStore).execute()
        case _ =>
          throw new RuntimeException(s"sorry this file format is not yet supported: ${file}/${output}, extension ${name}")
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
class MSDKMSSpectra(spectra: MsScan, mode: Option[IonMode]) extends MSSpectra {
  override val retentionTimeInSeconds: Double = spectra.getChromatographyInfo.getRetentionTime.toDouble

  override val scanNumber: Int = spectra.getScanNumber
  override val purity: Option[Double] = None
  override val ionMode: Option[IonMode] = mode
  override val massOfDetectedFeature: Option[Ion] = None
  /**
    * associated spectrum propties if applicable
    */
  override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[List[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = MSDKSample.build(spectra)

    override val msLevel: Short = 1
  })
}

/**
  * an msdk msms spectra
  *
  * @param spectra
  */
class MSDKMSMSSpectra(spectra: MsScan, mode: Option[IonMode]) extends MSMSSpectra {
  override val precursorIon: Double = spectra.getIsolations.get(0).getPrecursorMz
  override val retentionTimeInSeconds: Double = spectra.getChromatographyInfo.getRetentionTime.toDouble
  override val scanNumber: Int = spectra.getScanNumber
  override val purity: Option[Double] = None
  override val ionMode: Option[IonMode] = mode
  override val massOfDetectedFeature: Option[Ion] = MSDKSample.build(spectra).find(_.mass == spectra.getIsolations.get(0).getPrecursorMz)
  /**
    * associated spectrum propties if applicable
    */
  override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[List[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = MSDKSample.build(spectra)

    override val msLevel: Short = 2
  })
}