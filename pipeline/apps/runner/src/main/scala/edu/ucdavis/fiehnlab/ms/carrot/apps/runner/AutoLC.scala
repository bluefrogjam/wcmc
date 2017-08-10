package edu.ucdavis.fiehnlab.ms.carrot.apps.runner

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.loader.{DelegatingResourceLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.{ExperimentTXTReader, QuantifiedSampleTxtWriter}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.{PostProcessing, SimpleZeroReplacement}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted.LCMSPositiveModeTargetWorkflow
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{CentralWorkflowConfig, WorkflowProperties}
import org.springframework.beans.factory.annotation.{Autowired, Value}
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
  val app = new SpringApplication(classOf[AutoLC])
  app.setWebEnvironment(false)
  app.run(args: _*)
}

@Configuration
@ComponentScan(basePackageClasses = Array(classOf[ResourceLoader]))
@Import(Array(classOf[CentralWorkflowConfig]))
class MyConfiguration extends LazyLogging {
	@Value("${loaders.recursive.baseDirectory:./}")
	val directory: String = ""

  @Bean
  def workflow(workflowProperties: WorkflowProperties, reader: ExperimentTXTReader): LCMSPositiveModeTargetWorkflow[Double] = {
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


/*
  @Bean
  def postprocessing:java.util.List[PostProcessing[Double]] = new util.ArrayList[PostProcessing[Double]]()
  */
  @Bean(name = Array("quantification"))
  def quantification(properties: WorkflowProperties, libraryAccess: LibraryAccess[Target], quantificationPostProcessing: java.util.List[PostProcessing[Double]]): QuantifyByHeightProcess = new QuantifyByHeightProcess(libraryAccess, properties)

  @Bean
  def localLoader:ResourceLoader = new RecursiveDirectoryResourceLoader(new File(directory),1000)
}
