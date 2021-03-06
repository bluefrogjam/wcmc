package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.agilent

import java.io.File

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, SampleProperties}
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import org.apache.logging.log4j.scala.Logging

/**
  * supports loading of .d files and compressed .d files as carrot samples. Please be aware that this includes a lot of network transfers
  * due to the utilization of the dataform client
  */
class AgilentSample(override val fileName: String, file: File, dataFormerClient: DataFormerClient) extends Sample with Logging {

  def deconvolute: Seq[_ <: Feature] = {
    logger.info(s"converting ${file} to mzML representation")
    val start = System.nanoTime()

    try {
      val result = dataFormerClient.convert(fileName, "mzml")
      logger.info(s"converting ${file} to msDialV2 representation")

      MSDKSample(fileName, result.get).spectra
    } catch {
      case e: Exception => logger.error(s" Exception creating Agilent sample: ${e.getMessage}"); Seq.empty
    }
  }

  /**
    * all the deconvolution spectra for this file. Defined as lazy to reduce memory usage
    * and only do this operation once it's required
    */
  lazy override val spectra: Seq[_ <: Feature] = deconvolute

  override val properties: Option[SampleProperties] = Some(SampleProperties(fileName, None))
}

