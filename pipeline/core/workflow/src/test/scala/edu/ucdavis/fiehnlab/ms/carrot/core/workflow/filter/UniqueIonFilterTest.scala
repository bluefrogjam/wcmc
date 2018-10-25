package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, SpectrumProperties}
import org.scalatest.{ShouldMatchers, WordSpec}

class UniqueIonFilterTest extends WordSpec with ShouldMatchers {

  val accurateMassFeature = new Feature {
    /**
      * the associated sample
      */
    override val sample: String = ""
    /**
      * how pure this spectra is
      */
    override val purity: Option[Double] = None
    /**
      * the signal noise of this spectra
      */
    override val signalNoise: Option[Double] = None
    /**
      * the unique mass of this spectra
      */
    override val uniqueMass: Option[Double] = Option(234.32)
    /**
      * the local scan number
      */
    override val scanNumber: Int = 0
    /**
      * the retention time of this spectra. It should be provided in seconds!
      */
    override val retentionTimeInSeconds: Double = 0
    /**
      * specified ion mode for the given feature
      */
    override val ionMode: Option[IonMode] = None
    /**
      * the associated complete scan for this feature
      */
    override val associatedScan: Option[SpectrumProperties] = None
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = None
  }

  val featureNoMass = new Feature {
    /**
      * the associated sample
      */
    override val sample: String = ""
    /**
      * how pure this spectra is
      */
    override val purity: Option[Double] = None
    /**
      * the signal noise of this spectra
      */
    override val signalNoise: Option[Double] = None
    /**
      * the unique mass of this spectra
      */
    override val uniqueMass: Option[Double] = None
    /**
      * the local scan number
      */
    override val scanNumber: Int = 0
    /**
      * the retention time of this spectra. It should be provided in seconds!
      */
    override val retentionTimeInSeconds: Double = 0
    /**
      * specified ion mode for the given feature
      */
    override val ionMode: Option[IonMode] = None
    /**
      * the associated complete scan for this feature
      */
    override val associatedScan: Option[SpectrumProperties] = None
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = None
  }

  val nominalFeature = new Feature {
    /**
      * the associated sample
      */
    override val sample: String = ""
    /**
      * how pure this spectra is
      */
    override val purity: Option[Double] = None
    /**
      * the signal noise of this spectra
      */
    override val signalNoise: Option[Double] = None
    /**
      * the unique mass of this spectra
      */
    override val uniqueMass: Option[Double] = Option(232)
    /**
      * the local scan number
      */
    override val scanNumber: Int = 0
    /**
      * the retention time of this spectra. It should be provided in seconds!
      */
    override val retentionTimeInSeconds: Double = 0
    /**
      * specified ion mode for the given feature
      */
    override val ionMode: Option[IonMode] = None
    /**
      * the associated complete scan for this feature
      */
    override val associatedScan: Option[SpectrumProperties] = None
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = None
  }

  val testTargetAccurate = new Target {
    /**
      * a name for this spectra
      */
    override var name: Option[String] = None
    /**
      * retention time in seconds of this target
      */
    override val retentionIndex: Double = 0
    /**
      * the unique inchi key for this spectra
      */
    override var inchiKey: Option[String] = None
    /**
      * the mono isotopic mass of this spectra
      */
    override val precursorMass: Option[Double] = None
    /**
      * unique mass for a given target
      */
    override val uniqueMass: Option[Double] = Option(234.32)
    /**
      * is this a confirmed target
      */
    override var confirmed: Boolean = true
    /**
      * is this target required for a successful retention index correction
      */
    override var requiredForCorrection: Boolean = true
    /**
      * is this a retention index correction standard
      */
    override var isRetentionIndexStandard: Boolean = true
    /**
      * associated spectrum propties if applicable
      */
    override val spectrum: Option[SpectrumProperties] = None
  }

  val testTargetNoMass = new Target {
    /**
      * a name for this spectra
      */
    override var name: Option[String] = None
    /**
      * retention time in seconds of this target
      */
    override val retentionIndex: Double = 0
    /**
      * the unique inchi key for this spectra
      */
    override var inchiKey: Option[String] = None
    /**
      * the mono isotopic mass of this spectra
      */
    override val precursorMass: Option[Double] = None
    /**
      * unique mass for a given target
      */
    override val uniqueMass: Option[Double] = None
    /**
      * is this a confirmed target
      */
    override var confirmed: Boolean = true
    /**
      * is this target required for a successful retention index correction
      */
    override var requiredForCorrection: Boolean = true
    /**
      * is this a retention index correction standard
      */
    override var isRetentionIndexStandard: Boolean = true
    /**
      * associated spectrum propties if applicable
      */
    override val spectrum: Option[SpectrumProperties] = None
  }

  val testTargetNonAccurate = new Target {
    /**
      * a name for this spectra
      */
    override var name: Option[String] = None
    /**
      * retention time in seconds of this target
      */
    override val retentionIndex: Double = 0
    /**
      * the unique inchi key for this spectra
      */
    override var inchiKey: Option[String] = None
    /**
      * the mono isotopic mass of this spectra
      */
    override val precursorMass: Option[Double] = None
    /**
      * unique mass for a given target
      */
    override val uniqueMass: Option[Double] = Option(232)
    /**
      * is this a confirmed target
      */
    override var confirmed: Boolean = true
    /**
      * is this target required for a successful retention index correction
      */
    override var requiredForCorrection: Boolean = true
    /**
      * is this a retention index correction standard
      */
    override var isRetentionIndexStandard: Boolean = true
    /**
      * associated spectrum propties if applicable
      */
    override val spectrum: Option[SpectrumProperties] = None
  }
  "UniqueIonFilterTest" should {

    "must include the first spectra - accurate mode" in {
      new UniqueIonFilter(testTargetAccurate, "test", 0.005).include(accurateMassFeature, null) shouldBe true
    }

    "must include the first spectra - nominal mode" in {
      new UniqueIonFilter(testTargetNonAccurate, "test", 0.0).include(nominalFeature, null) shouldBe true
    }


    "must exclude the first spectra - nominal mode" in {
      new UniqueIonFilter(testTargetNonAccurate, "test", 0.0).include(accurateMassFeature, null) shouldBe false
    }

    "must exclude the first spectra - accurate mode" in {
      new UniqueIonFilter(testTargetNonAccurate, "test", 0.005).include(accurateMassFeature, null) shouldBe false
    }
    "must exclude the the spectrra do to no mass for spectra" in {
      new UniqueIonFilter(testTargetNonAccurate, "test", 0.005).include(featureNoMass, null) shouldBe false
    }
    "must exclude the the spectra do to no mass for target" in {
      new UniqueIonFilter(testTargetNoMass, "test", 0.005).include(accurateMassFeature, null) shouldBe false
    }
    "must exclude the the spectra do to no mass for target and spectra" in {
      new UniqueIonFilter(testTargetNoMass, "test", 0.005).include(featureNoMass, null) shouldBe false
    }


  }
}
