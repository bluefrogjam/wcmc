package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import java.io.InputStream

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by diego on 07/06/2017.
  *
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
class PositiveModeTargetedWorkflowMSDialSingleMSMSTest extends PositiveModeTargetedWorkflowTest {

	new TestContextManager(this.getClass).prepareTestInstance(this)

	def experimentDefinition: InputStream = getClass.getResourceAsStream("/full/qcExperimentMSDial_1MSMS.txt")

  override def expectedContentSize(): Integer = 33
  override def expectedValidationVal(): Double = 80000
  override def expectedValidationDelta(): Double = 0

}
