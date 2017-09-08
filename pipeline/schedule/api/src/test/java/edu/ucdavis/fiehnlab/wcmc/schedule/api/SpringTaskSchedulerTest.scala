package edu.ucdavis.fiehnlab.wcmc.schedule.api

import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.{ExperimentReaderTxTProperties, ExperimentTXTReader, QuantifiedSampleTxtWriter}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted.LCMSPositiveModeTargetWorkflow
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{CentralWorkflowConfig, WorkflowProperties}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 9/1/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Array("carrot.scheduler.spring", "carrot.report.quantify.height","carrot.store.result.fserv4j"))
class SpringTaskSchedulerTest extends WordSpec {

  @Autowired
  val scheduler: TaskScheduler = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SpringTaskSchedulerTest" should {

    "submit" in {

      val task = Task("test", AcquisitionMethod(None), Seq(SampleToProcess("B5_P20Lipids_Pos_QC000.abf", None)))

      scheduler.submit(task)
    }

  }
}

@SpringBootApplication
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[CentralWorkflowConfig]))
@EnableScheduling
class SpringTestApp {

  @Autowired
  val resourceLoader: DelegatingResourceLoader = null

  /**
    * our defined library of library targets
    *
    * @return
    */
  @Bean
  def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets.txt").get, "\t")


  @Bean
  def writer: QuantifiedSampleTxtWriter[Double] = {
    new QuantifiedSampleTxtWriter[Double]
  }

  @Bean
  def experimentTXTReader(localDirectorySampleLoader: SampleLoader, experimentTXTReaderProperties: ExperimentReaderTxTProperties): ExperimentTXTReader = {
    new ExperimentTXTReader(localDirectorySampleLoader, experimentTXTReaderProperties)
  }

  @Bean
  def workflow(properties: WorkflowProperties, writer: Writer[Sample], experimentTXTReader: Reader[Experiment]): LCMSPositiveModeTargetWorkflow[Double] = {
    new LCMSPositiveModeTargetWorkflow(properties)
  }
}
