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

		val expFile: File = new File(args.head)

		if (expFile.exists()) {
			val resultFile: String = expFile.getName.substring(0, expFile.getName.lastIndexOf("."))

			val outFile: FileOutputStream = new FileOutputStream(s"${resultFile}.final")

			val results = workflow.process(experimentTXTReader.read(new FileInputStream(expFile)))
			IOUtils.copy(results, outFile)

			outFile.flush()
			outFile.close()
		} else {
			println("Experiment doesn't exist")
			System.exit(1)
		}
	}
}
