package edu.ucdavis.fiehnlab.ms.carrot.core.api.storage

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import edu.ucdavis.fiehnlab.loader.ResourceStorage
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, Matrix}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.TrackingData
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


/**
  * This defines a basic task to be submitted to the carrot system
  * for processing and calculations
  */
case class Task(name: String, email: Option[String], acquisitionMethod: AcquisitionMethod, samples: Seq[SampleToProcess], mode: String = null, env: String = null)


/**
  * a basic sample which should be processed
  *
  * @param fileName
  * @param matrix
  */
case class SampleToProcess(fileName: String, className: String = "", comment: String = "", label: String = "", matrix: Matrix = Matrix("", "", "", Seq.empty))

/**
  * stores experimental data at the provides location, utilizes the defined writer
  */
@Component
class ResultStorage @Autowired()(resourceStorage: ResourceStorage, writer: Writer[Sample], stasisService: StasisService) extends Logging {

  def store(experiment: Experiment, task: Task): Unit = {

    val out = new ByteArrayOutputStream()
    writer.writeHeader(out)

    experiment.classes.foreach { c =>
      c.samples.foreach { s =>
        writer.write(out, s)
        out.flush()
        val bais = new ByteArrayInputStream(out.toByteArray)
        try {
          logger.info(s"saving results for sample ${s.name} in AWS")
          resourceStorage.store(bais, s.name)
          stasisService.addTracking(TrackingData(s.name, "exported", s.fileName))
          bais.close()
        } catch {
          case e: Exception =>
            logger.error(s"Exception saving result in AWS bucket: ${e.getMessage}")
            stasisService.addTracking(TrackingData(s.name, "failed", s.fileName, e.getMessage))
            bais.close()
        }
      }
    }
    writer.writeFooter(out)

    out.close()
  }

}







