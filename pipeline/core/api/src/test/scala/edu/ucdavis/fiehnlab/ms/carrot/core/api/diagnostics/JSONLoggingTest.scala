package edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, PositiveMode, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, SpectrumProperties}
import edu.ucdavis.fiehnlab.utilities.logging.{JSONLogging, JSONPhaseLogging}
import org.scalatest.{FunSuite, ShouldMatchers, WordSpec}

class JSONLoggingTest extends WordSpec with ShouldMatchers{

  "All logging test " must {

    "JSONLoggingTest" should {
      "should log the date as JSON" in {
        val test = new JSONLogging() {}

        val result = test.logJSON()

        val data = JSONLogging.objectMapper.readValue(result, classOf[Map[String, Any]])
        System.out.println(data)

        data.get("date").isDefined shouldBe true
      }

    }

    "JSONLoggingTest with JSONPhaseLogging" should {
      "should log the date as JSON" in {
        val test = new JSONLogging() with JSONPhaseLogging {

          /**
            * which phase we require to log
            */
          override val phaseToLog = "phase"
        }

        val result = test.logJSON()

        val data = JSONLogging.objectMapper.readValue(result, classOf[Map[String, Any]])
        System.out.println(data)

        data.get("date").isDefined shouldBe true
        data.get("phase").isDefined shouldBe true
      }

    }

    "JSONLoggingTest with JSONTargetLogging" should {
      val test = new JSONLogging() with JSONTargetLogging {
        /**
          * which target we require to log
          */
        override val targetToLog = new Target(){
          /**
            * a name for this spectra
            */
          override var name = Option("test")
          /**
            * retention time in seconds of this target
            */
          override val retentionIndex = 123.0
          /**
            * the unique inchi key for this spectra
            */
          override var inchiKey:Option[String] = None
          /**
            * the mono isotopic mass of this spectra
            */
          override val precursorMass = Some(123.0)
          /**
            * is this a confirmed target
            */
          override var confirmed = false
          /**
            * is this target required for a successful retention index correction
            */
          override var requiredForCorrection = false
          /**
            * is this a retention index correction standard
            */
          override var isRetentionIndexStandard = false
          /**
            * associated spectrum propties if applicable
            */
          override val spectrum = None
        }
      }


      "log the original data" in {

        val result = test.logJSON()

        val data = JSONLogging.objectMapper.readValue(result, classOf[Map[String, Any]])

        data.get("date").isDefined shouldBe true
      }

      "log additional target specific data" in {

        val result = test.logJSON()

        val data = JSONLogging.objectMapper.readValue(result, classOf[Map[String, Any]])

        System.out.println(data)
        data.get("date").isDefined shouldBe true
        data.get("target") should not be null
        data.get("target").get.asInstanceOf[Map[String,Any]].get("ri").isDefined shouldBe true

      }
    }

    "JSONLoggingTest with JSONTargetLogging with JSONFeatureLogging" should {
      val test = new JSONLogging() with JSONTargetLogging with JSONFeatureLogging {
        /**
          * which target we require to log
          */
        override val targetToLog = new Target(){
          /**
            * a name for this spectra
            */
          override var name = Option("test")
          /**
            * retention time in seconds of this target
            */
          override val retentionIndex = 123.0
          /**
            * the unique inchi key for this spectra
            */
          override var inchiKey:Option[String] = None
          /**
            * the mono isotopic mass of this spectra
            */
          override val precursorMass = Some(123.0)
          /**
            * is this a confirmed target
            */
          override var confirmed = false
          /**
            * is this target required for a successful retention index correction
            */
          override var requiredForCorrection = false
          /**
            * is this a retention index correction standard
            */
          override var isRetentionIndexStandard = false
          /**
            * associated spectrum propties if applicable
            */
          override val spectrum = None
        }
        /**
          * which feature we require to log
          */
        override val featureToLog = new Feature(){
          /**
            * the associated sample
            */
          override val sample = null
          /**
            * how pure this spectra is
            */
          override val purity = Option(0.0)
          /**
            * the local scan number
            */
          override val scanNumber = 1
          /**
            * the retention time of this spectra. It should be provided in seconds!
            */
          override val retentionTimeInSeconds = 12.0
          /**
            * specified ion mode for the given feature
            */
          override val ionMode:Option[IonMode] = None
          /**
            * accurate mass of this feature, if applicable
            */
          override val massOfDetectedFeature:Option[Ion] = None

          override val associatedScan : Option[SpectrumProperties] = None
        }
      }


      "log the original data" in {

        val result = test.logJSON()

        val data = JSONLogging.objectMapper.readValue(result, classOf[Map[String, Any]])

        data.get("date").isDefined shouldBe true
      }

      "log additional target specific data" in {

        val result = test.logJSON()

        val data = JSONLogging.objectMapper.readValue(result, classOf[Map[String, Any]])

        data.get("target") should not be null
        data.get("target").get.asInstanceOf[Map[String,Any]].get("ri").isDefined shouldBe true

      }
      "log additional feature specific data" in {

        val result = test.logJSON()

        val data = JSONLogging.objectMapper.readValue(result, classOf[Map[String, Any]])

        System.out.println(data)
        data.get("feature") should not be null
        data.get("feature").get.asInstanceOf[Map[String,Any]].get("rt").isDefined shouldBe true

      }
    }
  }


}
