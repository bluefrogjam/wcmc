package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import java.io.{File, InputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.LoadersConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, RetentionIndexTarget, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.{ExperimentReaderTxTProperties, ExperimentTXTReader, QuantifiedSampleTxtWriter}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{Workflow, WorkflowConfig, WorkflowProperties}
import edu.ucdavis.fiehnlab.ms.carrot.math.LinearRegression
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation._
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

import scala.io.Source

/**
	* Created by diego on 12/1/2016.
	*
	* utilize MSDial data for the testing of the workflow
	* might be remove at a later time
	*/
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[MSDialTestConfiguration]))
class PositiveModeTargetedWorkflowMSDialSmallTest extends WordSpec with Matchers with LazyLogging {
	@Autowired
	val workflow: LCMSPositiveModeTargetWorkflow[Double] = null

//	@Autowired
//	val properties: WorkflowProperties = null
//
	@Autowired
	val listener: TestWorkflowEventListener = null

//	@Autowired
//	val reader: ExperimentTXTReader = null
//
	val expectedCorrelation: Double = 0.99
	val expectedContentSize: Integer = 33
//	val expectedValidationVal: Double = 80000
//	val expectedValidationDelta: Double = 0

	def experimentDefinition: InputStream = getClass.getResourceAsStream("/full/qcExperimentMSDial.txt")

	new TestContextManager(this.getClass).prepareTestInstance(this)


	"LCMSPositiveModeTargetWorkflowTest" must {

		"process an experiment" when {

			"export all our data" should {

				val result = workflow.process(experimentDefinition)

				"result is not null" in {
					result should not be null
				}

				"ensure we got a quantified experiment as result" in {
					listener.quantifiedExperiment should not be null
				}

				s"content is of size ${expectedContentSize}" in {
					val content = Source.fromInputStream(result).getLines().toList

					content.size shouldBe expectedContentSize
				}

				"provide validation" when {

					val samples: Seq[QuantifiedSample[Double]] = listener.quantifiedExperiment.classes.flatMap(_.samples).collect {
						case sample: QuantifiedSample[Double] => sample
					}.filter(_ != null)

					s"*007 1_CUDA ISTD [M+H]+ regression coefficient > $expectedCorrelation" in {

						//automatically discovered values
						val annotatedTargets: Seq[Double] = samples.flatMap { s =>
							s.quantifiedTargets.filter { t =>
								t.target.name.get == "*007 1_CUDA ISTD [M+H]+"
							}.map(_.quantifiedValue.getOrElse(0.0))
						}

						//manually confirmed values by tomas
						val refrenceTarges: Seq[Double] = Array(667364.1, 655438.8, 630141.3, 657902.9, 641507.9, 660547.9, 650333.6,
							658413.1, 627398.6, 653240.2, 738846.3, 719581.7, 729094.6, 743863.3, 799186.8, 719726.9, 671941.6, 729776.1,
							709123, 741461.8, 635910, 639902.7, 620975.3, 618259.1, 652854.4, 628877.3, 613902.5, 618652.3, 623229.6)

						val regression = new LinearRegression()

						regression.calibration(annotatedTargets.toArray, refrenceTarges.toArray)

						val coeffiecient = regression.coefficient()(0)
						logger.info(s"CUDA ISTD [M+H]+ regression: $coeffiecient")

						coeffiecient should be > expectedCorrelation
					}


					/**
						* fails right now do to a MSDial bug TODO
						*/
					s"*015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD regression coefficient > ${expectedCorrelation}" in {

						//automatically discovered values
						val annotatedTargets: Seq[Double] = samples.flatMap { s =>
							s.quantifiedTargets.filter { t =>
								t.target.name.get == "*015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD"
							}.map(_.quantifiedValue.getOrElse(0.0))
						}

						//manually confirmed values by tomas
						val refrenceTarges: Seq[Double] = Array(84172.55, 88753.84, 91590.15, 86160.72, 89132, 83534.53, 90063.11,
							88874.77, 91844.45, 91814.16, 95505.53, 94563.38, 96612.41, 90806.9, 93718.09, 88924, 95578.27, 91195.52,
							94071.31, 91879.78, 91126.63, 90971.86, 93368.55, 87103.61, 90979.8, 84053.64, 89646.35, 86476.57, 88973.58)

						val regression = new LinearRegression()

						regression.calibration(annotatedTargets.toArray, refrenceTarges.toArray)

						val coefficient = regression.coefficient()(0)
						logger.info(s"MG 17:0/0:0/0:0 [M+Na]+ ISTD regression: $coefficient")

						coefficient should be > expectedCorrelation
					}

					s"*020 1_Sphingosine d17:1 [M+H]+ ISTD regression coefficient > ${expectedCorrelation}" in {

						//automatically discovered values
						val annotatedTargets: Seq[Double] = samples.flatMap { s =>
							s.quantifiedTargets.filter { t =>
								t.target.name.get == "*020 1_Sphingosine d17:1 [M+H]+ ISTD"
							}.map(_.quantifiedValue.getOrElse(0.0))
						}

						//manually confirmed values by tomas
						val refrenceTarges: Seq[Double] = Array(65737.89, 64059.59, 64284.1, 61659.55, 65604.73, 62722.94, 63476.96,
							64017.91, 62023.86, 65716.41, 71173.5, 68446.19, 70555.9, 72480.45, 75297.94, 66608.14, 67781.21, 71287.27,
							70727.13, 75156.08, 50854.79, 48742.45, 51324.99, 50445.16, 54343.21, 48873.45, 49486.82, 50665.44, 50382.46)

						val regression = new LinearRegression()

						regression.calibration(annotatedTargets.toArray, refrenceTarges.toArray)

						val coefficient = regression.coefficient()(0)
						logger.info(s"Sphingosine d17:1 [M+H]+ ISTD regression: $coefficient")

						coefficient should be > expectedCorrelation
					}

					s"*010 1_DG (18:1/2:0/0:0) [M+Na]+ ISTD regression coefficient > ${expectedCorrelation}" in {

						//automatically discovered values
						val annotatedTargets: Seq[Double] = samples.flatMap { s =>
							s.quantifiedTargets.filter { t =>
								t.target.name.get == "*010 1_DG (18:1/2:0/0:0) [M+Na]+ ISTD"
							}.map(_.quantifiedValue.getOrElse(0.0))
						}

						//manually confirmed values by tomas
						val refrenceTarges: Seq[Double] = Array(1144086, 1150598, 1146070, 1134526, 1160032, 1089979, 1118873, 1091180,
							1124240, 1130791, 1156351, 1257627, 1228662, 1168048, 1288668, 1178555, 1135616, 1171308, 1149503, 1174480,
							994138.8, 988540.3, 1034452, 1051443, 1051769, 1056460, 1041417, 1046814, 1016227)

						val regression = new LinearRegression()

						regression.calibration(annotatedTargets.toArray, refrenceTarges.toArray)

						val coefficient = regression.coefficient()(0)
						logger.info(s"DG (18:1/2:0/0:0) [M+Na]+ ISTD regression: $coefficient")

						coefficient should be > expectedCorrelation
					}
				}
			}
		}
	}
}


