package edu.ucdavis.fiehnlab.ms.carrot.apps.runner

import java.io.{File, FileInputStream, FileOutputStream}

import com.typesafe.scalalogging.LazyLogging
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
	val experimentTXTReader: ExperimentTXTReader = null

	override def run(args: String*): Unit = {
		logger.info("my runner class")
		if (args.length < 1) {
			System.exit(1)
		}

		println(args.mkString("\n"))
		val expFile: String = args.head

		val resultFile: String = new File(expFile).getName.substring(0, expFile.lastIndexOf(".") + 1)

		val outFile: FileOutputStream = new FileOutputStream(resultFile)

		if (new File(expFile).exists()) {
			val results = workflow.process(experimentTXTReader.read(new FileInputStream(expFile)))
			IOUtils.copy(results, outFile)
		} else {
			println("Experiment doesn't exist")
			System.exit(1)
		}
	}
}
