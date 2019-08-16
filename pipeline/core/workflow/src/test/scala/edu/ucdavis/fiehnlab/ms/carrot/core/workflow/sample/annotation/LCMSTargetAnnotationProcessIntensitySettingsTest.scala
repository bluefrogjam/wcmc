package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{NegativeMode, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 6/27/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("file.source.luna",
  "carrot.report.quantify.height",
  "carrot.processing.peakdetection",
  "carrot.lcms",
  "test",
  "carrot.targets.yaml.annotation",
  "carrot.targets.yaml.correction"))
class LCMSTargetAnnotationProcessIntensitySettingsTest extends WordSpec with Matchers with Logging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val lcmsProperties: LCMSAnnotationProcessProperties = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val stasis_cli: StasisService = null

  @Autowired
  val quantify: QuantifyByHeightProcess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LCMSTargetAnnotationProcessTest" should {

    "ensure it's variables are defined" in {
      assert(annotation.targets != null)
    }

    val samples: Seq[_ <: Sample] = loader.getSamples(Seq(
      "B2a_TEDDYLipids_Neg_QC002.mzml",
      //   "B2a_TEDDYLipids_Neg_QC006.mzml",
      //   "B2a_TEDDYLipids_Neg_QC016.mzml",
      //   "B2a_TEDDYLipids_Neg_QC017.mzml",
      //   "B2a_TEDDYLipids_Neg_QC030.mzml",
      "B2a_SA1303_TEDDYLipids_Neg_1AS6X.mzml"
      //    "B2a_SA1304_TEDDYLipids_Neg_139TR_2.mzml",
      //   "B2a_SA1304_TEDDYLipids_Neg_139TR.mzml",
      //   "B2a_SA1305_TEDDYLipids_Neg_19VHX.mzml",
      //   "B2a_TEDDYLipids_Neg_NIST001.mzml",
      //      "B2a_TEDDYLipids_Neg_NIST002.mzml"
    ))

    val purityComputed = samples //.map(purity.process)

    val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6550"), Some("test"), Some(NegativeMode())))

    List(160000f, /*80000f,*/ 40000f, /*20000f,*/ 10000f, /*5000f,*/ 2500f, /*1250f,*/ 625f, /*315.5f,*/ 156.25f).reverse.foreach { ri_intensity =>

      s"correcting data with RI Correction intensity of ${ri_intensity}" should {
        //correct the data
        correction.minPeakIntensity = ri_intensity

        logger.info(s"evaluating settings ${ri_intensity}")
        val correctedSample = purityComputed.map((item: Sample) => correction.process(deco.process(item, method, None), method, None))


        correctedSample.foreach { sample =>

          logger.info(s"found the following count of correction features ${sample.featuresUsedForCorrection.size}")
          s"process ${sample} to evaluate annotation count" should {
            List(100, /*500,*/ 1000, 2000, /*3000,*/ 5000).foreach {
              intensity =>
                s"depends on intensity, we are evaluating ${intensity}" in {
                  annotation.lcmsProperties.recursiveAnnotationMode = false
                  annotation.lcmsProperties.preferGaussianSimilarityForAnnotation = true
                  annotation.lcmsProperties.massIntensity = intensity

                  val result = annotation.process(sample, method, None)

                  logger.info(s"annotation count ${result.spectra.size}")
                  logger.info(s"correction failed: ${sample.correctionFailed}")

                  assert(result != null)
                  assert(result.noneAnnotated.size != result.spectra.size)
                  assert((result.noneAnnotated.size + result.spectra.size) == result.correctedWith.spectra.size)

                  val quantified = quantify.process(result, method, None)
                }
            }
          }
        }
      }
    }
  }
}
