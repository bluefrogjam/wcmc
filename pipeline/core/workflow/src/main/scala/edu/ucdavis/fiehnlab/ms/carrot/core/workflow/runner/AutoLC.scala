package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.runner

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}

/**
	* Created by diego on 7/14/2017.
	*/
@SpringBootApplication
@EnableAutoConfiguration //(exclude = Array(classOf[DataSourceAutoConfiguration]))
//@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
object AutoLC {
	def main(args: Array[String]): Unit = {
		if (args.size < 1) {
			println("I need an experiment file")
			System.exit(1)
		}

		val app = new SpringApplication(classOf[ExperimentRunner])
		app.setWebEnvironment(false)

		val context = app.run(args: _*)
	}
}
