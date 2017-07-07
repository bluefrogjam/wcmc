package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSLibrarySpectra, MSLibrarySpectraImpl, MSSpectra, MSSpectraImpl}
import org.scalatest.WordSpec
import Test._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion

object Test {
  /**
    * test spectra with 2 ions
    */
  val testAccurateMassSpectraWith4Ions = MSSpectraImpl(4, Ion(100.3241, 50) :: Ion(120.2132, 50) :: Ion(130.1321, 100) :: Ion(140.2224, 10) :: List(), 2000,accurateMass =Option( Ion(100.3241,100f) ))

  val testAccurateLibraryMassSpectraWith4Ions = MSLibrarySpectraImpl(
    4,
    Ion(100.3246, 50) :: Ion(120.2136, 50) :: Ion(130.1326, 100) :: Ion(140.2226, 10) :: List(),
    2324.2f,
    1,
    Some(100.3241),
    Some("test"),
    Some("BQJCRHHNABKAKU-KBQPJGBKSA-N"),
    Some(100.3241)
  )
  val testAccurateLibraryMassSpectra2With4Ions = MSLibrarySpectraImpl(
    4,
    Ion(100.3146, 50) :: Ion(12.2136, 50) :: Ion(32.1326, 100) :: Ion(140.2226, 10) :: List(),
    2324.2f,
    1,
    Some(100.3111),
    Some("test"),
    Some("BQJCRHHNABKAKU-KBQPJGBKSA-N"),
    Some(100.3111)
  )

}

/**
  * Created by wohlg_000 on 6/13/2016.
  */
class AccurateMassAnnotationPPMTest extends WordSpec {

  "the accurate mass annotation " must {


    "match this spectra" in {
      val test = new AccurateMassAnnotationPPM(5)

      assert(test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectraWith4Ions))
    }

    "doesn't match this spectra" in {
      val test = new AccurateMassAnnotationPPM(5)

      assert(!test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectra2With4Ions))
    }
  }

}

class AccurateMassAnnotationTest extends WordSpec {

  "the accurate mass annotation " must {


    "match this spectra" in {
      val test = new AccurateMassAnnotation(0.005)

      assert(test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectraWith4Ions))
    }

    "doesn't match this spectra" in {
      val test = new AccurateMassAnnotation(0.005)

      assert(!test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectra2With4Ions))
    }
  }


}


