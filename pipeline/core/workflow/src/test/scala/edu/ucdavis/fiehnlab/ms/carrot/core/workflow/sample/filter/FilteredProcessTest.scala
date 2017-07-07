package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra, MSSpectraImpl}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import org.scalatest.WordSpec
import edu.ucdavis.fiehnlab.ms.carrot.core.api._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Sample}

/**
  * Created by wohlg_000 on 4/22/2016.
  */
class FilteredProcessTest extends WordSpec {

  "FilteredProcessTest" should {

    "process - which should include everything" in {

      val filteredProcess = new FilteredProcess(
        List(new Filter[Feature]() {
          override def include(spectra: Feature): Boolean = true
        }),
        new WorkflowProperties
      )

      val result = filteredProcess.process(testSampleWith1Spectra)

      assert(result.spectra.size == 1)
    }
    "process - which should exclude everything" in {

      val filteredProcess = new FilteredProcess(
        List(new Filter[Feature]() {
          override def include(spectra: Feature): Boolean = false
        }),
        new WorkflowProperties
      )

      val result = filteredProcess.process(testSampleWith1Spectra)

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
        ),
        new WorkflowProperties
      )

      val result = filteredProcess.process(testSampleWith1Spectra)

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
  val testSpectraWith1Ion = MSSpectraImpl(scanNumber = 1, ions = Ion(100, 100) :: List(), retentionTimeInSeconds = 1000,accurateMass = Option(Ion(100,100)))

}
