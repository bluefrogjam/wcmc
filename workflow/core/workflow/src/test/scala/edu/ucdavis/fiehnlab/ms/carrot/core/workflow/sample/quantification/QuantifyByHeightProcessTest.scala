package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing.PurityProcessing
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg on 7/1/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("common"))
class QuantifyByHeightProcessTest extends WordSpec with LazyLogging{

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

  "QuantifyByHeightProcessTest" should {

    val samples: List[_ <: Sample] = new MSDialSample(getClass.getResourceAsStream("/lipids/B5_P20Lipids_Pos_NIST02.msdial"), "B5_P20Lipids_Pos_NIST02.msdial") :: new MSDialSample(getClass.getResourceAsStream("/lipids/B5_SA0002_P20Lipids_Pos_1FL_1006.msdial"), "B5_SA0002_P20Lipids_Pos_1FL_1006.msdial") :: List()

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
