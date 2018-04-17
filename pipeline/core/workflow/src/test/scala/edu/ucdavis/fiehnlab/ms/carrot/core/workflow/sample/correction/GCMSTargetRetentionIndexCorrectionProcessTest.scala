package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.NotEnoughStandardsFoundException
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
@ActiveProfiles(Array("carrot.gcms", "carrot.gcms.correction", "carrot.gcms.library.binbase"))
class GCMSTargetRetentionIndexCorrectionProcessWithBinBaseTest extends GCMSTargetRetentionIndexCorrectionProcessTest

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.gcms", "carrot.gcms.correction", "carrot.logging.json.enable"))
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

        val method = AcquisitionMethod(Option(ChromatographicMethod(name = "Gerstel", instrument = Some("LECO-GC-TOF"), column = Some("rtx5recal"), None)))
        /*
                "for sample 060712afisa86_1" should {

                  val result = correction.process(sampleLoader.getSample("060712afisa86_1.txt"), method)

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

                  val result = correction.process(sampleLoader.getSample("180321bZKsa26_1.txt"), method)

                  result.featuresUsedForCorrection.foreach { x =>
                    logger.info(s"${x.target.name} = ${x.annotation.retentionTimeInSeconds}")
                  }

                  "be at least as good as past calculation in BinBase" in {
                    assert(result.featuresUsedForCorrection.size >= 13)
                  }

                }


                "180213aJKsa01_1" :: "180213aJKsa02_1" :: "180213aJKsa03_1" :: "180213aJKsa04_1" :: "180213aJKsa05_1" :: "180213aJKsa18_1" :: "180213aJKsa19_1" :: "180213aJKsa20_1" :: List() foreach { sample =>

                  s"for sample $sample of study 386956" should {

                    "old BinBase cannot handle this sample, due to a new injector, which is based on Agilent, but carrot algorithm should be able to find it" in {
                      val result = correction.process(sampleLoader.getSample(s"${sample}.txt"), method)

                      result.featuresUsedForCorrection.foreach { x =>
                        logger.info(s"${x.target.name} = ${x.annotation.retentionTimeInSeconds}")
                      }


                      assert(result.featuresUsedForCorrection.size >= 12)
                    }

                  }

                }
        */

        "180220aJKsa01_1" :: "180220aJKsa02_1" :: "180220aJKsa03_1" :: List() foreach { sample =>

          s"for sample $sample of study 397074" should {

            "are no standards in these samples" in {
              intercept[NotEnoughStandardsFoundException] {
                correction.process(sampleLoader.getSample(s"${sample}.txt"), method, None)
              }


            }

          }

        }


        "180220aJKsa41_1" :: "180220aJKsa42_1" :: "180221aJKsa10_1" :: "180221aJKsa20_1" :: "180221aJKsa27_1" :: "180221aJKsa30_"  :: List() foreach { sample =>

          s"for sample $sample of study 397074" should {

            "old BinBase cannot handle this sample, due to a new injector, which is based on Agilent, but carrot algorithm should be able to find it" in {
              val result = correction.process(sampleLoader.getSample(s"${sample}.txt"), method, None)

              result.featuresUsedForCorrection.foreach { x =>
                logger.info(s"${x.target.name} = ${x.annotation.retentionTimeInSeconds}")
              }


              assert(result.featuresUsedForCorrection.size >= 12)
            }

          }

        }

        /*
                "180321sqlva01" :: "180321sqlva02" :: "180321sqlva03" :: "180321sqlva04" :: "180321sqlva05" :: List() foreach { sample =>

                  s"for sample $sample acquired by " should {

                    "old BinBase cannot handle this sample, due to a new injector, which is based on Agilent, so it should fail with the Gerstel method" in {
                      val result = correction.process(sampleLoader.getSample(s"${sample}.csv"), AcquisitionMethod(Option(ChromatographicMethod(name = "Gerstel", None, column = Some("rtx5"), None))))

                      result.featuresUsedForCorrection.foreach { x =>
                        logger.info(s"${x.target.name} = ${x.annotation.retentionTimeInSeconds}")
                      }


                      assert(result.featuresUsedForCorrection.size >= 13)
                    }

                  }

                }

        */
      }

    }
  }
}
