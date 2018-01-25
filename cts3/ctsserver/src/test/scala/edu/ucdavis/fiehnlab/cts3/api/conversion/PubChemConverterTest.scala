package edu.ucdavis.fiehnlab.cts3.api.conversion

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.cts3.api.Converter
import edu.ucdavis.fiehnlab.cts3.model.Hit
import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, ComponentScan, Import}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by diego on 1/18/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[PCConverterTestConfiguration]))
class PubChemConverterTest extends WordSpec with Matchers with LazyLogging {
  @Autowired
  val converter: PubChemConverter = null
  @Autowired
  val objectMapper:ObjectMapper = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "PubChemConverter" should {

    "Allow name 2 inchikey conversions" in {
      converter.canConvert("name", "inchikey") shouldBe true
    }

    "Deny conversion from invalid reqirement" in {
      converter.canConvert("kegg", "inchikey") shouldBe false
    }

    "Deny conversion to invalid provision" in {
      converter.canConvert("smiles", "kegg") shouldBe false
    }

//    "behave" ignore {
//      val hits = converter.doConvert("alanine", "name", "cid")
//
//      hits.size should be > 0
//      hits.head shouldBe a[Hit]
//
//      val hit = hits.head
//      hit.from shouldEqual "name"
//      hit.to shouldEqual "cid"
//      hit.result.trim() shouldEqual "602,5950,71080,51283"
//      hit.score shouldEqual 1.0
//    }

    val fromTest = Map(
      "keywords" -> Array("alanine", "QNAYBMKLOCPYGJ-REOHCLBHSA-N", "CC(=O)Oc1ccccc1C(O)=O", "602"),
      "from" -> Array("name", "inchikey", "smiles", "cid"),
      "result" -> Array("QNAYBMKLOCPYGJ-UHFFFAOYSA-N,QNAYBMKLOCPYGJ-REOHCLBHSA-N,QNAYBMKLOCPYGJ-UWTATZPHSA-N,QNAYBMKLOCPYGJ-AZXPZELESA-N",
        "QNAYBMKLOCPYGJ-REOHCLBHSA-N", "BSYNRYMUTXBXSQ-UHFFFAOYSA-N", "QNAYBMKLOCPYGJ-UHFFFAOYSA-N"))

    for (x <- fromTest("keywords").indices) {
      s"should convert ${fromTest("keywords")(x)} from ${fromTest("from")(x)} to inchikey resulting in ${fromTest("result")(x)} (input)" in {
        val hits = converter.doConvert(fromTest("keywords")(x), fromTest("from")(x), "inchikey")

        hits.size should be > 0
        hits.head shouldBe a[Hit]

        val hit = hits.head
        hit.from shouldEqual fromTest("from")(x)
        hit.to shouldEqual "inchikey"
        hit.result shouldEqual fromTest("result")(x)
        hit.score shouldEqual 1.0
      }
    }

    val alanineSIDS = "4590,73437,125505,435579,3343,585842,589753,627087,3433,627088,822770,824674,8181987,24858033,34715720,78123947"

    val toTest = Map(
      "keywords" -> Array("alanine"),
      "to" -> Array("cid", "sid", "inchikey", "inchicode", "smiles", "molweight", "formula", "exactmass"),
      "result" -> Array("602,5950,71080,51283",
        alanineSIDS,
        "QNAYBMKLOCPYGJ-UHFFFAOYSA-N,QNAYBMKLOCPYGJ-REOHCLBHSA-N,QNAYBMKLOCPYGJ-UWTATZPHSA-N,QNAYBMKLOCPYGJ-AZXPZELESA-N",
        "InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6),InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)/t2-/m0/s1,InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)/t2-/m1/s1,InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)/i4+1",
        "CC(C(=O)O)N",
        "89.094,90.087",
        "C3H7NO2",
        "89.048,90.045"))

    for (x <- toTest("to").indices) {
      s"should convert ${toTest("keywords")(0)} from name to ${toTest("to")(x)} resulting in ${toTest("result")(x)} (output)" in {
        val hits = converter.doConvert(toTest("keywords")(0), "name", toTest("to")(x))

        hits.size should be > 0
        hits.head shouldBe a[Hit]

        val hit = hits.head
        hit.from shouldEqual "name"
        hit.to shouldEqual toTest("to")(x)
        hit.result.trim() shouldEqual toTest("result")(x).trim()
        hit.score shouldEqual 1.0
      }
    }
  }
}

@SpringBootApplication
@ComponentScan
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class PCConverterTestConfiguration {
  @Bean
  def converter: PubChemConverter = new PubChemConverter()
}
