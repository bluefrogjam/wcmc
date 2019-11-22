package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import edu.ucdavis.fiehnlab.loader.ResourceStorage
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.TrackingData
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * stores experimental data at the provided location, utilizes the defined writer
 */
@Profile(Array("carrot.runner.required"))
@Component("resultStorage")
class ResultStorage @Autowired()(@Qualifier("outputStorage") resourceStorage: ResourceStorage, writer: Writer[Sample], stasisService: StasisService) extends Logging {

  logger.info(s"Creating ResultStorage with: $resourceStorage and ${writer.getClass.getSimpleName}")

  def store(experiment: Experiment, task: Task): Unit = {

    val out = new ByteArrayOutputStream()
    writer.writeHeader(out)

    experiment.classes.foreach { c =>
      c.samples.foreach { s =>
        writer.write(out, s)
        out.flush()
        val bais = new ByteArrayInputStream(out.toByteArray)
        try {
          val finalName = s"${s.name}.${writer.extension}"
          logger.info(s"saving result file $finalName in ${resourceStorage.getDestination}")
          resourceStorage.store(bais, finalName)
          stasisService.addTracking(TrackingData(s.name, "exported", finalName))
        } catch {
          case e: Exception =>
            logger.error(s"Exception saving result in ${resourceStorage.getDestination}: ${e.getMessage}")
            stasisService.addTracking(TrackingData(s.name, "failed", s.fileName, e.getMessage))
        } finally {
          bais.close()
        }
      }
    }
    writer.writeFooter(out)
    out.flush()
    out.close()
  }

}

