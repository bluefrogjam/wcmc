package edu.ucdavis.fiehnlab.ms.carrot.core.db.binbase

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest
@ActiveProfiles(Array("carrot.gcms.library.binbase"))
class BinBaseLibraryAccessTest extends WordSpec with ShouldMatchers {

  @Autowired
  val binBaseLibraryAccess: BinBaseLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "BinBaseLibraryAccessTest" should {

    "load" in {
      val spectra = binBaseLibraryAccess.load(AcquisitionMethod(Option(ChromatographicMethod("binbase", None, Option("rtx5recal"), None))))

      spectra.size should be >=  100
    }

    "libraries" in {
      binBaseLibraryAccess.libraries.size should be(2)
    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class MonaLibraryAccessTestConfiguration {

}
