package edu.ucdavis.fiehnlab.ms.carrot.core.api.math

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSSpectra, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, Target}
import org.scalatest.{Matchers, WordSpec}
/**
  * Created by wohlgemuth on 6/22/16.
  */
class MassAccuracyTest extends WordSpec with Matchers {

  "MassAccuracyTest" should {

    val accuracy = MassAccuracy

    "calculateMassErrorPPM" must {

      "example 1" in {
        val ppmError = accuracy.calculateMassErrorPPM(
          new MSSpectra {override val purity: Option[Double] = None
            override val metadata: Map[String, AnyRef] = Map()
            override val sample:String = null
            override val uniqueMass: Option[Double] = None
            override val signalNoise: Option[Double] = None

            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0

            override val associatedScan:Option[SpectrumProperties] = Some(new SpectrumProperties {

              override val ions: Seq[Ion] = Ion(100.3241, 100) :: List()
              override val modelIons: Option[Seq[Double]] = None
              override val rawIons: Option[Seq[Ion]] = None
              override val msLevel: Short = 1
            })
            override val retentionTimeInSeconds: Double = 0
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(100.3241, 100))
          }, new Target {
            override val uniqueMass: Option[Double] = None

            override val precursorMass: Option[Double] = Some(100.3242)
            override var name: Option[String] = None
            override var inchiKey: Option[String] = None
            override val retentionIndex: Double = 0
            override var confirmed: Boolean = false
            override var requiredForCorrection: Boolean = false
            override var isRetentionIndexStandard: Boolean = false
            override val spectrum: Option[SpectrumProperties] = None

          })


        ppmError.get shouldBe 0.9967 +- 0.002
      }

      "example 2" in {
        val ppmError = accuracy.calculateMassErrorPPM(
          new MSSpectra {override val purity: Option[Double] = None
            override val metadata: Map[String, AnyRef] = Map()
            override val sample:String = null
            override val uniqueMass: Option[Double] = None
            override val signalNoise: Option[Double] = None

            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0

            override val associatedScan:Option[SpectrumProperties] = Some(new SpectrumProperties {

              override val ions: Seq[Ion] = Ion(100.3241, 100) :: Ion(100.3341, 40) :: List()
              override val modelIons: Option[Seq[Double]] = None
              override val rawIons: Option[Seq[Ion]] = None
              override val msLevel: Short = 1
            })
            override val retentionTimeInSeconds: Double = 0
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(100.3241, 100))
          }, new Target {
            override val uniqueMass: Option[Double] = None

            override val precursorMass: Option[Double] = Some(100.3242)
            override var name: Option[String] = None
            override var inchiKey: Option[String] = None
            override val retentionIndex: Double = 0
            override var confirmed: Boolean = false
            override var requiredForCorrection: Boolean = false
            override var isRetentionIndexStandard: Boolean = false
            override val spectrum: Option[SpectrumProperties] = None

          })


        ppmError.get shouldBe 0.9967 +- 0.002
      }

      "example 3" in {
        val ppmError = accuracy.calculateMassErrorPPM(
          new MSSpectra {override val purity: Option[Double] = None
            override val metadata: Map[String, AnyRef] = Map()
            override val sample:String = null

            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0
            override val uniqueMass: Option[Double] = None
            override val signalNoise: Option[Double] = None


            override val associatedScan:Option[SpectrumProperties] = Some(new SpectrumProperties {

              override val ions: Seq[Ion] = Ion(100.3240, 100) :: Ion(100.3241, 40) :: List()
              override val modelIons: Option[Seq[Double]] = None
              override val rawIons: Option[Seq[Ion]] = None
              override val msLevel: Short = 1
            })

            override val retentionTimeInSeconds: Double = 0
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(100.3241, 100))
          }, new Target {
            override val uniqueMass: Option[Double] = None

            override val precursorMass: Option[Double] = Some(100.3242)
            override var name: Option[String] = None
            override var inchiKey: Option[String] = None
            override val retentionIndex: Double = 0
            override var confirmed: Boolean = false
            override var requiredForCorrection: Boolean = false
            override var isRetentionIndexStandard: Boolean = false
            override val spectrum: Option[SpectrumProperties] = None

          })


