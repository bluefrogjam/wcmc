package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import edu.ucdavis.fiehnlab.loader.ResourceStorage
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.Matrix
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

    experiment.classes.foreach { c =>
      val md = c.matrix.collect {
        case matrix: Matrix =>
          Map("identifier" -> matrix.identifier,
            "organ" -> matrix.organ,
            "species" -> matrix.species,
            "treatments" -> Seq.empty,
            "class" -> "missing")
      }.getOrElse(Map.empty)

      c.samples.foreach { s =>
        val out = new ByteArrayOutputStream()

        //TODO: fix this nice hack to make it immutable
        s.metadata = md

        writer.write(out, s, Some(md))
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
        out.close()
      }
    }
  }

}

