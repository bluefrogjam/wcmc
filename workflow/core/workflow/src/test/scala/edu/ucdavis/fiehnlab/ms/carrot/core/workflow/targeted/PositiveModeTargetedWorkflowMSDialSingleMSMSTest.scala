package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import java.io.InputStream

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.ExperimentTXTReader
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 07/06/2017.
  *
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("msdial"))
class PositiveModeTargetedWorkflowMSDialSingleMSMSTest extends PositiveModeTargetedWorkflowTest {
	@Autowired
	val loader: SampleLoader = null

	@Autowired
	val targetLibrary: LibraryAccess[Target] = null

	@Autowired
	val reader: ExperimentTXTReader = null

	new TestContextManager(this.getClass).prepareTestInstance(this)

	def experimentDefinition: InputStream = getClass.getResourceAsStream("/full/qcExperimentMSDial_1MSMS.txt")

  override def expectedContentSize(): Integer = 33
  override def expectedValidationVal(): Double = 80000
  override def expectedValidationDelta(): Double = 0

}
