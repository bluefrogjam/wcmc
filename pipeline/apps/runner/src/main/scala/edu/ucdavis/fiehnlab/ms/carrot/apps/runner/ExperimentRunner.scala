package edu.ucdavis.fiehnlab.ms.carrot.apps.runner

import java.io.{File, FileInputStream, FileNotFoundException, FileOutputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.{DelegatingResourceLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.Writer
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
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
  val resourceLoaders: java.util.Collection[ResourceLoader] = null

  @Autowired
  val experimentTXTReader: ExperimentTXTReader = null

  @Autowired
  val writer:Writer[Sample] = null

  override def run(args: String*): Unit = {
    if (args.length < 1) {
      System.exit(1)
    }

    var expFile: File = new File(args.head)

    logger.info(s"trying to work with file: ${expFile.getAbsolutePath}")
    if (!expFile.exists()) {
      logger.info("trying to load file remotely")
      if(resourceLoader.exists(args.head)) {
        expFile = resourceLoader.loadAsFile(args.head).get
      }
      else{
        throw new FileNotFoundException(s"sorry the specified file was neither found locally nor in any defined loaders. Missing file name is: ${args.head}. Defined loaders in context are: ${resourceLoaders}")
      }
    }

    val resultFile = new File(s"${expFile.getName.substring(0, expFile.getName.lastIndexOf("."))}.result.txt")

    logger.info(s"storing result at: ${resultFile.getAbsolutePath}")

    val outFile: FileOutputStream = new FileOutputStream(resultFile)

    val results = workflow.process(experimentTXTReader.read(new FileInputStream(expFile)),writer)
    IOUtils.copy(results, outFile)

    outFile.flush()
    outFile.close()

  }
}
