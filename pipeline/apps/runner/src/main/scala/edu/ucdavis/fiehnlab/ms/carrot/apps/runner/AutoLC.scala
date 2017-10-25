package edu.ucdavis.fiehnlab.ms.carrot.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.{DelegatingResourceLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.CentralWorkflowConfig
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.QuantifiedSampleTxtWriter
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.{PostProcessing, SimpleZeroReplacement, ZeroReplacementProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted.LCMSPositiveModeTargetWorkflow
import org.springframework.beans.factory.annotation.Value
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
  def workflow: LCMSPositiveModeTargetWorkflow[Double] = {
    new LCMSPositiveModeTargetWorkflow[Double]()
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


  @Bean
  def zeroReplacement: PostProcessing[Double] = new SimpleZeroReplacement()

	@Bean
	def zeroReplacementProperties: ZeroReplacementProperties = {
		val zrp = new ZeroReplacementProperties()
		zrp.fileExtension = "abf.processed" :: List.empty
		zrp
	}
}
