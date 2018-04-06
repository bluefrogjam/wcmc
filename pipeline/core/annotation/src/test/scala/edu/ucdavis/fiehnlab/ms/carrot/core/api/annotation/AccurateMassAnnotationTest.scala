package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation.Test._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSSpectra, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, Sample, Target}
import org.scalatest.WordSpec

object Test {
  /**
    * test spectra with 2 ions
    */
  val testAccurateMassSpectraWith4Ions = MSSpectraImpl(4, Ion(100.3241, 50) :: Ion(120.2132, 50) :: Ion(130.1321, 100) :: Ion(140.2224, 10) :: List(), 2000, massOfDetectedFeature = Option(Ion(100.3241, 100f)))

  val testAccurateLibraryMassSpectraWith4Ions = MSLibrarySpectraImpl(
    retentionIndex = 2324.2f,
    precursorMass = Some(100.3241),
    name = Some("test"),
    inchiKey = Some("BQJCRHHNABKAKU-KBQPJGBKSA-N"),
    io = Ion(100.3241, 50) :: Ion(120.2132, 50) :: Ion(130.1321, 100) :: Ion(140.2224, 10) :: List()
  )
  val testAccurateLibraryMassSpectra2With4Ions = MSLibrarySpectraImpl(
    retentionIndex = 2324.2f,
    precursorMass = Some(100.3111),
    name = Some("test"),
    inchiKey = Some("BQJCRHHNABKAKU-KBQPJGBKSA-N"),
    io = Ion(100.3241, 50) :: Ion(120.2132, 50) :: List()

  )

  /**
    * simple imple of an MSSpectra
    *
    * @param purity
    * @param scanNumber
    * @param retentionTimeInSeconds
    */
  final case class MSSpectraImpl(override val scanNumber: Int,
                                 io: Seq[Ion],
                                 override val retentionTimeInSeconds: Double,
                                 override val ionMode: Option[IonMode] = None,
                                 override val purity: Option[Double] = None,
                                 override val massOfDetectedFeature: Option[Ion]
                                ) extends MSSpectra {

    val sample:String = null
    /**
      * associated spectrum propties if applicable
      */
    override val associatedScan: Option[SpectrumProperties] = Some(new SpectrumProperties {
      /**
        * a list of model ions used during the deconvolution
        */
      override val modelIons: Option[Seq[Double]] = None
      /**
        * all the defined ions for this spectra
        */
      override lazy val ions: Seq[Ion] = io
      /**
        * the msLevel of this spectra
        */
      override val msLevel: Short = 1
    })
  }

  /**
    * default impl for a library spectra
    *
    * @param retentionIndex
    * @param name
    * @param inchiKey
    * @param precursorMass
    */
  sealed case class MSLibrarySpectraImpl(
                                          override val retentionIndex: Double,
                                          override var name: Option[String],
                                          override var inchiKey: Option[String],
                                          override val precursorMass: Option[Double],
                                          io: Seq[Ion]
                                        ) extends Target {
    /**
      * is this a confirmed target
      */
    override var confirmed: Boolean = false
    /**
      * is this target required for a successful retention index correction
      */
    override var requiredForCorrection: Boolean = false
    /**
      * is this a retention index correction standard
      */
    override var isRetentionIndexStandard: Boolean = false

    override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
      /**
        * a list of model ions used during the deconvolution
        */
      override val modelIons: Option[Seq[Double]] = None
      /**
        * all the defined ions for this spectra
        */
      override lazy val ions: Seq[Ion] = io
      /**
        * the msLevel of this spectra
        */
      override val msLevel: Short = 1
    })
  }

}

/**
  * Created by wohlg_000 on 6/13/2016.
  */
class AccurateMassAnnotationPPMTest extends WordSpec {

  "the accurate mass annotation " must {


    "match this spectra" in {
      val test = new AccurateMassAnnotationPPM(5,"")

      assert(test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectraWith4Ions))
    }

    "doesn't match this spectra" in {
      val test = new AccurateMassAnnotationPPM(5,"")

      assert(!test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectra2With4Ions))
    }
  }

}

class AccurateMassAnnotationTest extends WordSpec {

  "the accurate mass annotation " must {


    "match this spectra" in {
      val test = new AccurateMassAnnotation(0.005,0,"")

      assert(test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectraWith4Ions))
    }

    "doesn't match this spectra" in {
      val test = new AccurateMassAnnotation(0.005,0,"")

      assert(!test.isMatch(testAccurateMassSpectraWith4Ions, testAccurateLibraryMassSpectra2With4Ions))
    }
  }


}


