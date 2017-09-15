package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms

import org.scalatest.WordSpec
import edu.ucdavis.fiehnlab.ms.carrot.core.api._

/**
  * Created by wohlg_000 on 4/24/2016.
  */
class MSSpectraTest extends WordSpec {

  "MSSpectraTest" should {

    val spectra = testSpectraWith2Ions

    "basePeak" in {
      assert(spectra.spectrum.get.basePeak.mass == 100)
    }

    "splash" in {
      assert(spectra.spectrum.get.splash != null)
    }

  }
}
