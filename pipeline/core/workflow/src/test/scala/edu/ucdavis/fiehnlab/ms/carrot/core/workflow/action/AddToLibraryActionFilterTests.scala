package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, Ion, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.db.mona.MonaLibraryAccess
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("test",
  "carrot.lcms",
  "carrot.lcms.correction",
  "carrot.report.quantify.height",
  "carrot.processing.peakdetection",
  "carrot.processing.replacement.simple",
  "carrot.filters.ioncount",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation",
  "carrot.targets.dynamic",
  "carrot.targets.mona"
))
class AddToLibraryActionFilterTests extends WordSpec with Matchers with Logging with Eventually with BeforeAndAfterEach {
  @Autowired
  val action: AddToLibraryAction = null

  @Autowired
  val mona: MonaLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val testMethod: AcquisitionMethod = AcquisitionMethod(ChromatographicMethod("testAction", None, None, Some(PositiveMode())))

  override def beforeEach: Unit = {
    eventually(timeout(value = 5 seconds), interval(1 second)) {
      mona.deleteLibrary(testMethod, Some(false))
      mona.load(testMethod, Some(false)) should have size 0
    }
  }

  "Method targetAlreadyExists" should {
    val target1: AnnotationTarget = new AnnotationTarget {
      override var name: Option[String] = None
      override val retentionIndex: Double = 211.6269
      override var inchiKey: Option[String] = None
      override val precursorMass: Option[Double] = Some(176.9478)
      override val uniqueMass: Option[Double] = None
      override var confirmed: Boolean = false
      override var requiredForCorrection: Boolean = false
      override var isRetentionIndexStandard: Boolean = false
      override val spectrum: Option[SpectrumProperties] = Some(
        new SpectrumProperties {
          override val msLevel: Short = 2
          override val modelIons: Option[Seq[Double]] = None
          override val ions: Seq[Ion] = Seq(Ion(94.978691, 13306.002930f), Ion(104.978691, 1330.00f), Ion(124.978691, 306.03f))
          override val rawIons: Option[Seq[Ion]] = None
        })
    }
    val target2: AnnotationTarget = new AnnotationTarget {
      override var name: Option[String] = None
      override val retentionIndex: Double = 311.6269
      override var inchiKey: Option[String] = None
      override val precursorMass: Option[Double] = Some(136.4754)
      override val uniqueMass: Option[Double] = None
      override var confirmed: Boolean = false
      override var requiredForCorrection: Boolean = false
      override var isRetentionIndexStandard: Boolean = false
      override val spectrum: Option[SpectrumProperties] = Some(
        new SpectrumProperties {
          override val msLevel: Short = 2
          override val modelIons: Option[Seq[Double]] = None
          override val ions: Seq[Ion] = Seq(Ion(94.978691, 13306.002930f), Ion(123.32172, 3000000), Ion(133.1234, 324.0f))
          override val rawIons: Option[Seq[Ion]] = None
        })
    }

    "have minIonCount matching config file" in {
      action.minIonCount shouldBe 3
    }

    "return true checking for duplicated target" in {

      eventually(timeout(value = 5 seconds), interval(1 second)) {
        mona.add(Seq(target1), testMethod, None)
        mona.load(testMethod, Some(false)) should have size 1
      }

      action.targetAlreadyExists(target1, testMethod, mona.load(testMethod, Some(false))) shouldBe true
    }

    "return false checking for very different target" in {

      eventually(timeout(value = 5 seconds), interval(1 second)) {
        mona.add(Seq(target1), testMethod, None)
        mona.load(testMethod, Some(false)) should have size 1
      }

      action.targetAlreadyExists(target2, testMethod, mona.load(testMethod, Some(false))) shouldBe false
    }
  }
}
