package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.agilent

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSampleV2
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient

/**
  * supports loading of .d files and compressed .d files as carrot samples. Please be aware that this includes a lot of network transfers
  * due to the utilization of the dataform client
  */
class AgilentSample(override val fileName: String, file: File, dataFormerClient: DataFormerClient) extends Sample with LazyLogging {

  def deconvolute: Seq[_ <: Feature] = {
    logger.debug(s"converting ${file} to mzML representation")
    val start = System.nanoTime()

    val result = dataFormerClient.convert(fileName,"mzML")
    logger.debug(s"converting ${file} to msDialV2 representation")

    MSDKSample(fileName, result.get).spectra
  }

  /**
    * all the deconvolution spectra for this file. Defined as lazy to reduce memory usage
    * and only do this operation once it's required
    */
  lazy override val spectra: Seq[_ <: Feature] = deconvolute
}

