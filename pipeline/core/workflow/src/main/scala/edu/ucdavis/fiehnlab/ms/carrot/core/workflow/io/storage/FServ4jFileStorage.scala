package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.storage

import java.io.{File, FileOutputStream}

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * stores this sample on a remote server
  * using the FServ technologie
  */
@Component
@Profile(Array("carrot.store.result.fserv4j"))
class FServ4jFileStorage extends ResultStorage with Logging {

  @Autowired
  val writer: Writer[Sample] = null

  @Autowired
  val fServ4jClient: FServ4jClient = null

  /**
    * store the given experiment
    *
    * @param experiment
    */
  override def store(experiment: Experiment, task: Task): Unit = {

    val dir = new File(System.getProperty("java.io.tmpdir"))

    val file = new File(dir, s"${task.name}.${writer.extension}")

    logger.info(s"storing temporary data at: ${file}")
    val out = new FileOutputStream(file)

    writer.writeHeader(out)

    experiment.classes.foreach { c =>
      c.samples.foreach { s =>
        writer.write(out, s)
      }
    }
    writer.writeFooter(out)

    out.flush()
    out.close()

    fServ4jClient.upload(file, Some(s"${task.name}.${writer.extension}"))

    logger.info("delete temp file")
    file.delete()
  }
}
