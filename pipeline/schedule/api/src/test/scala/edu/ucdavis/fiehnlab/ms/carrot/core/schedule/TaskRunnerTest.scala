package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test",
  "carrot.resource.store.local",
  "carrot.targets.dummy"
))
class TaskRunnerTest extends WordSpec {
  val libName = "lcms_istds"

  @Autowired
  val taskRunner: TaskRunner = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "TaskRunnerTest" should {

    "run and throw exception since no samples are provided" in {

      intercept[AssertionError] {
        taskRunner.run(Task("test", None, acquisitionMethod = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(PositiveMode()))), samples = Seq.empty, mode = "lcms", env = "test"))
      }
    }

    "run successfully" in {
      taskRunner.run(Task("test", None, acquisitionMethod = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(PositiveMode()))), samples = SampleToProcess("B5_P20Lipids_Pos_NIST02.mzml") +: Seq.empty, mode = "lcms", env = "test"))
    }
  }
}
