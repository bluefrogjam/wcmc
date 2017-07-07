package edu.ucdavis.fiehnlab.ms.carrot.core.io.mzxml

import java.io.{File, FileInputStream}
import java.util.zip.GZIPInputStream

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import org.scalatest.Matchers._
import org.scalatest.WordSpec
/**
  * Created by wohlgemuth on 8/8/16.
  */
class MzXMLSampleTest extends WordSpec with LazyLogging{

  "MzXMLSampleTest" when {

    "read a sample with 1 spectra" must {

      val sample = new MzXMLSample(getClass.getResourceAsStream("/test.mzXML"),"test.mzXML")

      "have 1 spectra" in {
        sample.spectra.size shouldBe 1

        println(sample.spectra.head.spectraString)
      }

      "we have 1 spectra" when {

        val spectra = sample.spectra.head

        "we need to have a ms level of 1" in {
          spectra.msLevel shouldBe 1
        }

        "we need to have a retention time of 0"  in {
          spectra.retentionTimeInSeconds shouldBe 0
        }

        "we have to have a positive ion mode" in {
          spectra.ionMode.get.isInstanceOf[PositiveMode] shouldBe true
        }
        "we have 22431 peaks" in {
          spectra.ions.size shouldBe 22431
        }
        "we have a base peak of 4211.05245014" in {
          spectra.basePeak.mass shouldBe 4211.05245014 +- 0.0005
          spectra.basePeak.intensity shouldBe 29707f +- 0.1f
        }


      }
    }

    "able to read a large mzXML file" must {

      val sample = new MzXMLSample(new GZIPInputStream(getClass.getResourceAsStream("/Pos_QC005.mzXML.gz")),"Pos_QC005.mzXML.gz")

      "we should have spectra" in {
        sample.spectra should not be empty
      }

      "spectras must have ions" in {
        var run:Boolean = false
        sample.spectra.foreach{ spectra =>
          spectra.ions should not be empty
          run = true

        }

        run shouldBe true
      }


      "spectra's must have tic > 0" in {
        var run:Boolean = false
        sample.spectra.foreach{ spectra =>
          spectra.tic should not be 0
          run = true

        }

        run should not be false
      }

      "spectra's must have retention time > 0" in {
        var run:Boolean = false
        sample.spectra.foreach{ spectra =>
          spectra.retentionTimeInSeconds should not be 0
          run = true

        }

        run should not be false
      }


      "be identical to MSDK implementation" must {

        val msdk = MSDKSample(new File("src/test/resources/Pos_QC005.mzXML.gz"))

        "have same spectra size" in {
          msdk.spectra.size shouldBe sample.spectra.size
        }

        "have identical spectra" in {

          var run = false
          msdk.spectra.sortBy(_.retentionTimeInSeconds).zip(sample.spectra.sortBy(_.retentionTimeInSeconds)).foreach{ values:(MSSpectra,MSSpectra) =>
            values._1.spectraString shouldBe values._2.spectraString
            run = true
          }

          run should not be false
        }

        "have identical scans ids" in {

          msdk.spectra.sortBy(_.retentionTimeInSeconds).zip(sample.spectra.sortBy(_.retentionTimeInSeconds)).foreach{ values:(MSSpectra,MSSpectra) =>

            values._1.scanNumber shouldBe values._2.scanNumber

          }
        }


        "have identical retention times" in {

          msdk.spectra.sortBy(_.retentionTimeInSeconds).zip(sample.spectra.sortBy(_.retentionTimeInSeconds)).foreach{ values:(MSSpectra,MSSpectra) =>

            values._1.retentionTimeInSeconds shouldBe values._2.retentionTimeInSeconds +- 0.005

          }
        }
      }
    }
  }
}
