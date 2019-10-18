package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.converter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Target => CTarget}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.{Target => STTarget}
import org.apache.logging.log4j.scala.Logging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component


@Component
@Profile(Array("carrot.output.storage.converter.target"))
class CarrotToStasisConverter extends Logging {

  def asStasisTarget(target: CTarget): STTarget = {
    logger.debug(s"converting carrot target ${target.name.get} to stasis target")
    STTarget(
      target.retentionTimeInSeconds,
      target.name.get,
      target.name.get,
      target.accurateMass.getOrElse(0.0),
      target.idx)
  }
}
