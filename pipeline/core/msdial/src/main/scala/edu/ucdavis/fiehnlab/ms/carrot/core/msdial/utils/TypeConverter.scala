package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils

import java.util

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Sample}

import scala.collection.JavaConverters._

object TypeConverter {

  /**
    * Converts the Sample spectra Seq to a Java List
    *
    * @param sample
    * @return
    */
  def getJavaSpectrumList(sample: Sample): util.List[_ <: Feature] = {
    sample.spectra.collect {
      case spectrum: Feature =>
        if(spectrum.associatedScan.get.msLevel == 1) {
          spectrum
        } else {
          spectrum.asInstanceOf[MSMSSpectra]
        }
    }.asJava
  }

  /**
    * Converts the Feature ion Seq to a Java List
    *
    * @param spectrum
    * @return
    */
  def getJavaIonList(spectrum: Feature): util.List[Ion] = spectrum.associatedScan.get.ions.asJava
}
