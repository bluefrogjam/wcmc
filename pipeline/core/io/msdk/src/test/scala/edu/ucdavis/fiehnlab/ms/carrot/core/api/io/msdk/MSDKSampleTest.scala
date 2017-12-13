package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk

import java.io.File

import io.github.msdk.io.mzxml.MzXMLFileImportMethod
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 4/25/2016.
  */
class MSDKSampleTest extends WordSpec {

  "MSDKFileDelegateTest" must {

    "support mzml" should {

      val delegate:MSDKSample =  MSDKSample("test.mzML",new File("src/test/resources/test.mzML"))

      "unknownSpectra" in {
        assert(delegate.spectra.size == 7)
      }

      "fileName" in {
        assert(delegate.fileName != null)
      }

    }


    "support cdf" should {

      val delegate:MSDKSample =  MSDKSample("test.cdf",new File("src/test/resources/test.CDF"))

      "unknownSpectra" in {
        assert(delegate.spectra.size == 1278)
      }

      "fileName" in {
        assert(delegate.fileName != null)
      }
    }


    "support mzxml" should {

      val delegate:MSDKSample =  MSDKSample("test.mzXML",new File("src/test/resources/test.mzXML"))

      "unknownSpectra" in {
        assert(delegate.spectra.size == 1)
      }

      "fileName" in {
        assert(delegate.fileName != null)
      }
    }


  }
}
