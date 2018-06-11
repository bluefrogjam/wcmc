package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 7/5/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("carrot.report.quantify.height", "carrot.lcms"))
class ExperimentTXTReaderTest extends WordSpec with LazyLogging {

  @Autowired
  val reader: ExperimentTXTReader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  //"Deprecated and unused class being tested"
  "read" ignore {
    "convert the given file" should {

      "read the data" in {
        val result = reader.read(getClass.getResourceAsStream("/lipids/lipidExperiment"))

      }

      "with 2 classes" in {
        val result = reader.read(getClass.getResourceAsStream("/lipids/lipidExperiment"))

        assert(result.classes.size == 2)
      }

      "first file should have 3 samples" in {
        val result = reader.read(getClass.getResourceAsStream("/lipids/lipidExperiment"))

        assert(result.classes.head.samples.size == 3)
      }

      "first file should have 2 samples" in {
        val result = reader.read(getClass.getResourceAsStream("/lipids/lipidExperiment"))

        assert(result.classes.last.samples.size == 2)
      }
    }
  }

  // "Deprecated and unused class being tested"
  "loading an experiment" ignore {
    val experimentFile = "/preproc/preProcExperiment.txt"

    "load existing samples ignoring missing filess" in {
      val result = reader.read(getClass.getResourceAsStream(experimentFile))

      assert(result.classes.head.samples.size == 1)
    }
  }
}
