package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import org.springframework.beans.factory.annotation.Autowired
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, Target}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

/**
  * annotates the spectra against the given library hits
  */
abstract class AnnotationProcess[T <: Target, I <: Sample, O <: Sample](targets: LibraryAccess[T]) extends Process[I, O]() {

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
  def process(input: I, targets: Iterable[T], method: AcquisitionMethod): O
}

