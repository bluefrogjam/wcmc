package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, RetentionIndexTarget, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{CentralWorkflowConfig, WorkflowProperties}
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by diego on 7/25/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[BZRTestConfiguration]))
class BetterZeroReplacementTest extends WordSpec with LazyLogging with ShouldMatchers {

  @Autowired
  val betterZeroReplacement: BetterZeroReplacement = null

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrection = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  @Qualifier("quantification")
  val quantify: QuantifyByHeightProcess = null

  @Autowired
  val loader: SampleLoader = null

	try {
		new TestContextManager(this.getClass).prepareTestInstance(this)
	} catch {
		case ex: Exception =>
			fail(ex)
	}

  "SimpleZeroReplacementTest" must {

    val sample: QuantifiedSample[Double] =
      quantify.process(
        annotation.process(
          correction.process(
            loader.getSample("B5_P20Lipids_Pos_QC000.abf")
          )
        )
      )

    "replaceValue" should {

      var replaced: QuantifiedSample[Double] = null
      "replace the null values in the file" in {
          replaced = betterZeroReplacement.process(sample)


        replaced.spectra.foreach{ x =>
          logger.info(s"spectra: ${x}")
        }

        logger.info("---")
        replaced.quantifiedTargets.foreach{ x=>
          logger.info(s"target: ${x}")
        }

        //all spectra should be the same count as the targets
        replaced.spectra.size should be(replaced.quantifiedTargets.size)
      }
    }

  }
}

@SpringBootConfiguration
@Import(Array(classOf[CentralWorkflowConfig]))
class BZRTestConfiguration extends LazyLogging {
	@Value("${loaders.recursive.baseDirectory:./}")
	val directory: String = ""

	@Autowired
	val resourceLoader: DelegatingResourceLoader = null

	/**
		* defined standards for retention index correction
		*
		* @return
		*/
	@Bean
	def correctionStandardList: LibraryAccess[RetentionIndexTarget] = new TxtStreamLibraryAccess[RetentionIndexTarget](resourceLoader.loadAsFile("retentionIndexStandards.txt").get, "\t")

	/**
		* our defined library of library targets
		*
		* @return
		*/
	@Bean
	def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets.txt").get, "\t")

	@Bean(name = Array("quantification"))
	def quantification(properties: WorkflowProperties, libraryAccess: LibraryAccess[Target], quantificationPostProcessing: java.util.List[PostProcessing[Double]]): QuantifyByHeightProcess = new QuantifyByHeightProcess(libraryAccess, properties, quantificationPostProcessing)

	@Bean
	def betterZeroReplacement(properties: WorkflowProperties): PostProcessing[Double] = new BetterZeroReplacement(properties)

	@Bean
	def recursiveResourceLoader: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File(directory), 1000)
}
