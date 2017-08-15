package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation.Test._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, Target}
import org.scalatest.WordSpec

object Test {
  /**
    * test spectra with 2 ions
    */
  val testAccurateMassSpectraWith4Ions = MSSpectraImpl(4, Ion(100.3241, 50) :: Ion(120.2132, 50) :: Ion(130.1321, 100) :: Ion(140.2224, 10) :: List(), 2000, massOfDetectedFeature = Option(Ion(100.3241, 100f)))

  val testAccurateLibraryMassSpectraWith4Ions = MSLibrarySpectraImpl(
    retentionTimeInSeconds = 2324.2f,
    precursorMass = Some(100.3241),
    name = Some("test"),
    inchiKey = Some("BQJCRHHNABKAKU-KBQPJGBKSA-N")
  )
  val testAccurateLibraryMassSpectra2With4Ions = MSLibrarySpectraImpl(
    retentionTimeInSeconds = 2324.2f,
    precursorMass = Some(100.3111),
    name = Some("test"),
    inchiKey = Some("BQJCRHHNABKAKU-KBQPJGBKSA-N")
  )

  /**
    * simple imple of an MSSpectra
    *
    * @param modelIons
    * @param purity
    * @param scanNumber
    * @param ions
    * @param retentionTimeInSeconds
    * @param msLevel
    */
  final case class MSSpectraImpl(override val scanNumber: Int,
                                 override val ions: Seq[Ion],
                                 override val retentionTimeInSeconds: Double,
                                 override val msLevel: Short = 1,
                                 override val ionMode: Option[IonMode] = None,
                                 override val modelIons: Option[Seq[Double]] = None,
                                 override val purity: Option[Double] = None,
                                 override val massOfDetectedFeature: Option[Ion]
                                ) extends MSSpectra

  /**
    * default impl for a library spectra
    *
    * @param retentionTimeInSeconds

    * @param name
    * @param inchiKey
    * @param precursorMass
    */
  sealed case class MSLibrarySpectraImpl(
                                          override val retentionTimeInSeconds: Double,
                                          override val name: Option[String],
                                          override val inchiKey: Option[String],
                                          override val precursorMass: Option[Double]
                                        ) extends Target {
    /**
      * is this a confirmed target
      */
    override val confirmed: Boolean = false
    /**
      * is this target required for a successful retention index correction
      */
    override val requiredForCorrection: Boolean = false
    /**
      * is this a retention index correction standard
      */
    override val isRetentionIndexStandard: Boolean = false
  }

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


