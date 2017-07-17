package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import java.io.{File, FileInputStream, InputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.{Action, PostAction}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{Reader, SampleLoader, Writer}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{CentralWorkflowConfig, Workflow, WorkflowProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.event._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.{ExperimentReaderTxTProperties, ExperimentTXTReader, QuantifiedSampleTxtWriter}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted.LCMSPositiveModeTargetWorkflow
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}
import org.springframework.stereotype.Component
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._

/**
	* Created by diego on 7/17/2017.
	*/
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[ActionTestConfiguration]))
class MSMSScannerActionTest extends WordSpec with Matchers with LazyLogging {
	@Autowired
	val experimentTXTReader: ExperimentTXTReader = null

	@Autowired
	val workflow: Workflow[Double] = null

//	@Autowired
//	val listener: MyWorkflowEventListener = null

	@Autowired
	var msmsScannerAction: MSMSScannerAction = null

	@Autowired
	val loader: ResourceLoader = null

	new TestContextManager(this.getClass).prepareTestInstance(this)

	"MSMSScannerActionTest" should {

		"have post actions defined" in {
			logger.info(s"postActions: ${workflow.postActions.asScala.map{_.getClass.getSimpleName}.mkString("\n")}")
			workflow.postActions should not be empty

			logger.info(s"listeners: ${workflow.eventListeners.asScala.map{_.getClass.getSimpleName}.mkString("\n")}")
			workflow.eventListeners should not be empty
		}

		val experiment: Experiment = experimentTXTReader.read(loader.load("qcExperimentMSDial_1MSMS.txt").get)

		val is: InputStream = workflow.process(experiment)
		logger.info(s" --- stream: ${is.available()}")
		val result: String = scala.io.Source.fromInputStream(is).mkString
		logger.info(s" --- result: ${result.length}")

		"experiment loaded" in {
			experiment.classes should not be empty
		}

		"find MSMS spectra in an MSMS Sample" in {

			result.length should be > 0
		}

	}
}

@Component
class MyWorkflowEventListener extends WorkflowEventListener with LazyLogging {

	/**
		* informs the listener about a workflow event
		*
		* @param workflowEvent
		*/
	override def handle(workflowEvent: WorkflowEvent): Unit = workflowEvent match {
		case event: AnnotationFinishedEvent => {
				logger.debug("annotations finished...")
				logger.debug("Scanning for msms...")
		}

		case event: PreProcessingFinishedEvent => {
				logger.debug("pre processing finished...")
		}

		case event: CorrectionFinishedEvent => {
				logger.debug("correction finished...")
		}

		case event: QuantificationBeginEvent => {
				logger.debug("Too late... quant started")
		}

		case _ => logger.info(s"received event: ${workflowEvent}")
	}
}

@Configuration
@ComponentScan(basePackageClasses = Array(classOf[ResourceLoader]))
@Import(Array(classOf[CentralWorkflowConfig]))
class ActionTestConfiguration {
	@Bean
	def msmsScannerAction: MSMSScannerAction = new MSMSScannerAction()

	@Bean
	def postActions: Seq[PostAction] = Seq(msmsScannerAction)

	@Bean
	def listener: WorkflowEventListener = new MyWorkflowEventListener()

	@Bean
	def loader: ResourceLoader = new RecursiveDirectoryResourceLoader(new File("/src"))

//	@Bean
//	def writer: QuantifiedSampleTxtWriter[Double] = {
//		new QuantifiedSampleTxtWriter[Double]
//	}
//
//	@Bean
//	def experimentTXTReader(localDirectorySampleLoader: SampleLoader, experimentTXTReaderProperties: ExperimentReaderTxTProperties): ExperimentTXTReader = {
//		new ExperimentTXTReader(localDirectorySampleLoader, experimentTXTReaderProperties)
//	}
//
//	@Bean
//	def workflow(properties: WorkflowProperties, writer: Writer[Sample], experimentTXTReader: Reader[Experiment], listener: WorkflowEventListener): Workflow[Double] = {
//		val wf = new LCMSPositiveModeTargetWorkflow[Double](properties, writer, experimentTXTReader)
//		wf.eventListeners.add(listener)
//		wf
//	}
}
