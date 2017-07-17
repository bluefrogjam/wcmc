package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import java.io.InputStream

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import org.junit.runner.RunWith
import org.scalatest.Ignore
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by diego on 12/1/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration], classOf[PositiveModeTargetedWorkflowTestConfiguration]))
class PositiveModeTargetedWorkflowMzMLTest extends PositiveModeTargetedWorkflowTest {
  new TestContextManager(this.getClass).prepareTestInstance(this)

  def experimentDefinition: InputStream = getClass.getResourceAsStream("/qc/qcExperimentmzML.txt")

  override def expectedContentSize(): Integer = 5
  override def expectedValidationVal(): Double = 80000
  override def expectedValidationDelta(): Double = 1000
}
