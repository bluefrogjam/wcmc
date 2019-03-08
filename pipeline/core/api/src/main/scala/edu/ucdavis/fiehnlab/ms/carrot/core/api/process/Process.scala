package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.ProcessException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.TrackingData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
  * @tparam I
  * @tparam O
  */
abstract class Process[I <: Sample, O <: Sample]() extends Logging {

  @Autowired
  protected val applicationContext: ApplicationContext = null

  @Autowired
  private val stasisClient: StasisService = null

  /**
    * processes the data
    *
    * @param item
    * @return
    */
  final def process(item: I, method: AcquisitionMethod, rawSample: Option[Sample]): O = {
    try {
      val result: O = doProcess(item, method, rawSample)
      result
    }
    catch {
      case e: ProcessException =>
        stasisClient.addTracking(TrackingData(item.name, "failed", item.fileName, e.message))
        throw e
    }
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
