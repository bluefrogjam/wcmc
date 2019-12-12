package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectionTarget, Ion, Target}
import org.apache.logging.log4j.scala.Logging
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@ActiveProfiles(Array("test",
  "carrot.lcms",
  "carrot.report.quantify.height",
  "carrot.filters.ioncount",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation"
))
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
class IonCountFilterTest extends WordSpec with Matchers with Logging {
  @Value("${carrot.filters.minIonCount:3}")
  val ionCount: Int = 0

  @Autowired
  val icFilter: Filter[Target] = null

  @Autowired
  val context: ApplicationContext = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val peakedTarget: Target = new CorrectionTarget {
    override var name: Option[String] = None
    override val retentionIndex: Double = 1.0
    override var inchiKey: Option[String] = None
    override val precursorMass: Option[Double] = None
    override val uniqueMass: Option[Double] = None
    override var confirmed: Boolean = false
    override var requiredForCorrection: Boolean = false
    override var isRetentionIndexStandard: Boolean = false
    override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
      override val msLevel: Short = 2
      override val modelIons: Option[Seq[Double]] = None
      override val ions: Seq[Ion] = Seq(Ion(10, 1000), Ion(20, 10), Ion(30, 100), Ion(40, 3123))
      override val rawIons: Option[Seq[Ion]] = None
    })
  }

  val simpleTarget: Target = new CorrectionTarget {
    override var name: Option[String] = None
    override val retentionIndex: Double = 1.0
    override var inchiKey: Option[String] = None
    override val precursorMass: Option[Double] = None
    override val uniqueMass: Option[Double] = None
    override var confirmed: Boolean = false
    override var requiredForCorrection: Boolean = false
    override var isRetentionIndexStandard: Boolean = false
    override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
      override val msLevel: Short = 2
      override val modelIons: Option[Seq[Double]] = None
      override val ions: Seq[Ion] = Seq(Ion(30, 100), Ion(40, 3123))
      override val rawIons: Option[Seq[Ion]] = None
    })
  }


  "IncludeByPeakCount" should {

    "have default minPeakCount" in {
      ionCount shouldBe 3
      icFilter match {
        case f: IonCountFilter =>
          f.minIonCount should equal(ionCount)
      }
    }

    "include target with 3 or more ions" in {
      icFilter.include(peakedTarget, context) shouldBe true
    }

    "exclude targets with less that 3 ions" in {
      icFilter.include(simpleTarget, context) shouldBe false
    }

  }
}

@ActiveProfiles(Array("alter-filter",
  "carrot.lcms",
  "carrot.report.quantify.height",
  "carrot.filters.ioncount",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation"
))
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
class IonCountFilterAlterTest extends WordSpec with Matchers with Logging {
  @Value("${carrot.filters.minIonCount:3}")
  val ionCount = 0

  @Autowired
  val icFilter: Filter[Target] = null

  @Autowired
  val context: ApplicationContext = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val peakedTarget: Target = new CorrectionTarget {
    override var name: Option[String] = None
    override val retentionIndex: Double = 1.0
    override var inchiKey: Option[String] = None
    override val precursorMass: Option[Double] = None
    override val uniqueMass: Option[Double] = None
    override var confirmed: Boolean = false
    override var requiredForCorrection: Boolean = false
    override var isRetentionIndexStandard: Boolean = false
    override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
      override val msLevel: Short = 2
      override val modelIons: Option[Seq[Double]] = None
      override val ions: Seq[Ion] = Seq(Ion(10, 1000), Ion(20, 10), Ion(30, 100), Ion(40, 3123))
      override val rawIons: Option[Seq[Ion]] = None
    })
  }

  "IncludeByPeakCount" should {

    "have default minPeakCount" in {
      ionCount shouldBe 5
      icFilter match {
        case f: IonCountFilter =>
          f.minIonCount should equal(ionCount)
      }
    }

    "exclude target with 3 ions" in {
      icFilter.include(peakedTarget, context) shouldBe false
    }
  }
}

