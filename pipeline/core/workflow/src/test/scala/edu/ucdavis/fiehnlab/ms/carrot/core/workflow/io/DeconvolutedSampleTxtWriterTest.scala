package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.{File, FileOutputStream}
import java.util.zip.GZIPInputStream

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.{DeconvolutedSample, MSDialSample}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by diego on 3/1/2017.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
class DeconvolutedSampleTxtWriterTest extends WordSpec with Matchers with LazyLogging {
  val sample: MSDialSample = new MSDialSample(getClass.getResourceAsStream("/lipids/B5_P20Lipids_Pos_Blank000.msdial"), "B5_P20Lipids_Pos_Blank000.msdial") with DeconvolutedSample

  val writer: SampleTXTWriter = new SampleTXTWriter()

  /*val os: OutputStream = */

  "DeconvolutedSampleTxtWriter" should {
    "write a text file" in {

      val tmpFile: File = File.createTempFile("carrotTest",".txt")
      tmpFile should have length 0

      val os: FileOutputStream = new FileOutputStream(tmpFile)

      writer.write(os, sample)

      tmpFile.exists === true

      assert(tmpFile.length  > 0)
    }
  }
}
