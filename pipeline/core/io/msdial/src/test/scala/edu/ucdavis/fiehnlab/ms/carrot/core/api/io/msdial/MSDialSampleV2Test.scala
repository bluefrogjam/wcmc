package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSMSSpectra
import org.scalatest.{Matchers, WordSpec}

class MSDialSampleV2Test extends WordSpec with Matchers {

  val sample = new MSDialSampleV2(getClass.getResourceAsStream("/msms.msdial"), "msms.msdial")

  "MSDialSampleV2Test" should {

    "sample must have spectra" in {
      sample.spectra.size should be > 0
    }

    "spectra must have associdated scans" in {

      sample.spectra.count(_.associatedScan.isDefined) shouldBe sample.spectra.size
    }

    "spectra must have MSMS scans" in {
      sample.spectra.exists(_.isInstanceOf[MSMSSpectra]) shouldBe true

      sample.spectra.count(_.isInstanceOf[MSMSSpectra]) should be < sample.spectra.size

    }

    "spectra must have accurate mass" in {
      sample.spectra.count(_.accurateMass.isDefined) shouldBe sample.spectra.size
    }
  }
}
