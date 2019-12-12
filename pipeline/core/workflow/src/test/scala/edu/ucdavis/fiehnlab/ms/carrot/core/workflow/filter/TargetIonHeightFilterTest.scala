package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}
import org.apache.logging.log4j.scala.Logging
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@ActiveProfiles(Array("test",
  "carrot.lcms",
  "carrot.report.quantify.height",
  "carrot.filters.intensity",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation"
))
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
class TargetIonHeightFilterTest extends WordSpec with Matchers with Logging {
  @Value("${carrot.filters.minIntensity:1000}")
  val intensity: Int = 0

  @Autowired
  val intFilter: TargetIonHeightFilter = null

  @Autowired
  val context: ApplicationContext = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val shorty: Target = new Target {
    override var name: Option[String] = None
    override val retentionIndex: Double = 0
    override var inchiKey: Option[String] = None
    override val precursorMass: Option[Double] = None
    override val uniqueMass: Option[Double] = None
    override var confirmed: Boolean = false
    override var requiredForCorrection: Boolean = false
    override var isRetentionIndexStandard: Boolean = false
    override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
      override val msLevel: Short = 1
      override val modelIons: Option[Seq[Double]] = None
      override val ions: Seq[Ion] = Seq(Ion(10.000, 100))
      override val rawIons: Option[Seq[Ion]] = None
    })
  }

  val tally: Target = new Target {
    override var name: Option[String] = None
    override val retentionIndex: Double = 0
    override var inchiKey: Option[String] = None
    override val precursorMass: Option[Double] = None
    override val uniqueMass: Option[Double] = None
    override var confirmed: Boolean = false
    override var requiredForCorrection: Boolean = false
    override var isRetentionIndexStandard: Boolean = false
    override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
      override val msLevel: Short = 1
      override val modelIons: Option[Seq[Double]] = None
      override val ions: Seq[Ion] = Seq(Ion(10, 1000))
      override val rawIons: Option[Seq[Ion]] = None
    })
  }

  "TargetIonHeightFilterTest" should {

    "have minIntensity defined" in {
      intensity shouldBe 1000
      intFilter.minIntensity should equal(intensity)
    }

    "include target with high enough ion" in {
      intFilter.include(tally, context) shouldBe true
    }

    "exclude target with not high enough ion" in {
      intFilter.include(shorty, context) shouldBe false
    }
  }
}

@ActiveProfiles(Array("alter-filter",
  "carrot.lcms",
  "carrot.report.quantify.height",
  "carrot.filters.intensity",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation"
))
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
class TargetIonHeightAlterFilterTest extends WordSpec with Matchers with Logging {
  @Value("${carrot.filters.minIntensity}")
  val intensity: Int = 0

  @Autowired
  val intFilter: TargetIonHeightFilter = null

  @Autowired
  val context: ApplicationContext = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val shorty: Target = new Target {
    override var name: Option[String] = None
    override val retentionIndex: Double = 0
    override var inchiKey: Option[String] = None
    override val precursorMass: Option[Double] = None
    override val uniqueMass: Option[Double] = None
    override var confirmed: Boolean = false
    override var requiredForCorrection: Boolean = false
    override var isRetentionIndexStandard: Boolean = false
    override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
      override val msLevel: Short = 1
      override val modelIons: Option[Seq[Double]] = None
      override val ions: Seq[Ion] = Seq(Ion(10.000, 2000))
      override val rawIons: Option[Seq[Ion]] = None
    })
  }

  val tally: Target = new Target {
    override var name: Option[String] = None
    override val retentionIndex: Double = 0
    override var inchiKey: Option[String] = None
    override val precursorMass: Option[Double] = None
    override val uniqueMass: Option[Double] = None
    override var confirmed: Boolean = false
    override var requiredForCorrection: Boolean = false
    override var isRetentionIndexStandard: Boolean = false
    override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
      override val msLevel: Short = 1
      override val modelIons: Option[Seq[Double]] = None
      override val ions: Seq[Ion] = Seq(Ion(10, 6000))
      override val rawIons: Option[Seq[Ion]] = None
    })
  }

  "TargetIonHeightFilterTest" should {

    "have minIntensity defined" in {
      intensity shouldBe 5000
      intFilter.minIntensity should equal(intensity)
    }

    "include target with high enough ion" in {
      intFilter.include(tally, context) shouldBe true
    }

    "exclude target with not high enough ion" in {
      intFilter.include(shorty, context) shouldBe false
    }
  }
}
