package edu.ucdavis.fiehnlab.ms.carrot.apps.runner

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.{DelegatingResourceLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.{ExperimentTXTReader, QuantifiedSampleTxtWriter}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.{PostProcessing, SimpleZeroReplacement, ZeroReplacementProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted.LCMSPositiveModeTargetWorkflow
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{CentralWorkflowConfig, WorkflowProperties}
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.utilities.SpectrumMinimizer
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}

/**
  * Created by diego on 7/14/2017.
  */
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class AutoLC

object AutoLC extends App with LazyLogging {
  val app = new SpringApplication(classOf[AutoLC])
  app.setWebEnvironment(false)
  app.run(args: _*)
}

@Configuration
@ConfigurationProperties("application.properties")
@ComponentScan(basePackageClasses = Array(classOf[ResourceLoader]))
@Import(Array(classOf[CentralWorkflowConfig]))
class MyConfiguration extends LazyLogging {
	@Value("${loaders.recursive.baseDirectory:./}")
	val directory: String = ""

  @Bean
  def workflow(workflowProperties: WorkflowProperties, reader: ExperimentTXTReader): LCMSPositiveModeTargetWorkflow[Double] = {
    new LCMSPositiveModeTargetWorkflow[Double](workflowProperties)
  }

  @Bean
  def writer: Writer[Sample] = new QuantifiedSampleTxtWriter[Sample]()

  /**
    * defined standards for retention index correction
    *
    * @return
    */
  @Bean
  def correctionStandardList(resourceLoader: DelegatingResourceLoader): LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("retentionIndexStandards.txt").get)

  /**
    * our defined library of library targets
    *
    * @return
    */
  @Bean
  def libraryAccess(resourceLoader: DelegatingResourceLoader): LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets.txt").get)

  @Autowired
  val workflowProperties: WorkflowProperties = null

  @Bean
  def zeroReplacement(properties: WorkflowProperties): PostProcessing[Double] = new SimpleZeroReplacement(properties)

	@Bean
	def spectrumMinimizer: Option[SpectrumMinimizer] = Some(new SpectrumMinimizer())

	@Bean
	def zeroReplacementProperties: ZeroReplacementProperties = {
		val zrp = new ZeroReplacementProperties()
		zrp.fileExtension = "abf.processed" :: List.empty
		zrp
	}
}
