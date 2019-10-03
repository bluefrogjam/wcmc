package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, MSSpectra, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, Target}
import org.scalatest.WordSpec

/**
  * Created by wohlgemuth on 6/28/16.
  */
class RetentionIndexAnnotationTest extends WordSpec {

  "RetentionIndexAnnotationTest" should {

    val test = new RetentionIndexAnnotation(5)
    "isMatch" in {

      assert(test.isMatch(

        new MSSpectra with CorrectedSpectra {
          override val metadata: Map[String, AnyRef] = Map()
          val sample:String = null
          override val uniqueMass: Option[Double] = None
          override val signalNoise: Option[Double] = None

          override val purity: Option[Double] = None
          override val ionMode: Option[IonMode] = None
          override val scanNumber: Int = 1

          override val retentionTimeInSeconds: Double = 200
          override val retentionIndex: Double = retentionTimeInSeconds
          override val massOfDetectedFeature: Option[Ion] = Some(Ion(122.2, 123f))

          override val associatedScan = Option(new SpectrumProperties {
            override val ions: Seq[Ion] = Seq.empty
            override val modelIons: Option[Seq[Double]] = None
            override val rawIons: Option[Seq[Ion]] = None
            /**
              * the msLevel of this spectra
              */
            override val msLevel: Short = 1
          })
        },

        new Target {
          override val uniqueMass: Option[Double] = None

          override val precursorMass: Option[Double] = None
          override var name: Option[String] = None
          override var inchiKey: Option[String] = None
          override val retentionIndex: Double = 204.5f
          /**
            * is this a confirmed target
            */
          override var confirmed: Boolean = false
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
          override val spectrum = Option(new SpectrumProperties {
            override val ions: Seq[Ion] = Ion(122.2,123f) :: List()
            override val modelIons: Option[Seq[Double]] = None
            override val rawIons: Option[Seq[Ion]] = None
            /**
              * the msLevel of this spectra
              */
            override val msLevel: Short = 1
          }
          )
        }))

    }

    "isNoMatch" in {

      assert(!test.isMatch(

        new MSSpectra with CorrectedSpectra {
          override val metadata: Map[String, AnyRef] = Map()
          val sample:String = null
          override val uniqueMass: Option[Double] = None
          override val signalNoise: Option[Double] = None

          override val purity: Option[Double] = None
          override val ionMode: Option[IonMode] = None
          override val scanNumber: Int = 1
          override val associatedScan = Option(new SpectrumProperties {
            override val ions: Seq[Ion] = Seq.empty
            override val modelIons: Option[Seq[Double]] = None
            override val rawIons: Option[Seq[Ion]] = None
            /**
              * the msLevel of this spectra
              */
            override val msLevel: Short = 1
          })
          override val retentionTimeInSeconds: Double = 200
          override val retentionIndex: Double = retentionTimeInSeconds
          override val massOfDetectedFeature: Option[Ion] = Some(Ion(122.2, 123f))


        },

        new Target {
          override val uniqueMass: Option[Double] = None

          override val precursorMass: Option[Double] = None
          override var name: Option[String] = None
          override var inchiKey: Option[String] = None
          override val retentionIndex: Double = 205.5f
          /**
            * is this a confirmed target
            */
          override var confirmed: Boolean = false
          /**
            * is this target required for a successful retention index correction
            */
          override var requiredForCorrection: Boolean = false
          /**
            * is this a retention index correction standard
            */
          override var isRetentionIndexStandard: Boolean = false

          override val spectrum = Option(new SpectrumProperties {
            override val ions: Seq[Ion] = Ion(122.2,123f) :: List()
            override val modelIons: Option[Seq[Double]] = None
            override val rawIons: Option[Seq[Ion]] = None
            /**
              * the msLevel of this spectra
              */
            override val msLevel: Short = 1
          })
        }))

    }

  }


}
