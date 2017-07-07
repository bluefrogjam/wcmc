package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.{BufferedInputStream, FileInputStream, FileOutputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing.PurityProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.io.Source
import org.scalatest.Matchers._
import org.scalatest.{WordSpec, _}
/**
  * Created by wohlg on 7/9/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("common"))
class QuantifiedSampleTxtWriterTest extends WordSpec with LazyLogging{


  @Autowired
  val correction: LCMSTargetRetentionIndexCorrection = null

  @Autowired
  val purity: PurityProcessing = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  @Qualifier("quantification")
  val quantification: QuantifyByHeightProcess = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "QuantifiedSampleTxtWriterTest" should {


    val samples: List[_ <: Sample] = new MSDialSample(getClass.getResourceAsStream("/lipids/B5_P20Lipids_Pos_NIST02.msdial"), "B5_P20Lipids_Pos_NIST02.msdial") :: new MSDialSample(getClass.getResourceAsStream("/lipids/B5_SA0002_P20Lipids_Pos_1FL_1006.msdial"), "B5_SA0002_P20Lipids_Pos_1FL_1006.msdial") :: List()

    //correct the data
    val correctedSample = samples.map(correction.process)

    val annotated = correctedSample.map(annotation.process)

    val results = annotated.map(quantification.process)

    "write" in {

      results.size should be > 0

      val out = new FileOutputStream("target/test.txt")

      val seperator = "\t"
      val writer = new QuantifiedSampleTxtWriter[Double](seperator)

      writer.writeHeader(out)
      results.foreach{
        writer.write(out,_)
      }
      writer.writeFooter(out)

      out.flush()
      out.close()


      val lines = Source.fromInputStream(new FileInputStream("target/test.txt")).getLines().toList

      lines.foreach{ line =>
        logger.info(s"${line}")
      }

      assert(lines != null)
      lines.size shouldBe 2 + 4

      lines(0).split(seperator)(0) == "file"
      lines(4).split(seperator)(0) == results(0).fileName
      lines(5).split(seperator)(0) == results(1).fileName

    }
  }
}
