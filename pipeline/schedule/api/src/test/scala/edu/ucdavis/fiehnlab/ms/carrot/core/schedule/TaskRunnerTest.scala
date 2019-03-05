package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test"))
class TaskRunnerTest extends WordSpec {
  val libName = "lcms_istds"

  @Autowired
  val taskRunner: TaskRunner = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "TaskRunnerTest" should {

    "run - should fail since no samples are provided" in {

      intercept[AssertionError] {
        taskRunner.run(Task("test", "dpedrosa@ucdavis.edu",
          acquisitionMethod = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(PositiveMode()))),
          samples = Seq.empty,
          mode = "lcms",
          env = "test"
        ))
      }
    }

    "run - should pass" in {
      taskRunner.run(Task("test", "dpedrosa@ucdavis.edu",
        acquisitionMethod = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(PositiveMode()))),
        samples = SampleToProcess("B5_P20Lipids_Pos_QC000.mzML") +: Seq.empty,
        mode = "lcms",
        env = "test"
      ))
    }
  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class TestConfiguration extends Logging {
  @Bean
  def annotationLibrary(@Autowired(required = false) targets: java.util.List[LibraryAccess[AnnotationTarget]]): DelegateLibraryAccess[AnnotationTarget] = {
    if (targets == null) {
      logger.warn("no library provided, annotations will be empty!")
      new DelegateLibraryAccess[AnnotationTarget](new java.util.ArrayList())
    }
    else {
      new DelegateLibraryAccess[AnnotationTarget](targets)
    }
  }

  @Bean
  def correctionLibrary(targets: java.util.List[LibraryAccess[CorrectionTarget]]): DelegateLibraryAccess[CorrectionTarget] = new DelegateLibraryAccess[CorrectionTarget](targets)

}
