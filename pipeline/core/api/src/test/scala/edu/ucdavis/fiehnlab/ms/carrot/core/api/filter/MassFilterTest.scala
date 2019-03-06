package edu.ucdavis.fiehnlab.ms.carrot.core.api.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.scalatest.{Matchers, WordSpec}

class MassFilterTest extends WordSpec with Matchers {

  "MassFilterTest" should {

    "isNominal" in {
      new MassFilter[Feature](0.0) {
        /**
          * references to all used settings
          */
        //        override protected val usedSettings: Map[String, Any] = Map()
        /**
          * which phase we require to log
          */
        //        override protected val phaseToLog: String = "test"
      }.isNominal shouldBe true
    }

    "isNotNominal" in {
      new MassFilter[Feature](0.005) {
        /**
          * references to all used settings
          */
        //        override protected val usedSettings: Map[String, Any] = Map()
        /**
          * which phase we require to log
          */
        //        override protected val phaseToLog: String = "test"
      }.isNominal shouldBe false
    }


    "sameMass" in {
      new MassFilter[Feature](0.0) {
        /**
          * references to all used settings
          */
        //        override protected val usedSettings: Map[String, Any] = Map()
        /**
          * which phase we require to log
          */
        //        override protected val phaseToLog: String = "test"
      }.sameMass(100.0,100.0) shouldBe true

    }
    "sameMassAccurate" in {
      new MassFilter[Feature](0.005) {
        /**
          * references to all used settings
          */
        //        override protected val usedSettings: Map[String, Any] = Map()
        /**
          * which phase we require to log
          */
        //        override protected val phaseToLog: String = "test"
      }.sameMass(100.0,100.0) shouldBe true

    }

    "notSameMass" in {
      new MassFilter[Feature](0) {
        /**
          * references to all used settings
          */
        //        override protected val usedSettings: Map[String, Any] = Map()
        /**
          * which phase we require to log
          */
        //        override protected val phaseToLog: String = "test"
      }.sameMass(100.0,101.0) shouldBe false

    }

    "notSameMassAccurate" in {
      new MassFilter[Feature](0.005) {
        /**
          * references to all used settings
          */
        //        override protected val usedSettings: Map[String, Any] = Map()
        /**
          * which phase we require to log
          */
        //        override protected val phaseToLog: String = "test"
      }.sameMass(100.0,101.0) shouldBe false

    }



  }
}
