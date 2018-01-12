package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode, Sample}
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 4/22/2016.
  */
class FilteredProcessTest extends WordSpec {

  "FilteredProcessTest" should {
    val method = AcquisitionMethod(None)
    "process - which should include everything" in {

      val filteredProcess = new FilteredProcess(
        List(new Filter[Feature]() {
          override def include(spectra: Feature): Boolean = true
        })
      )

      val result = filteredProcess.process(testSampleWith1Spectra, method)

      assert(result.spectra.size == 1)
    }
    "process - which should exclude everything" in {

      val filteredProcess = new FilteredProcess(
        List(new Filter[Feature]() {
          override def include(spectra: Feature): Boolean = false
        })
      )

      val result = filteredProcess.process(testSampleWith1Spectra, method)

      assert(result.spectra.isEmpty)
    }
    "process - which should exclude everything, since 1 filter fails" in {

      val filteredProcess = new FilteredProcess(
        List(

          new Filter[Feature]() {
            override def include(spectra: Feature): Boolean = false
          },
          new Filter[Feature]() {
            override def include(spectra: Feature): Boolean = true
          }
        )
      )

      val result = filteredProcess.process(testSampleWith1Spectra, method)

      assert(result.spectra.isEmpty)
    }

  }


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

    val sample:Sample = null

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

    /**
      * associated spectrum propties if applicable
      */
    override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
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

  }

}
