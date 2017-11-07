package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import org.scalatest.{ShouldMatchers, WordSpec}

/**
  * Created by wohlgemuth on 11/2/17.
  */
class TargetTest extends WordSpec with ShouldMatchers {

  "TargetTest" must {

    "ensure" which {

      " targets are equal" should {

        "by retention time" in {
          val a = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }
          val b = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }

          a should equal(b)
        }

        "not by retention time" in {
          val a = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 1233
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }
          val b = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }

          a should not equal (b)
        }

        "by precursor mass" in {
          val a = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = Option(12345.5)
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }
          val b = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = Option(12345.5)
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }

          a should equal(b)
        }


        "not by precursor mass" in {
          val a = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = Option(12345.6)
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }
          val b = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = Option(12345.5)
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }

          a should not equal(b)
        }


        "not by precursor mass, if one is none" in {
          val a = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = Option(12345.6)
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }
          val b = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }

          a should not equal(b)
        }

        "by inchi key, if one is none" in {
          val a = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = Option("1234567-12345-1")
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }
          val b = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = None
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }

          a should equal(b)
        }


        "by inchi key" in {
          val a = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = Option("1234567-12345-1")
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }
          val b = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String]  = Option("1234567-12345-1")
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }

          a should equal(b)
        }

        "by name if both have defined names" in {
          val a = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = Option("1234567-12345-1")
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = Option("a")
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }
          val b = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String]  = Option("1234567-12345-1")
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = Option("a")
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }

          a should equal(b)
        }
        "by name if one is None" in {
          val a = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = Option("1234567-12345-1")
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = None
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }
          val b = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String]  = Option("1234567-12345-1")
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = Option("a")
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }

          a should equal(b)
        }

        "not by name if one is different" in {
          val a = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String] = Option("1234567-12345-1")
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = Option("a")
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }
          val b = new Target {
            /**
              * the unique inchi key for this spectra
              */
            override var inchiKey: Option[String]  = Option("1234567-12345-1")
            /**
              * retention time in seconds of this target
              */
            override val retentionIndex: Double = 123
            /**
              * is this a confirmed target
              */
            override var confirmed: Boolean = false
            /**
              * the mono isotopic mass of this spectra
              */
            override val precursorMass: Option[Double] = None
            /**
              * a name for this spectra
              */
            override var name: Option[String] = Option("b")
            /**
              * is this target required for a successful retention index correction
              */
            override var requiredForCorrection: Boolean = false
            /**
              * is this a retention index correction standard
              */
            override var isRetentionIndexStandard: Boolean = false
            /**
              * associated spectrum propties if applicable
              */
            override val spectrum: Option[SpectrumProperties] = None
          }

          a should not equal(b)
        }
      }
    }
  }
}