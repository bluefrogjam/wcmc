package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import java.io.InputStream

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.ExperimentTXTReader
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 12/1/2016.
  *
  * utilize MSDial data for the testing of the workflow
  * might be remove at a later time
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
class PositiveModeTargetedWorkflowMSDialSmallTest extends PositiveModeTargetedWorkflowTest {

	@Autowired
	val reader: ExperimentTXTReader = null

	new TestContextManager(this.getClass).prepareTestInstance(this)

	def experimentDefinition: InputStream = getClass.getResourceAsStream("/full/qcExperimentMSDial.txt")

  override def expectedContentSize(): Integer = 33
  override def expectedValidationVal(): Double = 80000
  override def expectedValidationDelta(): Double = 0

}
