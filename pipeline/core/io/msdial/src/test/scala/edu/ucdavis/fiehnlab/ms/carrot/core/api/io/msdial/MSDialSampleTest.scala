package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 5/7/2016.
  */
class MSDialSampleTest extends WordSpec {

  val sample = new MSDialSample(getClass.getResourceAsStream("/msdial-msms-lc.msdial"), "msdial-msms-lc.msdial")

  "MSDialSampleTest" should {
    "able to read all the spectra" in {
      assert(sample.spectra.size == 3619)
    }

    "ensure that all spectra have the correct properties" in {
      sample.spectra.collect {
        case spectra: MSSpectra =>

          assert(spectra.spectrum.get.ions.nonEmpty)
          assert(spectra.retentionTimeInSeconds > 0)
          assert(spectra.spectrum.get.msLevel == 1)
          assert(spectra.spectrum.get.modelIons.size == 1)
          assert(spectra.spectrum.get.splash != null)

        case feature: Feature =>
          assert(feature.retentionTimeInSeconds > 0)
          assert(feature.massOfDetectedFeature.isDefined)
      }
    }
  }
}
