package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import edu.ucdavis.fiehnlab.ms.carrot.core.api.acquisition.AcquisitionLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.exception.AcquisitionMethodNotFoundErrorForSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, Target}
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired

/**
  * annotates the spectra against the given library hits
  */
abstract class AnnotationProcess[T <: Target, I <: Sample, O <: Sample](targets: LibraryAccess[T], trackChanges: Boolean) extends Process[I, O]() {

  @Autowired
  val acquisitionLoader: AcquisitionLoader = null

  /**
    * allows for easy spring batch process
    *
    * @param input
    * @return
    */
  final override def doProcess(input: I): O = {
    val method = acquisitionLoader.load(input)
    if (method.isDefined) {
      process(input, targets.load(method.get).filter(_.confirmed))
    }
    else {
      throw new AcquisitionMethodNotFoundErrorForSample(input)
    }
  }

  /**
    * subclasses need to overwrite this method with the exact wished behavior
    *
    * @param input
    * @return
    */
  def process(input: I, targets: Iterable[T]): O
}

/**
  * @tparam I
  * @tparam O
  */
abstract class Process[I <: Sample, O <: Sample]() extends ItemProcessor[I, O] {

  /**
    * processes the data
    *
    * @param item
    * @return
    */
  final override def process(item: I): O = {
    val result: O = doProcess(item)
    result
  }

  /**
    * actually processes the item (implementations in subclasses)
    *
    * @param item
    * @return
    */
  def doProcess(item: I): O

  /**
    * the priority of the process
    *
    * @return
    */
  def priortiy: Int = 0
}

