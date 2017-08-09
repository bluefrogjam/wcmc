package edu.ucdavis.fiehnlab.ms.carrot.core

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._

/**
  * a couple of test objects, which should be used in the api test
  */
package object api {

  /**
    * a simple static test sample with 1 spectra
    */
  val testSampleWith1Spectra = new Sample {

    //we define 1 spectra for testing
    override val spectra: List[_ <: MSSpectra] = testSpectraWith1Ion :: List()

    override val fileName: String = "test"
  }

  /**
    * simple test spectra with 1 ion
    */
  val testSpectraWith1Ion = new MSSpectra {
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[Seq[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = Ion(100, 100) :: List()
    /**
      * specified ion mode for the given feature
      */
    override val ionMode: Option[IonMode] = None
    /**
      * how pure this spectra is
      */
    override val purity: Option[Double] = None
    /**
      * the retention time of this spectra. It should be provided in seconds!
      */
    override val retentionTimeInSeconds: Double = 1000
    /**
      * the local scan number
      */
    override val scanNumber: Int = 1
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = Option(Ion(100, 100))

  }
  /**
    * test spectra with 2 ions
    */
  val testSpectraWith2Ions = new MSSpectra {
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[Seq[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = Ion(100, 100) :: Ion(120, 50) :: List()
    /**
      * specified ion mode for the given feature
      */
    override val ionMode: Option[IonMode] = None
    /**
      * how pure this spectra is
      */
    override val purity: Option[Double] = None
    /**
      * the retention time of this spectra. It should be provided in seconds!
      */
    override val retentionTimeInSeconds: Double = 2000
    /**
      * the local scan number
      */
    override val scanNumber: Int = 2
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = Option(Ion(100, 100))
  }


  //MSSpectraImpl(2, Ion(100, 100) :: Ion(120, 50) :: List(), 2000,massOfDetectedFeature = Option(Ion(100,100)))

  /**
    * test spectra with 2 ions
    */
  val testSpectraWith3Ions = new MSSpectra {
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[Seq[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = Ion(100, 10) :: Ion(120, 50) :: Ion(130, 100) :: List()
    /**
      * specified ion mode for the given feature
      */
    override val ionMode: Option[IonMode] = None
    /**
      * how pure this spectra is
      */
    override val purity: Option[Double] = None
    /**
      * the retention time of this spectra. It should be provided in seconds!
      */
    override val retentionTimeInSeconds: Double = 3000
    /**
      * the local scan number
      */
    override val scanNumber: Int = 3
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = Option(Ion(100, 100))
  }


  //MSSpectraImpl(3, Ion(100, 10) :: Ion(120, 50) :: Ion(130, 100) :: List(), 3000,massOfDetectedFeature = Option(Ion(100,100)))

  /**
    * test spectra with 2 ions
    */
  val testSpectraWith4Ions = new MSSpectra {
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[Seq[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = Ion(100, 50) :: Ion(120, 50) :: Ion(130, 100) :: Ion(140, 10) :: List()
    /**
      * specified ion mode for the given feature
      */
    override val ionMode: Option[IonMode] = None
    /**
      * how pure this spectra is
      */
    override val purity: Option[Double] = None
    /**
      * the retention time of this spectra. It should be provided in seconds!
      */
    override val retentionTimeInSeconds: Double = 4000
    /**
      * the local scan number
      */
    override val scanNumber: Int = 4
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = Option(Ion(100, 100))
  }


  //MSSpectraImpl(4, Ion(100, 50) :: Ion(120, 50) :: Ion(130, 100) :: Ion(140, 10) :: List(), 4000, massOfDetectedFeature = Option(Ion(100, 100)))

  val testAccurateMassSpectraWith4Ions = new MSSpectra {
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[Seq[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = Ion(100.3241, 50) :: Ion(120.2132, 50) :: Ion(130.1321, 100) :: Ion(140.2224, 10) :: List()
    /**
      * specified ion mode for the given feature
      */
    override val ionMode: Option[IonMode] = None
    /**
      * how pure this spectra is
      */
    override val purity: Option[Double] = None
    /**
      * the retention time of this spectra. It should be provided in seconds!
      */
    override val retentionTimeInSeconds: Double = 5000
    /**
      * the local scan number
      */
    override val scanNumber: Int = 5
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = Option(Ion(100.3241, 50))
  }

  //MSSpectraImpl(5, Ion(100.3241, 50) :: Ion(120.2132, 50) :: Ion(130.1321, 100) :: Ion(140.2224, 10) :: List(), 5000, massOfDetectedFeature = Option(Ion(100.3241, 50)))
  val testAccurateMassSpectraWith4Ions2 = new MSSpectra {
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[Seq[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = Ion(100.3241, 50) :: Ion(100.3242, 50) :: Ion(100.2339, 100) :: Ion(140.2224, 10) :: List()
    /**
      * specified ion mode for the given feature
      */
    override val ionMode: Option[IonMode] = None
    /**
      * how pure this spectra is
      */
    override val purity: Option[Double] = None
    /**
      * the retention time of this spectra. It should be provided in seconds!
      */
    override val retentionTimeInSeconds: Double = 6000
    /**
      * the local scan number
      */
    override val scanNumber: Int = 6
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = Option(Ion(100.3241, 50))
  }


  //MSSpectraImpl(6, Ion(100.3241, 50) :: Ion(100.3242, 50) :: Ion(100.2339, 100) :: Ion(140.2224, 10) :: List(), 6000, massOfDetectedFeature = Option(Ion(100.3241, 50)))

  val testAccurateLinraryMassSpectraWith4Ions = new MSLibrarySpectra {
    /**
      * this ion is used for quantification of data
      * and used for height calculations
      */
    override val quantificationIon: Option[Double] = Some(100.3241)
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[Seq[Double]] = None
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = Ion(100.3246, 50) :: Ion(120.2136, 50) :: Ion(130.1326, 100) :: Ion(140.2226, 10) :: List()
    /**
      * specified ion mode for the given feature
      */
    override val ionMode: Option[IonMode] = None
    /**
      * how pure this spectra is
      */
    override val purity: Option[Double] = None
    /**
      * the local scan number
      */
    override val scanNumber: Int = 4
    /**
      * accurate mass of this feature, if applicable
      */
    override val massOfDetectedFeature: Option[Ion] = None
    /**
      * the mono isotopic mass of this spectra
      */
    override val monoIsotopicMass: Option[Double] = Some(100.3241)
    /**
      * the unique inchi key for this spectra
      */
    override val inchiKey: Option[String] = Some("BQJCRHHNABKAKU-KBQPJGBKSA-N")
    /**
      * retention time in seconds of this target
      */
    override val retentionTimeInSeconds: Double = 2000
    /**
      * a name for this spectra
      */
    override val name: Option[String] = Some("test")
    /**
      * is this a confirmed target
      */
    override val confirmedTarget: Boolean = false
    /**
      * is this target required for a successful retention index correction
      */
    override val requiredForCorrection: Boolean = false
    /**
      * is this a retention index correction standard
      */
    override val isRetentionIndexStandard: Boolean = false
  }


  /*MSLibrarySpectraImpl
  (
    4,
    Ion(100.3246, 50) :: Ion(120.2136, 50) :: Ion(130.1326, 100) :: Ion(140.2226, 10) :: List(),
    2324.2f,
    1,
    Some(100.3241),
    Some("test"),
    Some("BQJCRHHNABKAKU-KBQPJGBKSA-N"),
    Some(2324.2f),
    2000
  )

*/
}
