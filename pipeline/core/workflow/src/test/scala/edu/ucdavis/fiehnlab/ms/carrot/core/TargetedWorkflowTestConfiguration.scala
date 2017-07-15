package edu.ucdavis.fiehnlab.ms.carrot.core

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{RetentionIndexTarget, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{CentralWorkflowConfig, WorkflowProperties}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation._

/**
  * Test configuration of a LCMS target workflow
  */
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[CentralWorkflowConfig]))
class TargetedWorkflowTestConfiguration extends LazyLogging {

  @Autowired
  val resourceLoader: DelegatingResourceLoader = null

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
  def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets.txt").get, "\t")

  @Bean(name = Array("quantification"))
  def quantification(properties: WorkflowProperties, libraryAccess: LibraryAccess[Target], quantificationPostProcessing: List[PostProcessing[Double]]): QuantifyByHeightProcess = new QuantifyByHeightProcess(libraryAccess, properties, quantificationPostProcessing)

  @Bean
  def quantificationPostProcessing: List[PostProcessing[Double]] = List.empty[PostProcessing[Double]]

}
