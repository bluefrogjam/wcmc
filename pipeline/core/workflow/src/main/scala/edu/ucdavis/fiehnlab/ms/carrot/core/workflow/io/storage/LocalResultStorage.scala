package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.storage

import java.io.{File, FileOutputStream}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(Array("carrot.output.storage.local"))
class LocalResultStorage[T] extends ResultStorage with Logging {

  @Autowired
  val writer: Writer[Sample] = null

  @Value("${scarrot.output.storage.local.dir:./}")
  val dir: String = "./"

  @Value("${wcmc.workflow.lcms.output.storage.local.postfix:\"\"}")
  val post_fix: String = ""

  @Value("${scarrot.output.storage.local.prefix:\"\"}")
  val prefix_fix: String = ""

  override def store(experiment: Experiment, task: Task): Unit = {
    val dir = new File(this.dir)

    val file = new File(dir, s"${prefix_fix}${task.name}${post_fix}.${writer.extension}")

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
  }
}
