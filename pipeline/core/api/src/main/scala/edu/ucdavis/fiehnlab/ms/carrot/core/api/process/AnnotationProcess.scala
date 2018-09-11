package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.MergeLibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, Target}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService

/**
  * annotates the spectra against the given library hits
  */
abstract class AnnotationProcess[I <: Sample, O <: Sample](targets: MergeLibraryAccess, stasisClient: StasisService) extends Process[I, O]() {

  /**
    * allows for easy spring batch process
    *
    * @param input
    * @return
    */
  final override def doProcess(input: I, method: AcquisitionMethod, rawSample: Option[Sample]): O = {
    process(input, targets.load(method).filter(_.confirmed), method)
  }

  /**
    * subclasses need to overwrite this method with the exact wished behavior
    *
    * @param input
    * @return
    */
  def process(input: I, targets: Iterable[Target], method: AcquisitionMethod): O
}

