package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{PositiveMode, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.db.binbase.BinBaseLibraryTarget
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.GCMSTargetRetentionIndexCorrectionProcess
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
@ActiveProfiles(Array("file.source.eclipse", "carrot.gcms", "carrot.gcms.correction", "carrot.gcms.library.binbase"))
class GCMSTargetAnnotationProcessTest extends WordSpec {


  @Autowired
  val correction: GCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val annotation: GCMSTargetAnnotationProcess = null

  @Autowired
  val sampleLoader: SampleLoader = null

  val logger: Logger = LoggerFactory.getLogger(getClass)

  val method = AcquisitionMethod(ChromatographicMethod(name = "Gerstel", instrument = Some("LECO-GC-TOF"), column = Some("rtx5recal"), ionMode = Option(PositiveMode())))


  new TestContextManager(this.getClass()).prepareTestInstance(this)

  def filexExtension: String = "txt"

  protected def prepareSample(sample: Sample) = {
    sample
  }

  "GCMSTargetAnnotationProcessTest" should {

    "process" must {

      "for sample 180501dngsa32_1" should {


        "be at least as good as past calculation in BinBase, sample id is: 1073331" in {

          val result = correction.process(prepareSample(sampleLoader.getSample("180501dngsa32_1." + filexExtension)), method)

          result.featuresUsedForCorrection.foreach { x =>
            logger.info(s"${x.target.name} = ${x.annotation.retentionTimeInSeconds}")
          }

          assert(result.featuresUsedForCorrection.size >= 13)

          var annotationResult = annotation.process(result, method)

          logger.info(s"annotated: ${annotationResult.spectra.size}")
          logger.info(s"missing: ${annotationResult.noneAnnotated.size}")

          annotationResult.spectra.foreach { x =>
            logger.info(s"${x.target.name} - ${x.retentionIndex} - ${x.target.getClass.getName}")
          }

          val binIdsInBinBaseCarrot = annotationResult.spectra.filter(_.target.isInstanceOf[BinBaseLibraryTarget]).map(_.target.asInstanceOf[BinBaseLibraryTarget].binId.toInt).sorted.toSet
          val binIdsInBinBaseLegacy = Array(
            117191,
            4937,
            6408,
            17446,
            84161,
            2475,
            84087,
            2472,
            2469,
            1686,
            17982,
            125790,
            17069,
            1373,
            46315,
            16661,
            161497,
            110602,
            168,
            21622,
            291,
            1912,
            20961,
            4384,
            199021,
            127444,
            4716,
            341677,
            13,
            43,
            240058,
            16545,
            1680,
            46173,
            21664,
            17883,
            88048,
            1815,
            16668,
            240078,
            3356,
            127,
            127676,
            1725,
            117195,
            329430,
            126305,
            3029,
            102605,
            239311,
            1064,
            121002,
            210327,
            1875,
            209178,
            42205,
            62253,
            1878,
            17001,
            241374,
            17105,
            344489,
            49,
            169,
            17651,
            120765,
            1704,
            13922,
            1169,
            5346,
            1909,
            62821,
            10,
            241360,
            1700,
            29923,
            889,
            191801,
            340673,
            18157,
            204741,
            31359,
            239634,
            236816,
            50422,
            329442,
            209685,
            239300,
            241312,
            148,
            334150,
            4949,
            327162,
            31408,
            209177,
            125664,
            5483,
            228,
            239310,
            17101,
            215929,
            209686,
            1965,
            50,
            239325,
            239314,
            236822,
            14755,
            92321,
            241399,
            130478,
            2936,
            26868,
            24,
            103102,
            133179,
            3268,
            6,
            209175,
            161878,
            237709,
            160961,
            43702,
            47,
            239316,
            4,
            30,
            6866,
            209176,
            490,
            10962,
            1079,
            183433,
            3256,
            85123,
            32,
            3,
            1905,
            23635,
            1913,
            7,
            2862,
            240042,
            3228,
            340667,
            341265,
            122191,
            160842,
            318795,
            341113,
            42224,
            145501,
            44175,
            239302,
            128,
            328907,
            4531,
            217691,
            1,
            238073,
            117141,
            17068,
            4976,
            318776,
            322524,
            4211,
            18082,
            249053,
            62,
            137,
            210223,
            106936,
            3179,
            22423,
            1971,
            325221,
            315573,
            12133,
            42167,
            282,
            210269,
            1694,
            1671,
            253919,
            125154,
            19163,
            189361,
            47170,
            127661,
            1741,
            11,
            170728,
            96,
            43734,
            160,
            3301
          ).sorted.toSet


          val foundInBinBaseButNotCarrot = binIdsInBinBaseLegacy.diff(binIdsInBinBaseCarrot)
          val foundInCarrotBytNotBinBase = binIdsInBinBaseCarrot.diff(binIdsInBinBaseLegacy)

          logger.info(s"found in binbase but not carrot: ${foundInBinBaseButNotCarrot.size}")
          logger.info(s"found in carrot but not binbase: ${foundInCarrotBytNotBinBase.size}")
          foundInBinBaseButNotCarrot.foreach{ x => logger.info(s"in binbase: ${x}")}
          foundInCarrotBytNotBinBase.foreach{ x => logger.info(s"in carrot: ${x}")}
        }

      }

    }
  }
}
