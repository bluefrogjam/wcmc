package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg on 7/1/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("backend-txt","quantify-by-height"))
class QuantifyByHeightProcessTest extends WordSpec with LazyLogging{

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrection = null

  @Autowired
  val loader:SampleLoader = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val quantification: QuantifyByHeightProcess = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "QuantifyByHeightProcessTest" should {

    val samples: Seq[_ <: Sample] = loader.getSamples(Seq(  "B5_P20Lipids_Pos_NIST02.abf","B5_SA0002_P20Lipids_Pos_1FL_1006.abf"))

    //compute purity values
    val purityComputed = samples //.map(purity.process)

    //correct the data
    val correctedSample = purityComputed.map(correction.process)

    val annotated = correctedSample.map(annotation.process)

    annotated.foreach { sample =>

      s"process ${sample}" in {

        val result:QuantifiedSample[Double] = quantification.process(sample)

        var annotationCount = 0
        result.quantifiedTargets.foreach{ a=>
          if(a.spectra.isDefined){
            annotationCount = annotationCount+ 1
          }
        }

        //make sure that we the same amount of annotations as spectra
        assert(annotationCount == sample.spectra.size)
      }
    }
  }
}