        ppmError.get shouldBe 0.99676 +- 0.002
      }

      "example 4" in {
        val ppmError = accuracy.calculateMassErrorPPM(
          new MSSpectra {override val purity: Option[Double] = None
            override val metadata: Map[String, AnyRef] = Map()
            override val sample:String = null
            override val uniqueMass: Option[Double] = None
            override val signalNoise: Option[Double] = None

            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0

            override val associatedScan:Option[SpectrumProperties] = Some(new SpectrumProperties {

              override val ions: Seq[Ion] = Ion(1567.6401, 100) :: Ion(100.3341, 40) :: List()
              override val modelIons: Option[Seq[Double]] = None
              override val rawIons: Option[Seq[Ion]] = None
              override val msLevel: Short = 1
            })

            override val retentionTimeInSeconds: Double = 0
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(1567.6401, 100))
          }, new Target {
            override val uniqueMass: Option[Double] = None

            override val precursorMass: Option[Double] = Some(1567.59330)
            override var name: Option[String] = None
            override var inchiKey: Option[String] = None
            override val retentionIndex: Double = 0
            override var confirmed: Boolean = false
            override var requiredForCorrection: Boolean = false
            override var isRetentionIndexStandard: Boolean = false
            override val spectrum: Option[SpectrumProperties] = None

          })


        ppmError.get shouldBe 29.8546 +- 0.002
      }

      "example 5" in {
        val ppmError = accuracy.calculateMassErrorPPM(
          new MSSpectra {override val purity: Option[Double] = None
            override val metadata: Map[String, AnyRef] = Map()
            override val sample:String = null
            override val uniqueMass: Option[Double] = None
            override val signalNoise: Option[Double] = None

            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0

            override val associatedScan:Option[SpectrumProperties] = Some(new SpectrumProperties {

              override val ions: Seq[Ion] = Ion(1567.5459, 100) :: Ion(100.3341, 40) :: List()
              override val modelIons: Option[Seq[Double]] = None
              override val rawIons: Option[Seq[Ion]] = None
              override val msLevel: Short = 1
            })


            override val retentionTimeInSeconds: Double = 0
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(1567.5459, 100))
          }, new Target {
            override val uniqueMass: Option[Double] = None

            override val precursorMass: Option[Double] = Some(1567.59330)
            override var name: Option[String] = None
            override var inchiKey: Option[String] = None
            override val retentionIndex: Double = 0
            override var confirmed: Boolean = false
            override var requiredForCorrection: Boolean = false
            override var isRetentionIndexStandard: Boolean = false
            override val spectrum: Option[SpectrumProperties] = None

          })


        ppmError.get shouldBe 30.2374 +- 0.002
      }

      "example 6" in {
        val ppmError = accuracy.calculateMassErrorPPM(

          new MSSpectra {override val purity: Option[Double] = None
            override val metadata: Map[String, AnyRef] = Map()
            override val sample:String = null
            override val uniqueMass: Option[Double] = None
            override val signalNoise: Option[Double] = None
            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0
            override val retentionTimeInSeconds: Double = 0
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(1567.5459, 100))

            override val associatedScan:Option[SpectrumProperties] = Some(new SpectrumProperties {
              override val ions: Seq[Ion] = Ion(1567.5459, 100) :: Ion(100.3341, 40) :: List()
              override val modelIons: Option[Seq[Double]] = None
              override val rawIons: Option[Seq[Ion]] = None
              override val msLevel: Short = 1
            })
          }, new Target {
            override val uniqueMass: Option[Double] = None
            override val precursorMass: Option[Double] = Some(1567.59330)
            override var name: Option[String] = None
            override var inchiKey: Option[String] = None
            override val retentionIndex: Double = 0
            override var confirmed: Boolean = false
            override var requiredForCorrection: Boolean = false
            override var isRetentionIndexStandard: Boolean = false
            override val spectrum: Option[SpectrumProperties] = None
          })

        ppmError.get shouldBe 30.2374 +- 0.0001
      }
    }
  }

}