@Configuration
@ComponentScan(basePackageClasses = Array(classOf[ResourceLoader]))
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[WorkflowConfig], classOf[LoadersConfiguration]))
class MSDialTestConfiguration extends LazyLogging {

	/**
		* defined standards for retention index correction
		*
		* @return
		*/
	@Bean
	def correctionStandardList: LibraryAccess[RetentionIndexTarget] = new TxtStreamLibraryAccess[RetentionIndexTarget](new File("src/test/resources/retentionIndexStandards.txt"), "\t")

	/**
		* our defined library of library targets
		*
		* @return
		*/
	@Bean
	def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](new File("src/test/resources/targets.txt"), "\t")

	@Bean(name = Array("quantification"))
	def quantification(properties: WorkflowProperties, libraryAccess: LibraryAccess[Target], quantificationPostProcessing: List[PostProcessing[Double]]): QuantifyByHeightProcess = new QuantifyByHeightProcess(libraryAccess, properties, quantificationPostProcessing)

	@Bean
	def quantificationPostProcessing: List[PostProcessing[Double]] = List.empty[PostProcessing[Double]]

	@Bean
	def writer: QuantifiedSampleTxtWriter[Double] = {
		new QuantifiedSampleTxtWriter[Double]
	}

	@Bean
	def experimentTXTReader(localDirectorySampleLoader: SampleLoader, experimentTXTReaderProperties: ExperimentReaderTxTProperties): ExperimentTXTReader = {
		new ExperimentTXTReader(localDirectorySampleLoader, experimentTXTReaderProperties)
	}

	@Bean
	def workflow(properties: WorkflowProperties, writer: Writer[Sample], experimentTXTReader: Reader[Experiment]): Workflow[Double] = {
		new LCMSPositiveModeTargetWorkflow(properties, writer, experimentTXTReader)
	}

}

