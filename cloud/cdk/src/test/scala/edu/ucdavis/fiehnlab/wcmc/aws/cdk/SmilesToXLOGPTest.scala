package edu.ucdavis.fiehnlab.wcmc.aws.cdk

import java.util

import com.fasterxml.jackson.databind.ObjectMapper

import scala.collection.JavaConverters._
import org.scalatest.{ShouldMatchers, WordSpec}

class SmilesToXLOGPTest extends WordSpec with ShouldMatchers{

  "SmilesToXLOGPTest" should {

    "handleRequest" in {

      val toTest = new SmilesToXLOGP

      val request = Map[String,Object]("body" ->"C(C1C(C(C(C(O1)O)O)O)O)O").asJava
      val result = toTest.handleRequest(request,null).getBody()

      new ObjectMapper().readValue(result,classOf[java.util.HashMap[String,Any]]).get("result") shouldBe (-1.6970000000000007)
    }

  }
}
