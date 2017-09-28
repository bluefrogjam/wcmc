package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{GapFilledSpectra, QuantifiedSample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted.LCMSPositiveModeTargetWorkflow
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{CentralWorkflowConfig, WorkflowProperties}
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.MSDialRestProcessor
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.utilities.SpectrumMinimizer
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
	* Created by diego on 7/25/2016.
	*/
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[BZRTestConfiguration]))
class BetterZeroReplacementTest extends WordSpec with ShouldMatchers with LazyLogging {

	@Autowired
	val betterZeroReplacement: SimpleZeroReplacement = null

	@Autowired
	val preprocess: MSDialRestProcessor = null

	@Autowired
	val correction: LCMSTargetRetentionIndexCorrection = null

	@Autowired
	val annotation: LCMSTargetAnnotationProcess = null

	@Autowired
	@Qualifier("quantification")
	val quantify: QuantifyByHeightProcess = null

	@Autowired
	val loader: SampleLoader = null

	@Autowired
	val workflow: LCMSPositiveModeTargetWorkflow[Double] = null

	new TestContextManager(this.getClass).prepareTestInstance(this)

	logger.info(s"Workflow: ${workflow}")

	"BetterZeroReplacementTest" must {

		"replaceValue" should {
			val rawsample = loader.getSample("B5_P20Lipids_Pos_QC001.abf")

			val sample: QuantifiedSample[Double] =
				quantify.process(
					annotation.process(
						correction.process(
							rawsample
						)
					)
				)

			logger.info(s"Sample size: ${sample.spectra.length}")

			"replace the null values in the file" in {
				sample should not be null

				var replaced: QuantifiedSample[Double] = betterZeroReplacement.process(sample)
				replaced should not be null

				val merge = replaced.quantifiedTargets.zipAll(replaced.spectra, None, None)

				logger.info(s"\n${merge.filter(item => item._2.isInstanceOf[GapFilledSpectra[Double]] || item._2 == None).mkString("\n")}")

				//all spectra should be the same count as the targets
				val minAnnotations = replaced.quantifiedTargets.length //* 0.75
				replaced.spectra.length >= minAnnotations.toInt
			}
		}

	}
}

@Configuration
@Import(Array(classOf[CentralWorkflowConfig]))
class BZRTestConfiguration extends LazyLogging {
	@Value("${loaders.recursive.baseDirectory:./}")
	val directory: String = ""

	@Autowired
	val resourceLoader: DelegatingResourceLoader = null

//	@Bean
//	def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets1.txt").get, "\t")

	@Bean
	def betterZeroReplacement(properties: WorkflowProperties): PostProcessing[Double] = new SimpleZeroReplacement(properties)

	@Bean(name = Array("quantification"))
	def quantification(properties: WorkflowProperties, libraryAccess: LibraryAccess[Target], quantificationPostProcessing: java.util.List[PostProcessing[Double]]): QuantifyByHeightProcess = new QuantifyByHeightProcess(libraryAccess, properties, quantificationPostProcessing)

	@Bean
	def recursiveResourceLoader: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File(directory), 1000)

	@Bean
	def minimizer: SpectrumMinimizer = new SpectrumMinimizer()

	@Bean
	def zeroReplacementProperties: ZeroReplacementProperties = {
		val props = new ZeroReplacementProperties()
		props.fileExtension = "abf.processed" :: List.empty
//		props.massAccuracyPPM = 30
//		props.massAccuracy = 0.05
		props
	}
}
