package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import java.io.File

import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{PositiveMode, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 2/7/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("carrot.processing.peakdetection", "carrot.report.quantify.height", "carrot.lcms", "test","carrot.targets.yaml.annotation","carrot.targets.yaml.correction"))
class PPAndDTest extends WordSpec with Matchers with Logging {
  val libName = "lcms_istds"

  @Autowired
  val peakDetection: PeakDetection = null

  @Autowired
  val stasis_cli: StasisService = null

  val method = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(PositiveMode())))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "PeakDetection" should {
    "process mzml sample" in {

      logger.info(new File("../msdial/src/test/resources/testSmall0.mzml").getAbsolutePath)

      val sample = MSDKSample("testSmall0.mzml", new File("../msdial/src/test/resources/testSmall0.mzml"))
      sample.spectra should not be Seq.empty
      sample.spectra should have size 18

      val result = peakDetection.process(sample, method, None)
      result should not be Seq.empty

      val deconv = result.asInstanceOf[Sample]
      deconv.spectra should not be Seq.empty
      deconv.spectra should have size 125

      stasis_cli.getTracking(sample.name).status.map(_.value) should contain("deconvoluted")
    }
  }
}

@Configuration
class PPAndDTestConfiguration {
  @Autowired
  val resourceLoader: DelegatingResourceLoader = null

  @Bean
  def resourceLoaderSrc: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))
}
