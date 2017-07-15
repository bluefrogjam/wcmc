package edu.ucdavis.fiehnlab.ms.carrot.apps.runner

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{RetentionIndexTarget, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.{ExperimentTXTReader, QuantifiedSampleTxtWriter}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted.LCMSPositiveModeTargetWorkflow
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{CentralWorkflowConfig, Workflow, WorkflowProperties}
import edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j.MSDialRestProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}

/**
	* Created by diego on 7/14/2017.
	*/
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class AutoLC

object AutoLC extends App with LazyLogging {
	logger.info("creating app")
	val app = new SpringApplication(classOf[AutoLC])
		app.setWebEnvironment(false)
	app.run(args: _*)
}

@Configuration
@ComponentScan(basePackageClasses = Array(classOf[ResourceLoader], classOf[MSDialRestProcessor]))
@Import(Array(classOf[CentralWorkflowConfig]))
class MyConfiguration extends LazyLogging {
	logger.info("creating config")

	@Bean
	def workflow(workflowProperties: WorkflowProperties, reader: ExperimentTXTReader): LCMSPositiveModeTargetWorkflow[Double] = {
		logger.info("creating new workflow")
		new LCMSPositiveModeTargetWorkflow[Double](workflowProperties, writer, reader)
	}

	@Bean
	def writer: Writer[Sample] = new QuantifiedSampleTxtWriter[Sample]()

	/**
		* defined standards for retention index correction
		*
		* @return
		*/
	@Bean
	def correctionStandardList: LibraryAccess[RetentionIndexTarget] = new TxtStreamLibraryAccess[RetentionIndexTarget](resourceLoader.loadAsFile("retentionIndexStandards.txt").get, "\t")

	/**
		* our defined library of library targets
		*
		* @return
		*/
	@Bean
	def libraryAccess: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets.txt").get, "\t")

	@Bean
	def resourceLoader: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

	@Autowired
	val workflowProperties: WorkflowProperties = null

	@Bean
	def quantificationPostProcessing: List[PostProcessing[Double]] = List.empty[PostProcessing[Double]]

	@Bean(name = Array("quantification"))
	def quantification(properties: WorkflowProperties, libraryAccess: LibraryAccess[Target], quantificationPostProcessing: List[PostProcessing[Double]]): QuantifyByHeightProcess = new QuantifyByHeightProcess(libraryAccess, properties, quantificationPostProcessing)

}
