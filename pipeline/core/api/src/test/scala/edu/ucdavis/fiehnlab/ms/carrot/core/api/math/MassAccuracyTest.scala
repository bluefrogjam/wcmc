package edu.ucdavis.fiehnlab.ms.carrot.core.api.math

import edu.ucdavis.fiehnlab.ms.carrot.core.api._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, Target}
import org.scalatest.WordSpec
import org.scalatest._
import Matchers._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
/**
  * Created by wohlgemuth on 6/22/16.
  */
class MassAccuracyTest extends WordSpec {

  "MassAccuracyTest" should {

    val accuracy = MassAccuracy

    "inMilliDalton" must {

      "return the correct ion 1" in {
        val result = accuracy.findClosestIon(testAccurateMassSpectraWith4Ions2, 100.3241).get
        assert(result == Ion(100.3241, 50))
      }
      "return the correct ion 2" in {
        val result = accuracy.findClosestIon(testAccurateMassSpectraWith4Ions2, 100.3242).get
        assert(result == Ion(100.3242, 50))
      }
    }

    "calculateMassErrorPPM" must {

      "example 1" in {
        val ppmError = accuracy.calculateMassErrorPPM(
          new MSSpectra {override val purity: Option[Double] = None
            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0
            override val ions: Seq[Ion] = Ion(100.3241,100) :: List()
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = 0
            override val retentionTimeInSeconds: Double = 0
            /**
              * accurate mass of this feature, if applicable
              */
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(100.3241,100))
          }, new Target {
            override val monoIsotopicMass: Option[Double] = Some(100.3242)
            override val name: Option[String] = None
            override val inchiKey: Option[String] = None
            override val retentionTimeInSeconds: Double = 0
          })


        ppmError.get shouldBe 0.9967 +- 0.002
      }

      "example 2" in {
        val ppmError = accuracy.calculateMassErrorPPM(
          new MSSpectra {override val purity: Option[Double] = None
            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0
            override val ions: Seq[Ion] = Ion(100.3241,100) :: Ion(100.3341,40) :: List()
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = 0
            override val retentionTimeInSeconds: Double = 0
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(100.3241,100))
          }, new Target {
            override val monoIsotopicMass: Option[Double] = Some(100.3242)
            override val name: Option[String] = None
            override val inchiKey: Option[String] = None
            override val retentionTimeInSeconds: Double = 0
          })


        ppmError.get shouldBe 0.9967 +- 0.002
      }

      "example 3" in {
        val ppmError = accuracy.calculateMassErrorPPM(
          new MSSpectra {override val purity: Option[Double] = None
            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0
            override val ions: Seq[Ion] = Ion(100.3240,100) :: Ion(100.3341,40) :: List()
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = 0
            override val retentionTimeInSeconds: Double = 0
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(100.3241,100))
          }, new Target {
            override val monoIsotopicMass: Option[Double] = Some(100.3242)
            override val name: Option[String] = None
            override val inchiKey: Option[String] = None
            override val retentionTimeInSeconds: Double = 0
          })


        ppmError.get shouldBe 1.9935 +- 0.002
      }
      "example 4" in {
        val ppmError = accuracy.calculateMassErrorPPM(
          new MSSpectra {override val purity: Option[Double] = None
            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0
            override val ions: Seq[Ion] = Ion(1567.6401,100) :: Ion(100.3341,40) :: List()
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = 0
            override val retentionTimeInSeconds: Double = 0
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(1567.6401,100))
          }, new Target {
            override val monoIsotopicMass: Option[Double] = Some(1567.59330)
            override val name: Option[String] = None
            override val inchiKey: Option[String] = None
            override val retentionTimeInSeconds: Double = 0
          })


        ppmError.get shouldBe 29.8546 +- 0.002
      }
      "example 5" in {
        val ppmError = accuracy.calculateMassErrorPPM(
          new MSSpectra {override val purity: Option[Double] = None
            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0
            override val ions: Seq[Ion] = Ion(1567.5459,100) :: Ion(100.3341,40) :: List()
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = 0
            override val retentionTimeInSeconds: Double = 0
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(1567.5459,100))
          }, new Target {
            override val monoIsotopicMass: Option[Double] = Some(1567.59330)
            override val name: Option[String] = None
            override val inchiKey: Option[String] = None
            override val retentionTimeInSeconds: Double = 0
          })


        ppmError.get shouldBe 30.2374 +- 0.002
      }
      "example 6" in {
        val ppmError = accuracy.calculateMassErrorPPM(
          new MSSpectra {override val purity: Option[Double] = None
            override val ionMode: Option[IonMode] = None
            override val scanNumber: Int = 0
            override val ions: Seq[Ion] = Ion(1567.5459,100) :: Ion(100.3341,40) :: List()
            override val modelIons: Option[Seq[Double]] = None
            override val msLevel: Short = 0
            override val retentionTimeInSeconds: Double = 0
            override val massOfDetectedFeature: Option[Ion] = Option(Ion(1567.5459,100))
          }, new Target {
            override val monoIsotopicMass: Option[Double] = Some(1567.59330)
            override val name: Option[String] = None
            override val inchiKey: Option[String] = None
            override val retentionTimeInSeconds: Double = 0
          })

        ppmError.get shouldBe 30.2374 +- 0.0001
      }
    }
  }

}
