package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.abf

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.MSDialRestProcessor

/**
  * Created by wohlgemuth on 7/11/17.
  */
class ABFSample(override val fileName:String,file:File, client:MSDialRestProcessor) extends Sample with LazyLogging{

  /**
    * simple wrapper method for the deconvolution process
    * @return
    */

  def deconvolute:Seq[_ <:Feature] = {
    logger.debug("converting to MSDial representation")
	  val start = System.nanoTime()
	  val spec = MSDialSample(fileName, client.process(file)).spectra
	  logger.info(s"preprocess took: ${(System.nanoTime() - start) / 1000000000}s")
	  spec
  }
  /**
    * all the deconvolution spectra for this file. Defined as lazy to reduce memory usage
    * and only do this operation once it's required
    */
   lazy override val spectra: Seq[_ <: Feature] = deconvolute
}
