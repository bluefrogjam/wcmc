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
      * associated spectrum propties if applicable
      */
    override val spectrum:Option[SpectrumProperties] = Some(new SpectrumProperties {
      /**
        * a list of model ions used during the deconvolution
        */
      override val modelIons: Option[Seq[Double]] = None
      /**
        * all the defined ions for this spectra
        */
      override val ions: Seq[Ion] = Ion(100, 100) :: List()
      /**
        * the msLevel of this spectra
        */
      override val msLevel: Short = 1
    })
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

    override val spectrum:Option[SpectrumProperties] = Some(new SpectrumProperties {
      /**
        * a list of model ions used during the deconvolution
        */
      override val modelIons: Option[Seq[Double]] = None
      /**
        * all the defined ions for this spectra
        */
      override val ions: Seq[Ion] = Ion(100, 100) :: Ion(120, 50) :: List()
      override val msLevel: Short = 1
    })

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


    override val spectrum:Option[SpectrumProperties] = Some(new SpectrumProperties {
      /**
        * a list of model ions used during the deconvolution
        */
      override val modelIons: Option[Seq[Double]] = None
      /**
        * all the defined ions for this spectra
        */
      override val ions: Seq[Ion] = Ion(100, 10) :: Ion(120, 50) :: Ion(130, 100) :: List()
      override val msLevel: Short = 1
    })


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

    override val spectrum:Option[SpectrumProperties] = Some(new SpectrumProperties {

      /**
        * a list of model ions used during the deconvolution
        */
      override val modelIons: Option[Seq[Double]] = None
      /**
        * all the defined ions for this spectra
        */
      override val ions: Seq[Ion] = Ion(100, 50) :: Ion(120, 50) :: Ion(130, 100) :: Ion(140, 10) :: List()
      override val msLevel: Short = 1
    })

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

    override val spectrum:Option[SpectrumProperties] = Some(new SpectrumProperties {

      /**
        * a list of model ions used during the deconvolution
        */
      override val modelIons: Option[Seq[Double]] = None
      /**
        * all the defined ions for this spectra
        */
      override val ions: Seq[Ion] = Ion(100.3241, 50) :: Ion(120.2132, 50) :: Ion(130.1321, 100) :: Ion(140.2224, 10) :: List()
      override val msLevel: Short = 1
    })

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

    override val spectrum:Option[SpectrumProperties] = Some(new SpectrumProperties {

      /**
        * a list of model ions used during the deconvolution
        */
      override val modelIons: Option[Seq[Double]] = None
      /**
        * all the defined ions for this spectra
        */
      override val ions: Seq[Ion] = Ion(100.3241, 50) :: Ion(100.3242, 50) :: Ion(100.2339, 100) :: Ion(140.2224, 10) :: List()
      override val msLevel: Short = 1
    })
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

}
