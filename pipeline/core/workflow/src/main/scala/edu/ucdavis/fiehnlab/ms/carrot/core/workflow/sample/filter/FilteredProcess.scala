package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.Process
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{ProcessedSample, Sample}
import org.springframework.beans.factory.annotation.Autowired

/**
  * Defines a process, which filters certain spectra out of the sample
  */
class FilteredProcess @Autowired()(val filters: List[Filter[Feature]]) extends Process[Sample, ProcessedSample] {

  /**
    * iterates over all the defiend filters and removes certain data from the sample
    * a new sample will be returned
    *
    * @param sample
    * @return
    */
  override def doProcess(sample: Sample): ProcessedSample = {

    /**
      * iterate over all the spectra and try to filter the provided spectra
      * if any filter returns false, the spectra will be rejected at this stage
      */
    val result: Seq[Feature] = sample.spectra.filter { spectra =>

      //there has to be a better way to make this work
      var reject = true

      filters.foreach { filter =>
        if (filter.exclude(spectra)) {
          reject = false
        }
      }
      reject
    }

    /**
      * returns our processed result
      */
    new ProcessedSample {
      override val spectra: Seq[_ <: Feature] = result
      override val fileName: String = sample.fileName
    }
  }
}
