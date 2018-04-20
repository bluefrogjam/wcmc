package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import java.io.{File, FileWriter}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDialLCMSProcessedSample
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{ComponentScan, Configuration}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 1/30/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[LCTestConfig]))
@ActiveProfiles(Array("carrot.lcms"))
class MSDialLCMSProcessingTest extends WordSpec with Matchers with LazyLogging {

  @Autowired
  val msdProcessing: MSDialLCMSProcessing = null

  @Autowired
  val properties: MSDialLCMSProcessingProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDialLCMSProcessingTest" should {
    // Setting required processing properties
    properties.ionMode = PositiveMode()

    "check peakpicking" in {
      val sample: MSDKSample = MSDKSample("testA.mzml", new File(getClass.getResource("/testA.mzml").getFile))


      sample.spectra.size should be > 1

      val outSample = msdProcessing.process(sample, properties)

      logger.debug(s"Sample result: $outSample")
      outSample.spectra should not be null
      outSample.spectra.size should be > 0

      outSample shouldBe a[MSDialLCMSProcessedSample]
    }

    "check peakpicking in RT range (1.45 - 1.60)" in {
      val sample: MSDKSample = MSDKSample("testSmall0.mzml", new File(getClass.getResource("/testSmall0.mzml").getFile))

      val outSample = msdProcessing.process(sample, properties)

      outSample.spectra.size should be > 0
      outSample shouldBe a[MSDialLCMSProcessedSample]

      saveFile("testSmall0.carrot", outSample.asInstanceOf[MSDialLCMSProcessedSample])
    }

    "check peakpicking in RT range (10.00 - 10.44)" in {
      val sample: MSDKSample = MSDKSample("testSmall1.mzml", new File(getClass.getResource("/testSmall1.mzml").getFile))

      val outSample = msdProcessing.process(sample, properties)

      outSample.spectra.size should be > 0
      outSample shouldBe a[MSDialLCMSProcessedSample]

      saveFile("testSmall1.carrot", outSample.asInstanceOf[MSDialLCMSProcessedSample])
    }

    "check peakpicking in RT range (4.74 - 5.50)" in {
      val sample: MSDKSample = MSDKSample("testSmall2.mzml", new File(getClass.getResource("/testSmall2.mzml").getFile))

      val outSample = msdProcessing.process(sample, properties)

      outSample.spectra.size should be > 0
      outSample shouldBe a[MSDialLCMSProcessedSample]

      saveFile("testSmall2.carrot", outSample.asInstanceOf[MSDialLCMSProcessedSample])
    }

    def saveFile(filename: String, sample: MSDialLCMSProcessedSample): Unit = {
      val file = new File(getClass.getResource("/").getPath + s"/$filename")
      logger.info(s"Saving ${file.getName} ...")
      if(file.exists()) file.delete()

      val writer: FileWriter = new FileWriter(file)

      writer.append("Scan#\trt(min)\taccurate Mass\tIntensity\tMS1 spectrum\tMS2spectrum\n")

      sample.spectra.foreach {
        case spec@(t: MSMSSpectra) =>
          val s = spec.asInstanceOf[MSMSSpectra]
          writer.append(s"${s.scanNumber}\t")
            .append(s"${s.retentionTimeInMinutes.toFloat}\t")
            .append(s"${s.massOfDetectedFeature.get.mass.toFloat}\t")
            .append(s"${s.massOfDetectedFeature.get.intensity.toInt}\t")
            .append(s"${s.associatedScan.get.ions.map(ion => s"${ion.mass.toFloat}:${ion.intensity.toInt}").mkString(" ")}\t")
            .append(s"${s.spectrum.get.relativeSpectra.map(ion => s"${ion.mass.toFloat}:${ion.intensity.toInt}").mkString(" ")}\n")
        case spec@(t: Feature) =>
          writer.append(s"${spec.scanNumber}\t")
            .append(s"${spec.retentionTimeInMinutes.toFloat}\t")
            .append(s"${spec.massOfDetectedFeature.get.mass.toFloat}\t")
            .append(s"${spec.massOfDetectedFeature.get.intensity.toInt}\t")
            .append(s"${spec.associatedScan.get.ions.map(ion => s"${ion.mass.toFloat}:${ion.intensity.toInt}").mkString(" ")}\t")
            .append(s"\n")
      }

      writer.flush()
      writer.close()
      logger.info(s"... finished.")
    }
  }
}

@Configuration
@ComponentScan
class LCTestConfig {}
