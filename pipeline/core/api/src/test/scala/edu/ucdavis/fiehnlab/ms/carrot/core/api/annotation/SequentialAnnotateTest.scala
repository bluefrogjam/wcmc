package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.scalatest.{ShouldMatchers, WordSpec}

/**
  * Created by diego on 4/6/2018
  **/
class SequentialAnnotateTest extends WordSpec with ShouldMatchers with  LazyLogging {

  val anotTrue = new Annotate {
    override protected def doMatch(unknown: Feature, target: Target): Boolean = true
  }

  val anotFalse = new Annotate {
    override protected def doMatch(unknown: Feature, target: Target): Boolean = false
  }


  "testDoMatch" should  {

    "should always pass" in {
      new SequentialAnnotate(Seq(anotTrue, anotTrue)).isMatch(null, null) shouldBe true
    }
    "should always fail" in {
      new SequentialAnnotate(Seq(anotTrue, anotFalse)).isMatch(null, null) shouldBe false
    }
    "should always fail2" in {
      new SequentialAnnotate(Seq(anotFalse, anotTrue)).isMatch(null, null) shouldBe false
    }
  }

}
