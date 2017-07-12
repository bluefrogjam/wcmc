package edu.ucdavis.fiehnlab.ms.carrot.core

import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.RetentionTimeDifference
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._

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
  val testSpectraWith1Ion = MSSpectraImpl(scanNumber = 1, ions = Ion(100, 100) :: List(), retentionTimeInSeconds = 1000,accurateMass = Option(Ion(100,100)))

  /**
    * test spectra with 2 ions
    */
  val testSpectraWith2Ions = MSSpectraImpl(2, Ion(100, 100) :: Ion(120, 50) :: List(), 2000,accurateMass = Option(Ion(100,100)))

  /**
    * test spectra with 2 ions
    */
  val testSpectraWith3Ions = MSSpectraImpl(3, Ion(100, 10) :: Ion(120, 50) :: Ion(130, 100) :: List(), 3000,accurateMass = Option(Ion(100,100)))

  /**
    * test spectra with 2 ions
    */
  val testSpectraWith4Ions = MSSpectraImpl(4, Ion(100, 50) :: Ion(120, 50) :: Ion(130, 100) :: Ion(140, 10) :: List(), 4000,accurateMass = Option(Ion(100,100)))

  val testAccurateMassSpectraWith4Ions = MSSpectraImpl(5, Ion(100.3241, 50) :: Ion(120.2132, 50) :: Ion(130.1321, 100) :: Ion(140.2224, 10) :: List(), 5000,accurateMass = Option(Ion(100.3241,50)))
  val testAccurateMassSpectraWith4Ions2 = MSSpectraImpl(6, Ion(100.3241, 50) :: Ion(100.3242, 50) :: Ion(100.2339, 100) :: Ion(140.2224, 10) :: List(), 6000,accurateMass = Option(Ion(100.3241,50)))

  val testAccurateLinraryMassSpectraWith4Ions = MSLibrarySpectraImpl
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
                                 override val accurateMass: Option[Ion]
                                ) extends MSSpectra

  /**
    * default impl for a library spectra
    *
    * @param scanNumber
    * @param ions
    * @param retentionTimeInSeconds
    * @param msLevel
    * @param ionMode
    * @param modelIons
    * @param purity
    * @param quantificationIon
    * @param name
    * @param inchiKey
    * @param monoIsotopicMass
    */
  sealed case class MSLibrarySpectraImpl(
                                          override val scanNumber: Int,
                                          override val ions: Seq[Ion],
                                          override val retentionTimeInSeconds: Double,
                                          override val msLevel: Short = 1,
                                          override val quantificationIon: Option[Double],
                                          override val name: Option[String],
                                          override val inchiKey: Option[String],
                                          override val monoIsotopicMass: Option[Double],
                                          override val ionMode: Option[IonMode] = None,
                                          override val modelIons: Option[Seq[Double]] = None,
                                          override val purity: Option[Double] = None,
                                          override val accurateMass: Option[Ion] =None

                                        ) extends MSLibrarySpectra {
  }

}
