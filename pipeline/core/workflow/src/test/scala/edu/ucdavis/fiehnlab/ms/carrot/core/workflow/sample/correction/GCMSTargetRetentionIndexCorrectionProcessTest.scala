package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.GCMSTargetRetentionIndexCorrectionProcess
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.gcms", "carrot.gcms.correction"))
class GCMSTargetRetentionIndexCorrectionProcessTest extends WordSpec with ShouldMatchers {

  @Autowired
  val correction: GCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val sampleLoader: SampleLoader = null

  val logger: Logger = LoggerFactory.getLogger(getClass)

  new TestContextManager(this.getClass()).prepareTestInstance(this)


  "a gcms target correction" must {
    "find the retention index standard" should {
      "load a GCMS style sample" in {
        val sample = sampleLoader.getSample("060712afisa86_1.txt")

        sample.name should be("060712afisa86_1")
      }

      "allow to process data while loading a configuration from the Gerstel default Method" must {

        "for sample 060712afisa86_1" should {

          val result = correction.process(sampleLoader.getSample("060712afisa86_1.txt"), AcquisitionMethod(Option(ChromatographicMethod(name = "Gerstel", None, column = Some("rtx5"), None))))

          result.featuresUsedForCorrection.foreach { x =>
            logger.info(s"${x.target.name} = ${x.annotation.retentionTimeInSeconds}")
          }

          logger.info("")
          "be at least as good as past calculation in BinBase" in {
            //old correction only has 7 results
            assert(result.featuresUsedForCorrection.size >= 7)

          }

        }

        "for sample 180321bZKsa26_1" should {

          val result = correction.process(sampleLoader.getSample("180321bZKsa26_1.txt"), AcquisitionMethod(Option(ChromatographicMethod(name = "Gerstel", None, column = Some("rtx5"), None))))

          result.featuresUsedForCorrection.foreach { x =>
            logger.info(s"${x.target.name} = ${x.annotation.retentionTimeInSeconds}")
          }

          "be at least as good as past calculation in BinBase" in {
            assert(result.featuresUsedForCorrection.size >= 13)
          }

        }


        "for sample 180213aJKsa01_1" should {

          "BinBase cannot handle this sample, due to a new injector, which is based on Agilent, so it should fail with the Gerstel method" in {
            val result = correction.process(sampleLoader.getSample("180213aJKsa01_1.txt"), AcquisitionMethod(Option(ChromatographicMethod(name = "Gerstel", None, column = Some("rtx5"), None))))

            result.featuresUsedForCorrection.foreach { x =>
              logger.info(s"${x.target.name} = ${x.annotation.retentionTimeInSeconds}")
            }


            assert(result.featuresUsedForCorrection.size >= 13)
          }

        }
      }
    }
  }
}
