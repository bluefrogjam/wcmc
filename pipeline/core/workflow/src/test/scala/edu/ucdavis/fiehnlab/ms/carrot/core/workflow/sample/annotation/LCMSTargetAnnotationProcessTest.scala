package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByScanProcess
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 6/27/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("backend-txt-lcms", "quantify-by-scan", "carrot.processing.peakdetection", "carrot.lcms"))
class LCMSTargetAnnotationProcessTest extends WordSpec with LazyLogging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

	@Autowired
	val lcmsProperties: LCMSTargetAnnotationProperties = null

	@Autowired
	val loader: SampleLoader = null

  @Autowired
  val deco: PeakDetection = null

  /**
    * used to verify picked scans are correct
    */
  @Autowired
  val quantify: QuantifyByScanProcess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LCMSTargetAnnotationProcessTest" should {

    "ensure it's variables are defined" in {
      assert(annotation.targets != null)
    }

    val samples: Seq[_ <: Sample] = loader.getSamples(Seq("B5_P20Lipids_Pos_NIST01.d.zip", "B5_P20Lipids_Pos_NIST02.d.zip"))

    //compute purity values
    val purityComputed = samples //.map(purity.process)

    val method = AcquisitionMethod()

    //correct the data
    val correctedSample = purityComputed.map((item: Sample) => correction.process(deco.process(item, method, None), method, None))

    correctedSample.foreach { sample =>
      s"process ${sample} without recursive annotation and with preferring mass accuracy over retention index distance" in {

        annotation.lcmsProperties.recursiveAnnotationMode = false
        annotation.lcmsProperties.preferMassAccuracyOverRetentionIndexDistance = true

        val result = annotation.process(sample, method, None)

        assert(result != null)
        assert(result.noneAnnotated.size != result.spectra.size)
        assert((result.noneAnnotated.size + result.spectra.size) == result.correctedWith.spectra.size)

        result.featuresUsedForCorrection.foreach { spectra =>   //sortBy(_.target.name.get).
          logger.debug(f"${spectra.target.name.get}")
          logger.debug(f"\ttarget data:")
          logger.debug(f"\t\t mass:          ${spectra.target.precursorMass.get}%1.4f")
          logger.debug(f"\t\t rt (s):        ${spectra.target.retentionIndex}%1.3f")
          logger.debug(f"\t\t rt (m):        ${spectra.target.retentionTimeInMinutes}%1.3f")
          logger.debug(f"\tannotation data:")
          logger.debug(f"\t\t ri (m):        ${spectra.annotation.retentionTimeInMinutes}%1.3f")
          logger.debug(f"\t\t ri (s):        ${spectra.annotation.retentionTimeInSeconds}%1.3f")
          logger.debug(f"\t\t rt (m):        ${spectra.annotation.retentionTimeInMinutes}%1.3f")
          logger.debug(f"\t\t rt (s):        ${spectra.annotation.retentionTimeInSeconds}%1.3f")
          logger.debug(f"\t\t mass accuracy: --") //${spectra.massAccuracy.get}%1.5f
          logger.debug(f"\t\t mass accuracy: --") //${/*spectra.massAccuracyPPM.get*/}%1.3f ppm
          logger.debug(f"\t\t distance ri:   --") //${/*spectra.retentionIndexDistance.get*/}%1.3f

          logger.debug("")
        }
        val quantified = quantify.process(result, method, None)


        //these are our ISD
        quantified.spectra.filter(_.target.name.get == "*023 Acylcarnitine C10:0 [M+H]+").head.retentionTimeInMinutes shouldBe 0.603 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*007 1_CUDA ISTD [M+H]+").head.retentionTimeInMinutes shouldBe 0.794 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*020 1_Sphingosine d17:1 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 1.061 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*013 1_LPE 17:1 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 1.368 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*012 1_LPC 17:0 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 1.859 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD").head.retentionTimeInMinutes shouldBe 3.066 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*010 1_DG (18:1/2:0/0:0) [M+Na]+ ISTD").head.retentionTimeInMinutes shouldBe 3.182 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*017 1_PC 12:0/13:0 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 3.523 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*009 1_DG (12:0/12:0/0:0) [M+NH4]+ ISTD").head.retentionTimeInMinutes shouldBe 4.289 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*019 1_SM 17:0 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 5.096 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*004 1_Ceramide C17 [M+H-H2O]+ ISTD").head.retentionTimeInMinutes shouldBe 6.011 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*005 1_Ceramide C17 [M+Na]+ ISTD").head.retentionTimeInMinutes shouldBe 6.027 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*018 1_PE 17:0/17:0 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 6.327 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*002 1_CE (22:1) [M+NH4]+ ISTD").head.retentionTimeInMinutes shouldBe 11.809 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*022 1_TG d5 (17:0/17:1/17:0) ISTD [M+NH4]+").head.retentionTimeInMinutes shouldBe 11.094 +- 0.02
      }

      s"process ${sample} with recursive annotation and with preferring mass accuracy over retention index distance" in {

        annotation.lcmsProperties.recursiveAnnotationMode = true
        annotation.lcmsProperties.preferMassAccuracyOverRetentionIndexDistance = true

        val result = annotation.process(sample, method, None)

        assert(result != null)
        assert(result.noneAnnotated.size != result.spectra.size)
        assert((result.noneAnnotated.size + result.spectra.size) == result.correctedWith.spectra.size)

        logger.debug(s"sample name: ${sample.fileName}")
        result.spectra.sortBy(_.target.name.get).foreach { spectra =>
          logger.debug(f"${spectra.target.name.get}")
          logger.debug(f"\ttarget data:")
          logger.debug(f"\t\t mass:          ${spectra.target.precursorMass.get}%1.4f")
          logger.debug(f"\t\t rt (s):        ${spectra.target.retentionIndex}%1.3f")
          logger.debug(f"\t\t rt (m):        ${spectra.target.retentionTimeInMinutes}%1.3f")
          logger.debug(f"\tannotation data:")
          logger.debug(f"\t\t ri (m):        ${spectra.retentionIndex / 60}%1.3f")
          logger.debug(f"\t\t ri (s):        ${spectra.retentionIndex}%1.3f")
          logger.debug(f"\t\t rt (s):        ${spectra.retentionTimeInSeconds}%1.3f")
          logger.debug(f"\t\t rt (m):        ${spectra.retentionTimeInMinutes}%1.3f")
          logger.debug(f"\t\t mass accuracy: ${spectra.massAccuracy.get}%1.5f")
          logger.debug(f"\t\t mass accuracy: ${spectra.massAccuracyPPM.get}%1.3f ppm")
          logger.debug(f"\t\t distance ri:   ${spectra.retentionIndexDistance.get}%1.3f")


          logger.debug("")
        }
        val quantified = quantify.process(result, method, None)


        //these are our ISD
        quantified.spectra.filter(_.target.name.get == "*002 1_CE (22:1) [M+NH4]+ ISTD").head.retentionTimeInMinutes shouldBe 11.809 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*004 1_Ceramide C17 [M+H-H2O]+ ISTD").head.retentionTimeInMinutes shouldBe 6.011 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*005 1_Ceramide C17 [M+Na]+ ISTD").head.retentionTimeInMinutes shouldBe 6.027 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*007 1_CUDA ISTD [M+H]+").head.retentionTimeInMinutes shouldBe 0.794 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*009 1_DG (12:0/12:0/0:0) [M+NH4]+ ISTD").head.retentionTimeInMinutes shouldBe 4.289 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*010 1_DG (18:1/2:0/0:0) [M+Na]+ ISTD").head.retentionTimeInMinutes shouldBe 3.182 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*012 1_LPC 17:0 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 1.859 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*013 1_LPE 17:1 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 1.368 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD").head.retentionTimeInMinutes shouldBe 3.066 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*017 1_PC 12:0/13:0 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 3.523 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*018 1_PE 17:0/17:0 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 6.327 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*019 1_SM 17:0 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 5.096 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*020 1_Sphingosine d17:1 [M+H]+ ISTD").head.retentionTimeInMinutes shouldBe 1.061 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*022 1_TG d5 (17:0/17:1/17:0) ISTD [M+NH4]+").head.retentionTimeInMinutes shouldBe 11.094 +- 0.02
        quantified.spectra.filter(_.target.name.get == "*023 Acylcarnitine C10:0 [M+H]+").head.retentionTimeInMinutes shouldBe 0.603 +- 0.02
      }
    }

  }
}
