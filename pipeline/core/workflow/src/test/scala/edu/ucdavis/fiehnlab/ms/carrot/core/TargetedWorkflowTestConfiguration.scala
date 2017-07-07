package edu.ucdavis.fiehnlab.ms.carrot.core

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{RetentionIndexTarget, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{WorkflowConfig, WorkflowProperties}
import org.springframework.context.annotation._

/**
  * Test configuration of a LCMS target workflow
  */
@Configuration
@Import(Array(classOf[WorkflowConfig], classOf[LoadersConfiguration]))
@Profile(Array("common", "msdial"))
class TargetedWorkflowTestConfiguration extends LazyLogging {

  /**
    * defined standards for retention index correction
    *
    * @return
    */
  @Bean
  def correctionStandardList: LibraryAccess[RetentionIndexTarget] = new TxtStreamLibraryAccess[RetentionIndexTarget](getClass.getResourceAsStream("/retentionIndexStandards.txt"), "\t")

  /**
    * our defined library of library targets
    *
    * @return
    */
  @Bean
  def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](getClass.getResourceAsStream("/targets.txt"), "\t")

  @Bean(name = Array("quantification"))
  def quantification(properties: WorkflowProperties, libraryAccess: LibraryAccess[Target], quantificationPostProcessing: List[PostProcessing[Double]]): QuantifyByHeightProcess = new QuantifyByHeightProcess(libraryAccess, properties, quantificationPostProcessing)

  @Bean
  def quantificationPostProcessing: List[PostProcessing[Double]] = List.empty[PostProcessing[Double]]

  @Bean
  def directoryResourceLoaderDiego: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("G:\\Data\\P20 Study processing\\CSH pos ABF"))

  @Bean
  def directoryResourceLoader: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("src/test"));

	@Bean
	def directoryResourceLoaderDiego2: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("G:/Data/P20 Study processing/CSH pos ABF"))

}
