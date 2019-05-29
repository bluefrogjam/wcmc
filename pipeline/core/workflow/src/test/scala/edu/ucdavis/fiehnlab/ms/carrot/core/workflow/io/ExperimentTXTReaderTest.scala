package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 7/5/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("carrot.report.quantify.height", "carrot.lcms", "test"))
class ExperimentTXTReaderTest extends WordSpec with Logging {

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

}
