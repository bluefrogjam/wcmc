package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.storage

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.QuantifiedSample
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.converter.SampleToMapConverter
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.{ResultData, TrackingData}
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
@Profile(Array("carrot.output.storage.aws"))
class StasisResultStorage[T] extends ResultStorage with Logging {

  @Autowired
  val stasis_cli: StasisService = null

  val converter = new SampleToMapConverter[T]

  def save(sample: QuantifiedSample[T]): ResultData = {

    val data: ResultData = ResultData(sample.name, converter.convert(sample).asJava)

    val response = stasis_cli.addResult(data)

    if (response.getStatusCode == HttpStatus.OK) {
      stasis_cli.addTracking(TrackingData(sample.name, "exported", sample.fileName))
      response.getBody
    } else {
      logger.warn(response.getStatusCode.getReasonPhrase)
      data
    }
  }

  /**
    * store the given experiment
    *
    * @param experiment
    */
  override def store(experiment: Experiment, task: Task): Unit = {
    experiment.classes.foreach(x =>
      x.samples.foreach {
        case sample: QuantifiedSample[T] =>
          save(sample)
      }
    )
  }
}
