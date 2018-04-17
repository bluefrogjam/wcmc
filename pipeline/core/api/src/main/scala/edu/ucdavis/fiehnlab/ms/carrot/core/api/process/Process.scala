package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
  * @tparam I
  * @tparam O
  */
abstract class Process[I <: Sample, O <: Sample]() {

  @Autowired
  protected val applicationContext: ApplicationContext = null

  /**
    * processes the data
    *
    * @param item
    * @return
    */
  final def process(item: I, method: AcquisitionMethod, rawSample: Option[Sample] = None): O = {
    val result: O = doProcess(item, method, rawSample)
    result
  }

  /**
    * actually processes the item (implementations in subclasses)
    *
    * @param item
    * @return
    */
  def doProcess(item: I, method: AcquisitionMethod, rawSample: Option[Sample]): O

  /**
    * the priority of the process
    *
    * @return
    */
  def priortiy: Int = 0
}
