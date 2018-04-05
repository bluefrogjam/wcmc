package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.{FileInputStream, FileOutputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, QuantifiedSample, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroReplacement
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.io.Source
/**
  * Created by wohlg on 7/9/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("backend-txt-lcms", "carrot.report.quantify.height", "carrot.processing.replacement.simple", "carrot.lcms"))
class QuantifiedSampleTxtWriterTest extends WordSpec with LazyLogging{


  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val quantification: QuantifyByHeightProcess = null

  @Autowired
  val replacement:ZeroReplacement = null

  @Autowired
  val loader: SampleLoader = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "QuantifiedSampleTxtWriterTest" should {


    val method = AcquisitionMethod(None)

    val samples: Seq[_ <: Sample] = loader.getSamples(Seq("B5_P20Lipids_Pos_NIST02.d.zip", "B5_P20Lipids_Pos_QC000.d.zip"))

    //correct the data
    val correctedSample = samples.map((item: Sample) => correction.process(item, method))

    val annotated = correctedSample.map((item: CorrectedSample) => annotation.process(item, method))

    val quantified = annotated.map((item: AnnotatedSample) => quantification.process(item,method ))

    val results = quantified.map((item: QuantifiedSample[Double]) => replacement.process(item, method))

    "write" in {

      results.size should be > 0

      val out = new FileOutputStream("target/test.txt")

      val seperator = ","
      val writer = new QuantifiedSampleTxtWriter[Double]

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
