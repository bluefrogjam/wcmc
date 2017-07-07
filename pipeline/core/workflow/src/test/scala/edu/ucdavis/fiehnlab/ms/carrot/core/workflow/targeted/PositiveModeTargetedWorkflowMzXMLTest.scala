package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import java.io.InputStream

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import org.junit.runner.RunWith
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by diego on 12/1/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[TargetedWorkflowTestConfiguration], classOf[PositiveModeTargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("common"))
class PositiveModeTargetedWorkflowMzXMLTest extends PositiveModeTargetedWorkflowTest {
  new TestContextManager(this.getClass).prepareTestInstance(this)

  def experimentDefinition: InputStream = getClass.getResourceAsStream("/qc/qcExperimentmzXML.txt")

  override def expectedContentSize(): Integer = 5
  override def expectedValidationVal(): Double = 80000
  override def expectedValidationDelta(): Double = 1000
}
