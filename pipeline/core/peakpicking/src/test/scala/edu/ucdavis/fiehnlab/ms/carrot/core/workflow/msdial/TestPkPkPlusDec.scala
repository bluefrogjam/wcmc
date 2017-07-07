package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial

import java.util.{ArrayList => JArrayList, List => JList}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Centroided, Profiled}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.{LocalDirectorySampleLoader, LocalDirectorySampleLoaderProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution.MS1Deconvolution
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.{MsdialGCBasedPeakSpotting, MSDialPreProcessingProperties}
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
  * Created by diego on 9/20/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[PPDTestConfiguration]))
class TestPkPkPlusDec extends WordSpec with Matchers with LazyLogging {

  @Autowired
  val peakpicker: MsdialGCBasedPeakSpotting = null

  @Autowired
  val deconvoluter: MS1Deconvolution = null

  @Autowired
  val ppdSampleLoader: LocalDirectorySampleLoader = null

  @Autowired
  val mSDialPreProcessingProperties: MSDialPreProcessingProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "PeakPickingPlusDeconvolution" should {
    mSDialPreProcessingProperties.retentionTimeBegin = 0.5
    mSDialPreProcessingProperties.retentionTimeEnd = 12.5
    mSDialPreProcessingProperties.amplitudeCutoff = 10
    mSDialPreProcessingProperties.massRangeBegin = 280
    mSDialPreProcessingProperties.massRangeEnd = 1700
    mSDialPreProcessingProperties.averagePeakWidth = 5
    mSDialPreProcessingProperties.minimumAmplitude = 2500
    mSDialPreProcessingProperties.massSliceWidth = 0.1
    mSDialPreProcessingProperties.massAccuracy = 0.05

    println("reading sample data... ")
    ppdSampleLoader should not be null

    logger.debug("Folders: " + ppdSampleLoader.properties.directories)

    val sample: Sample = ppdSampleLoader.loadSample("pp-zerofilter.mzXML").get // full file
    val rawSpectra = getRawSpectra(sample)

    "load sample data" in {
      sample should not be null
      sample.spectra should not be empty
    }

    "process sample file" in {
      logger.info(s"processing ${sample.spectra.length} spectra in sample: ${sample.name}")

      val start = System.currentTimeMillis()
      val detectedPeaks = peakpicker.detectPeaks(rawSpectra, mSDialPreProcessingProperties)
      logger.debug(s"peakpicking took ${(System.currentTimeMillis() - start) / 1000}s (${System.currentTimeMillis() - start}ms)")

      val msdialCount = 5382
      val delta = (0.01 * msdialCount).toInt
      logger.debug("Allowed result size difference (1%): ±" + delta + " peaks")
      logger.debug("Actual result size difference: " + Math.abs(msdialCount - detectedPeaks.size()) + " peaks")

      detectedPeaks should not be null
      detectedPeaks.size should be (msdialCount +- delta)

      logger.debug("Starting deconvolution...")
      logger.debug("sending raw spectra: " + rawSpectra.size + "\npeaks: " + detectedPeaks.size() + "\nproperties: " + (mSDialPreProcessingProperties != null))

      val deconvolutedSample = deconvoluter.gcmsMS1DecResults(rawSpectra, detectedPeaks, mSDialPreProcessingProperties)

      deconvolutedSample should not be null
      logger.debug("deconvoluted peaks: " + deconvolutedSample.size())
      deconvolutedSample.size() should be (370 +- 5)

    }
  }

  def getRawSpectra(sample: Sample): JList[RawSpectrum] = {
    val rawSpectra: JList[RawSpectrum] = new JArrayList[RawSpectrum](sample.spectra.size)

    sample.spectra.foreach {
      case centroided: Centroided =>
        rawSpectra.add(new RawSpectrum(centroided))
      case profile =>
        rawSpectra.add(new RawSpectrum(profile.asInstanceOf[Profiled]))
    }

    for (i <- 0 until rawSpectra.size) {
      rawSpectra.get(i).scanNum = i
    }

    rawSpectra
  }

}

@Configuration
@ComponentScan(basePackages = Array("edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection"))
class PPDTestConfiguration extends LazyLogging {
  @Bean
  def resourceLoaderDelegate: DelegatingResourceLoader = new DelegatingResourceLoader()

  @Bean
  def deconvoluter: MS1Deconvolution = new MS1Deconvolution()

  @Bean
  def ppdSampleLoader(properties: LocalDirectorySampleLoaderProperties): SampleLoader = {
    logger.debug(" -----------------  creating sample loader -----------------")
    properties.directories = (
      "src/test/resources/mzxml" ::
        "G:\\Data\\carrot\\P20-lipids\\carrot" ::
        "./" :: List()).asJava

    new LocalDirectorySampleLoader(properties)
  }
}