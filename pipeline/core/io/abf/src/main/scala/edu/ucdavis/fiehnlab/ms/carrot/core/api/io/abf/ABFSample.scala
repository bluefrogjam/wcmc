package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.abf

import java.io.File

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j.MSDialRestProcessor

/**
  * Created by wohlgemuth on 7/11/17.
  */
class ABFSample(override val fileName:String,file:File, val client:MSDialRestProcessor) extends Sample{

  /**
    * all the deconvolution spectra for this file. Defined as lazy to reduce memory usage
    * and only do this operation once it's required
    */
  lazy override val spectra: Seq[_ <: Feature] = MSDialSample(fileName,client.process(file)).spectra
}
