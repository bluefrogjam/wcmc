package edu.ucdavis.fiehnlab.ms.carrot.core.adductjoiner

import org.apache.logging.log4j.scala.Logging
import joinery.DataFrame
import org.scalatest.{Matchers, WordSpec}
import org.springframework.test.context.TestContextManager

import scala.collection.JavaConverters._

/**
  * Created by diego on 2/26/2018
  **/
class JoinerTest extends WordSpec with Matchers with Logging {

  val joiner: Joiner = new Joiner

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "testJoin" should {

    "join adducts" ignore {
      val targets = DataFrame.readCsv(this.getClass.getResourceAsStream("/CSH_Positive_All-Targets_tabulated.csv")).reindex(0).transpose()

      targets.columns().asScala.filterNot(Array("mass", "retention time (s)", "retention time (min)").contains(_)).foreach(col => {
        println(s"Sample: $col")
        joiner.join(targets.retain("retention time (min)", "mass", col.toString), col.toString)
        println("-----------------")
      }
      )
    }
  }

  def df2Annot(data: DataFrame[Object]): Seq[Annotation] = {

    println(data)

    Seq.empty
  }
}
