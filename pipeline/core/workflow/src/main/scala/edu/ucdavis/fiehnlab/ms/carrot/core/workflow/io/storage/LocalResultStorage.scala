package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.storage

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import edu.ucdavis.fiehnlab.loader.ResourceStorage
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(Array("carrot.output.storage.local"))
class LocalResultStorage(resourceStorage: ResourceStorage) extends ResultStorage with Logging {

  @Autowired
  val writer: Writer[Sample] = null

  override def store(experiment: Experiment, task: Task): Unit = {

    val out = new ByteArrayOutputStream()
    writer.writeHeader(out)

    experiment.classes.foreach { c =>
      c.samples.foreach { s =>
        writer.write(out, s)
      }
    }
    writer.writeFooter(out)

    out.flush()
    out.close()

    resourceStorage.store(new ByteArrayInputStream(out.toByteArray), s"${task.name}.${writer.extension}")

  }
}
