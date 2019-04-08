package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{NegativeMode, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.db.mona.MonaLibraryAccess
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test", "teddy", "carrot.targets.dummy"))
class TaskRunnerWithMSMSTest extends WordSpec {
  val libName = "teddy"

  @Autowired
  val taskRunner: TaskRunner = null

  @Autowired
  val mona: MonaLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "TaskRunnerWithMSMSTest" should {
    "process negative sample and upload MSMS to mona" in {

      val method = AcquisitionMethod(ChromatographicMethod(libName, Some("6550"), Some("test"), Some(NegativeMode())))

      taskRunner.run(Task("test", "dpedrosa@ucdavis.edu",
        acquisitionMethod = method,
        samples = SampleToProcess("B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml") +: Seq.empty,
        mode = "lcms",
        env = "test"
      ))

      mona.load(method).count(_.spectrum.get.msLevel == 2) > 0
    }

    "process positive sample and upload MSMS to mona" in {

      val method = AcquisitionMethod(ChromatographicMethod(libName, Some("6530"), Some("test"), Some(PositiveMode())))

      taskRunner.run(Task("test", "dpedrosa@ucdavis.edu",
        acquisitionMethod = method,
        samples = SampleToProcess("B1_SA0001_TEDDYLipids_Pos_1RAR7_MSMS.mzml") +: Seq.empty,
        mode = "lcms",
        env = "test"
      ))

      mona.load(method).count(_.spectrum.get.msLevel == 2) > 0
    }
  }
}

// 936 msms (untargeted)
