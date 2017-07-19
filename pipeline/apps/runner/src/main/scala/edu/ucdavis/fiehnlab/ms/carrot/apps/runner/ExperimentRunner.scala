package edu.ucdavis.fiehnlab.ms.carrot.apps.runner

import java.io.{File, FileInputStream, FileOutputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.ExperimentTXTReader
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted.LCMSPositiveModeTargetWorkflow
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
  * Created by diego on 7/14/2017.
  */

@Component
class ExperimentRunner extends CommandLineRunner with LazyLogging {
  @Autowired
  val workflow: LCMSPositiveModeTargetWorkflow[Double] = null

  @Autowired
  val resourceLoader: DelegatingResourceLoader = null

  @Autowired
  val experimentTXTReader: ExperimentTXTReader = null

  override def run(args: String*): Unit = {
    if (args.length < 1) {
      System.exit(1)
    }

    var expFile: File = new File(args.head)

    if (!expFile.exists()) {
      expFile = resourceLoader.loadAsFile(args.head).get
    }

    val resultFile = new File(s"${expFile.getName.substring(0, expFile.getName.lastIndexOf("."))}.result.txt")

    logger.info(s"storing result at: ${resultFile.getAbsolutePath}")

    val outFile: FileOutputStream = new FileOutputStream(resultFile)

    val results = workflow.process(experimentTXTReader.read(new FileInputStream(expFile)))
    IOUtils.copy(results, outFile)

    outFile.flush()
    outFile.close()

  }
}
