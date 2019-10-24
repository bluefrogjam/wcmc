package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.NegativeMode
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 3/6/2019
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest()
@ActiveProfiles(Array("carrot.lcms", "file.source.eclipse", "test"))
class MSMSProcessingTest extends WordSpec with Logging with Matchers {

  @Autowired
  val msdProcessing: MSDialLCMSProcessing = null

  @Autowired
  val properties: MSDialLCMSProcessingProperties = null

  @Autowired
  val sampleLoader: SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDialProcessing" should {

    "ensure that the sample exist" in {
      assert(sampleLoader.sampleExists("B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml"))
    }

    "have defined sample" in {
      val sample = sampleLoader.loadSample("B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml")
      sample shouldBe defined
    }

    "find MSMS spectra" in {
      val sample = sampleLoader.loadSample("B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml")
      properties.ionMode = NegativeMode()

      val deconvoluted = msdProcessing.process(sample.get, properties)

      deconvoluted.spectra.count(_.associatedScan.get.msLevel == 2) should be > 0
    }
  }
}

/*
B2b_SA1594_TEDDYLipids_Neg_1U2WN
B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN
B2b_SA1595_TEDDYLipids_Neg_1U6Y1
B2b_SA1595_TEDDYLipids_Neg_MSMS_1U6Y
B2b_SA1596_TEDDYLipids_Neg_1PL5A
B2b_SA1596_TEDDYLipids_Neg_MSMS_1PL5A
B2b_SA1597_TEDDYLipids_Neg_16VP2
B2b_SA1597_TEDDYLipids_Neg_MSMS_16VP2
B2b_SA1598_TEDDYLipids_Neg_11FY5
B2b_SA1598_TEDDYLipids_Neg_MSMS_11FY5
B2b_SA1599_TEDDYLipids_Neg_14QSQ
B2b_SA1599_TEDDYLipids_Neg_MSMS_14QSQ

 */
