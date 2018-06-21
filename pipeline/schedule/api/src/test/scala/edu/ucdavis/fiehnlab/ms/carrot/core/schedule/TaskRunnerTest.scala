package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import java.io.File

import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{PositiveMode, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.TxtStreamLibraryAccess
import edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j.Everything4J
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Profile}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
@ActiveProfiles(Array("backend-txt-lcms", "carrot.report.quantify.height", "carrot.processing.peakdetection", "carrot.lcms", "file.source.luna"))
class TaskRunnerTest extends WordSpec {

  @Autowired
  val taskRunner: TaskRunner = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  // IGNORING for now until we rewrite the scheduler to use aws

  "TaskRunnerTest" should {

    "run - should fail since no samples are provided" ignore {

      intercept[AssertionError] {
        taskRunner.run(Task("test", "wohlgemuth@ucdavis.edu", acquisitionMethod = AcquisitionMethod(ChromatographicMethod("lcms_istds", Some("test"), Some("test"), Some(PositiveMode()))), samples = Seq.empty))
      }
    }

    "run - should pass" ignore {
      taskRunner.run(Task("test", "wohlgemuth@ucdavis.edu", acquisitionMethod = AcquisitionMethod(ChromatographicMethod("lcms_istds", Some("test"), Some("test"), Some(PositiveMode()))), samples = SampleToProcess("B5_P20Lipids_Pos_QC000.mzml") :: List()))
    }


  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class TestConfiguration {

  @Autowired
  val resourceLoader: DelegatingResourceLoader = null


  /**
    * below there will be all different directory loaders from the different workstations we are working on
    * smarter would be to use spring profiles
    *
    * @return
    */
  @Bean
  def resourceLoaderSrc: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

  /**
    * our defined library of library targets
    *
    * @return
    */
  @Profile(Array("backend-txt-lcms"))
  @Bean
  def targetLibraryLCMS: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets.txt").get, "\t")


  @Bean
  def client: FServ4jClient = new FServ4jClient(
    "testfserv.fiehnlab.ucdavis.edu",
    80
  )


  @Bean
  def everything4JEclipse: Everything4J = new Everything4J("eclipse.fiehnlab.ucdavis.edu")

  @Bean
  def workflow: Workflow[Double] = new Workflow[Double]
}
