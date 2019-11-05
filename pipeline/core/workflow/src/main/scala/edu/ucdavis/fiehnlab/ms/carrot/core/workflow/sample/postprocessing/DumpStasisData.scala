package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import java.io.ByteArrayInputStream

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.loader.ResourceStorage
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Sample}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.{SampleResponse, TrackingResponse}
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * utility class to easily dump stasis data to the specified resource storage to keep for later records
  */

@Component
@Profile(Array("carrot.processing.dump.stasis"))
class DumpStasisData @Autowired()(storage: ResourceStorage, objectMapper: ObjectMapper, stasis: StasisService) extends PostProcessing[Double] with Logging {
  /**
    * actually processes the item (implementations in subclasses)
    *
    * @param item
    * @return
    */
  override def doProcess(item: QuantifiedSample[Double], method: AcquisitionMethod, rawSample: Option[Sample]): QuantifiedSample[Double] = {
    val tracking = stasis.getTracking(item.name)
    val aquisition = stasis.getAcquisition(item.name)


    val result = objectMapper.writeValueAsBytes(Dumped(name = item.name, tracking = tracking, acquisition = aquisition))

    storage.store(new ByteArrayInputStream(result), s"${item.name}.stasis.json")
    item
  }
}

case class Dumped(name: String, tracking: Option[TrackingResponse], acquisition: Option[SampleResponse])
