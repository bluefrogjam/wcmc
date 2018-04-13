package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSample, IonMode, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 4/12/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("file.source.localhost","quantify-by-scan", "carrot.processing.peakdetection", "carrot.lcms", "carrot.logging.json.enable"))
class RICorrectionBugTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val targetLibraryLCMS: LibraryAccess[Target] = null

  @Autowired
  val loader: SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Retention Index Correction Process" should {
    logger.debug("minimumDefinedStandard " + correction.minimumDefinedStandard.toString)
    logger.debug("minimumFoundStandards " + correction.minimumFoundStandards.toString)
    logger.debug("massAccuracySetting " +correction.massAccuracySetting.toString)
    logger.debug("minPeakIntensity " + correction.minPeakIntensity.toString)

    "find the right pick (adduct?)" in {
      val method = AcquisitionMethod(Some(ChromatographicMethod("mytest", None, None, Some(new IonMode("positive")))))

      val sample: CorrectedSample = correction.process(
        deco.process(
          loader.getSample("Weiss005_posCSH_40298234_039.mzML"), method
        ), method
      )

      logger.debug(sample.featuresUsedForCorrection.mkString("\n"))

      sample should not be None
    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class CorrectionTestConfig {
  @Autowired
  val resourceLoader: ResourceLoader = null

  @Bean
  def targetLibraryLCMS: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets_20180315.txt").get, "\t")

}
