package edu.ucdavis.fiehnlab.ms.carrot.core.db.binbase

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.GCMSCorrectionTarget
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest
@ActiveProfiles(Array("carrot.gcms.library.binbase","carrot.gcms"))
class BinBaseLibraryAccessTest extends WordSpec with ShouldMatchers {

  @Autowired
  val binBaseLibraryAccess: BinBaseLibraryAccess = null

  @Autowired
  val  correction:LibraryAccess[GCMSCorrectionTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "BinBaseLibraryAccessTest" should {

    "load" in {
      val method = AcquisitionMethod(ChromatographicMethod("binbase", None, Option("rtx5recal"), None))
      val spectra = binBaseLibraryAccess.load(method)


      //we need more than 1000 bins in it
      spectra.size should be >=  1000
    }

    "load must also merge the correction standards into the library" in {
      val method = AcquisitionMethod(ChromatographicMethod("binbase", None, Option("rtx5recal"), None))
      val spectra = binBaseLibraryAccess.load(method)
      val markers = correction.load(method)

      //this library should have been merged with the marker library
      spectra.collect{
        case x:GCMSCorrectionTarget =>
      }.size should be (markers.size)

      spectra.collect{
        case x:BinBaseLibraryTarget =>
      }.size should be >= 1000


    }

    "libraries" in {
      binBaseLibraryAccess.libraries.size should be(2)
    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class MonaLibraryAccessTestConfiguration {

}
