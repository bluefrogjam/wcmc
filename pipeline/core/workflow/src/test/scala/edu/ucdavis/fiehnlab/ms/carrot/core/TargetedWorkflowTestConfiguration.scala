package edu.ucdavis.fiehnlab.ms.carrot.core

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{RetentionIndexTarget, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{WorkflowConfig, WorkflowProperties}
import edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j.MSDialRestProcessor
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation._

/**
  * Test configuration of a LCMS target workflow
  */
@SpringBootApplication
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[WorkflowConfig], classOf[LoadersConfiguration],classOf[MSDialRestProcessor]))
@Profile(Array("common"))
class TargetedWorkflowTestConfiguration extends LazyLogging {

  /**
    * defined standards for retention index correction
    *
    * @return
    */
  @Bean
  def correctionStandardList: LibraryAccess[RetentionIndexTarget] = new TxtStreamLibraryAccess[RetentionIndexTarget](new File("src/test/resources/retentionIndexStandards.txt"), "\t")

  /**
    * our defined library of library targets
    *
    * @return
    */
  @Bean
  def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](new File("src/test/resources/targets.txt"), "\t")

  @Bean(name = Array("quantification"))
  def quantification(properties: WorkflowProperties, libraryAccess: LibraryAccess[Target], quantificationPostProcessing: List[PostProcessing[Double]]): QuantifyByHeightProcess = new QuantifyByHeightProcess(libraryAccess, properties, quantificationPostProcessing)

  @Bean
  def quantificationPostProcessing: List[PostProcessing[Double]] = List.empty[PostProcessing[Double]]

}
