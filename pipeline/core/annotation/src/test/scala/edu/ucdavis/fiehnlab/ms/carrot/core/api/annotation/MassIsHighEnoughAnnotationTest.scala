package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import org.scalatest.WordSpec

import Test._

class MassIsHighEnoughAnnotationTest extends WordSpec {

  "the accurate mass annotation " must {


    "match this spectra with 0 intensity" in {
      val test = new MassIsHighEnoughAnnotation(0.005, 0)
      assert(test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectraWith4Ions))
    }


    "match this spectra with 10 intensity" in {
      val test = new MassIsHighEnoughAnnotation(0.005, 10)
      assert(test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectraWith4Ions))
    }

    "match this spectra with 50 intensity" in {
      val test = new MassIsHighEnoughAnnotation(0.005, 50)
      assert(test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectraWith4Ions))
    }

    "does not match this spectra with 160 intensity" in {
      val test = new MassIsHighEnoughAnnotation(0.005, 160)
      assert(!test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectraWith4Ions))
    }

    "doesn't match this spectra" in {
      val test = new MassIsHighEnoughAnnotation(0.005, 0)

      assert(!test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectra2With4Ions))
    }
  }
}
