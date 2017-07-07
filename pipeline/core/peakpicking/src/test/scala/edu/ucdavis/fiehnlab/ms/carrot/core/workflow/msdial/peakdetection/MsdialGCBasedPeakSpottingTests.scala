package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection

import java.util.{ArrayList => JArrayList, List => JList}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.RawSpectrum
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._

/**
  * Created by diego on 8/26/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[PeakDetectionTestConfiguration]))
class MSDialGCBasedPeakSpottingTests extends WordSpec with Matchers with LazyLogging {

  @Autowired
  val peakpicker: MsdialGCBasedPeakSpotting = null

  @Autowired
  val sampleLoader: LocalDirectorySampleLoader = null

  @Autowired
  val mSDialPreProcessingProperties: MSDialPreProcessingProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "GC based peak detection" should {
    logger.debug("reading sample data... ")
    val start = System.currentTimeMillis()
    val sample: Sample = sampleLoader.loadSample("B6C_zeroclean_cent_Pos_QC001.mzXML").get // full file
    logger.debug(s"peakpicking took ${(System.currentTimeMillis() - start) / 1000}s (${System.currentTimeMillis() - start}ms)")

    val rawSpectra: JList[RawSpectrum] = new JArrayList()
    sample.spectra.foreach { spec: MSSpectra =>
      rawSpectra.add(new RawSpectrum(spec))
    }
    for (i: Int <- 0 until rawSpectra.size) {
      rawSpectra.get(i).scanNum = i
    }

    "load a sample with data in it" in {
      sample.spectra should not be empty
    }

    "process raw data from sample" in {
      logger.info(s"processing ${sample.spectra.length} spectra in sample: ${sample.name}")

      mSDialPreProcessingProperties.retentionTimeBegin = 2.1
      mSDialPreProcessingProperties.retentionTimeEnd = 2.4
      mSDialPreProcessingProperties.massAccuracy = 0.01

      val start = System.currentTimeMillis()
      val ppSample = peakpicker.detectPeaks(rawSpectra, mSDialPreProcessingProperties)
      logger.debug(s"peakpicking took ${(System.currentTimeMillis() - start) / 1000}s (${System.currentTimeMillis() - start}ms)")

      ppSample should not be null
      ppSample.size should be(42 +- 2)

    }

    "remove duplicate peaks" in {

      val results = peakpicker.getPeakSpots(rawSpectra, mSDialPreProcessingProperties)

      results.asScala.count(_.scanNumberAtPeakTop == 5) shouldBe 3
    }

    "finds all peaks in full file" in {
      logger.info(s"processing ${sample.spectra.length} spectra in sample: ${sample.name}")
      mSDialPreProcessingProperties.retentionTimeBegin = 0.5
      mSDialPreProcessingProperties.retentionTimeEnd = 12.5
      val start = System.currentTimeMillis()
      val ppSample = peakpicker.getPeakSpots(rawSpectra, mSDialPreProcessingProperties)
      logger.debug(s"peakpicking took ${(System.currentTimeMillis() - start) / 1000}s (${System.currentTimeMillis() - start}ms)")

      ppSample should not be null
      ppSample.size should be(10123 +- 10)
    }
  }
}

@Configuration
@ComponentScan(basePackages = Array("edu.ucdavis.fiehnlab.ms.carrot.core.workflow"))
class PeakDetectionTestConfiguration extends LazyLogging {
  @Bean
  def resourceLoader: DelegatingResourceLoader = new DelegatingResourceLoader()

  @Bean
  def peakpicker: MsdialGCBasedPeakSpotting = new MsdialGCBasedPeakSpotting()

  @Bean
  def sampleLoader(properties: LocalDirectorySampleLoaderProperties): SampleLoader = {
    properties.directories = ("src/test/resources/mzxml" ::
      "G:\\Data\\carrot\\stephanie\\mzXML" ::
      "G:\\Data\\carrot\\stephanie\\mzXML\\New Folder" ::
      "./" :: List()).asJava

    new LocalDirectorySampleLoader(properties)
  }


  @Bean
  def mSDialPreProcessingProperties: MSDialPreProcessingProperties = new MSDialPreProcessingProperties()

}