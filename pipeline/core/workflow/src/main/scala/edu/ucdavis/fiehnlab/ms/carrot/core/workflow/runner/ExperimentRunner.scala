package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.runner

import java.io.{File, FileInputStream, FileOutputStream}

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.ExperimentTXTReader
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted.LCMSPositiveModeTargetWorkflow
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
	* Created by diego on 7/14/2017.
	*/

//@Component
class ExperimentRunner extends CommandLineRunner {
	@Autowired
	val workflow: LCMSPositiveModeTargetWorkflow[Double] = null

	@Autowired
	val experimentTXTReader: ExperimentTXTReader = null


	override def run(args: String*): Unit = {

		val expFile: String = args.head
		val resultFile: String = new File(expFile).getName.substring(0, expFile.lastIndexOf(".") + 1)

		val outFile: FileOutputStream = new FileOutputStream(resultFile)

		if (new File(expFile).exists()) {
			val result = workflow.process(experimentTXTReader.read(new FileInputStream(expFile)))
			IOUtils.copy(result, outFile)
		} else {
			println("Experiment doesn't exist")
			System.exit(1)
		}
	}
}
