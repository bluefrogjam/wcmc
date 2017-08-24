package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.leco

import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 5/6/2016.
  */
class LecoSampleTest extends WordSpec {

  "LecoSampleTest" should {

    val sample = new LecoSample(getClass.getResourceAsStream("/leco.txt"), "leco.txt")

    "able to read all the spectra" in {
      assert(sample.spectra.size == 133)
    }

    "ensure that all spectra have the correct properties" in {
      sample.spectra.foreach{ spectra =>

        assert(spectra.spectrum.get.ions.nonEmpty)
        assert(spectra.retentionTimeInSeconds > 0)

        assert(spectra.spectrum.get.modelIons.size == 1)
        assert(spectra.purity.get > 0)
        assert(spectra.spectrum.get.splash != null)

      }
    }

  }
}
