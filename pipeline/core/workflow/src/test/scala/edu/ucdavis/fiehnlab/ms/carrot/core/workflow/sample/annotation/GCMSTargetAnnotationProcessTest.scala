package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{PositiveMode, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.db.binbase.{BinBaseLibraryAccess, BinBaseLibraryTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.GCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("file.source.eclipse", "carrot.gcms", "carrot.gcms.correction", "carrot.gcms.library.binbase", "test"))
class GCMSTargetAnnotationProcessTest extends WordSpec with Matchers {


  @Autowired
  val correction: GCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val annotation: GCMSTargetAnnotationProcess = null

  @Autowired
  val sampleLoader: SampleLoader = null

  val logger: Logger = LoggerFactory.getLogger(getClass)

  val method = AcquisitionMethod(ChromatographicMethod(name = "Gerstel", instrument = Some("LECO-GC-TOF"), column = Some("rtx5recal"), ionMode = Option(PositiveMode())))

  @Autowired
  val library: BinBaseLibraryAccess = null

  @Autowired
  val stasis_cli: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  def filexExtension: String = "txt"

  protected def prepareSample(sample: Sample): Sample = {
    sample
  }

  "GCMSTargetAnnotationProcessTest" should {

    "process" must {

      "for sample 180501dngsa32_1" should {


        "be at least as good as past calculation in BinBase, sample id is: 1073384" in {

          library.binQuery =
            """
              |select b.* from spectra a, bin b where a.bin_id is not null and a.sample_id = 1073384 and a.bin_id not in (
              |	select bin_id from standard
              |)
              |and CAST(a.bin_id as text) != b."name" and a.bin_id = b.bin_id
              |and b.export = 'TRUE'
              |and b.group_id is null
            """.stripMargin

          val result = correction.process(prepareSample(sampleLoader.getSample("180507dZKsa09_1." + filexExtension)), method, None)

          result.featuresUsedForCorrection.foreach { x =>
            logger.info(s"${x.target.name} = ${x.annotation.retentionTimeInSeconds}")
          }

          assert(result.featuresUsedForCorrection.size >= 13)

          var annotationResult = annotation.process(result, method, None)

          logger.info(s"annotated: ${annotationResult.spectra.size}")
          logger.info(s"missing: ${annotationResult.noneAnnotated.size}")

          annotationResult.spectra.foreach { x =>
            logger.info(s"${x.target.name} - ${x.retentionIndex} - ${x.target.getClass.getName}")
          }

          val binIdsInBinBaseCarrot = annotationResult.spectra.filter(_.target.isInstanceOf[BinBaseLibraryTarget]).map(_.target.asInstanceOf[BinBaseLibraryTarget].binId.toInt).sorted.toSet
          val binIdsInBinBaseLegacy = Array(
            6432,
            171211,
            291,
            94,
            4903,
            13,
            14,
            724,
            727,
            17533,
            34065,
            3274,
            31356,
            16713,
            16545,
            1680,
            171823,
            18490,
            70,
            20287,
            127,
            126321,
            1689,
            53724,
            104147,
            488,
            146,
            33,
            41895,
            4792,
            4594,
            113643,
            2038,
            65,
            4527,
            79,
            125,
            413,
            1208,
            299,
            1692,
            16643,
            1965,
            50,
            483,
            25,
            101725,
            18043,
            285,
            106285,
            97,
            100955,
            453,
            4923,
            1871,
            585
          ).sorted.toSet


          val foundInBinBaseButNotCarrot = binIdsInBinBaseLegacy.diff(binIdsInBinBaseCarrot)
          val foundInCarrotBytNotBinBase = binIdsInBinBaseCarrot.diff(binIdsInBinBaseLegacy)

          logger.info(s"found in binbase but not carrot: ${foundInBinBaseButNotCarrot.size}")
          logger.info(s"found in carrot but not binbase: ${foundInCarrotBytNotBinBase.size}")
          foundInBinBaseButNotCarrot.foreach { x => logger.info(s"in binbase: ${x}") }
          foundInCarrotBytNotBinBase.foreach { x => logger.info(s"in carrot: ${x}") }


          //there is no deconvolution or quantification done on this sample
          stasis_cli.getTracking(annotationResult.name).status.map(_.value) should contain("corrected")
          stasis_cli.getTracking(annotationResult.name).status.map(_.value) should contain("annotated")
        }

      }

      "for sample 180419bCSsa12_1" should {


        "be at least as good as past calculation in BinBase, sample id is: 1073361" in {

          library.binQuery =
            """
              |select b.* from spectra a, bin b where a.bin_id is not null and a.sample_id = 1073361 and a.bin_id not in (
              |	select bin_id from standard
              |)
              |and CAST(a.bin_id as text) != b."name" and a.bin_id = b.bin_id
              |and b.export = 'TRUE'
              |and b.group_id is null
            """.stripMargin

          val result = correction.process(prepareSample(sampleLoader.getSample("180419bCSsa12_1." + filexExtension)), method, None)

          result.featuresUsedForCorrection.foreach { x =>
            logger.info(s"${x.target.name} = ${x.annotation.retentionTimeInSeconds}")
          }

          assert(result.featuresUsedForCorrection.size >= 13)

          var annotationResult = annotation.process(result, method, None)

          logger.info(s"annotated: ${annotationResult.spectra.size}")
          logger.info(s"missing: ${annotationResult.noneAnnotated.size}")

          annotationResult.spectra.foreach { x =>
            logger.info(s"${x.target.name} - ${x.retentionIndex} - ${x.target.getClass.getName}")
          }

          val binIdsInBinBaseCarrot = annotationResult.spectra.filter(_.target.isInstanceOf[BinBaseLibraryTarget]).map(_.target.asInstanceOf[BinBaseLibraryTarget].binId.toInt).sorted.toSet
          val binIdsInBinBaseLegacy = Array(
            170271,
            110304,
            42937,
            102121,
            3272,
            16601,
            6432,
            290,
            20961,
            724,
            1674,
            3244,
            16829,
            2787,
            34065,
            11214,
            4931,
            4706,
            16545,
            71,
            17883,
            131590,
            107906,
            20287,
            3356,
            127,
            1667,
            1689,
            1683,
            84543,
            34153,
            17400,
            18492,
            233,
            488,
            3169,
            3469,
            3252,
            17886,
            85112,
            33,
            1685,
            65,
            16637,
            4527,
            1679,
            97,
            79,
            4944,
            16903,
            413,
            4953,
            1208,
            299,
            50,
            25,
            31743,
            4939,
            91421,
            4923,
            403,
            1871,
            3524,
            94,
            13,
            14,
            165,
            106285,
            208,
            10827,
            135
          ).sorted.toSet


          val foundInBinBaseButNotCarrot = binIdsInBinBaseLegacy.diff(binIdsInBinBaseCarrot)
          val foundInCarrotBytNotBinBase = binIdsInBinBaseCarrot.diff(binIdsInBinBaseLegacy)

          logger.info(s"found in binbase but not carrot: ${foundInBinBaseButNotCarrot.size}")
          logger.info(s"found in carrot but not binbase: ${foundInCarrotBytNotBinBase.size}")
          foundInBinBaseButNotCarrot.foreach { x => logger.info(s"in binbase: ${x}") }
          foundInCarrotBytNotBinBase.foreach { x => logger.info(s"in carrot: ${x}") }
        }

      }

    }

  }
}
