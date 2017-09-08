package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.{FileInputStream, FileOutputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroReplacement
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.io.Source
/**
  * Created by wohlg on 7/9/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("backend-txt","carrot.report.quantify.height"))
class QuantifiedSampleTxtWriterTest extends WordSpec with LazyLogging{


  @Autowired
  val correction: LCMSTargetRetentionIndexCorrection = null

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



    val samples: Seq[_ <: Sample] = loader.getSamples(Seq("B5_P20Lipids_Pos_NIST02.abf", "B5_P20Lipids_Pos_QC000.abf"))

    //correct the data
    val correctedSample = samples.map(correction.process)

    val annotated = correctedSample.map(annotation.process)

    val quantified = annotated.map(quantification.process)

    val results = quantified.map(replacement.process)

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
