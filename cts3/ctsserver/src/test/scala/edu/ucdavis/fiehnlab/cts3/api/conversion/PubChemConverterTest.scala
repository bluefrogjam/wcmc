package edu.ucdavis.fiehnlab.cts3.api.conversion

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.cts3.api.Converter
import edu.ucdavis.fiehnlab.cts3.model.Hit
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, ComponentScan}
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

    val fromTest = Map(
      "keywords" -> Array("alanine", "QNAYBMKLOCPYGJ-REOHCLBHSA-N", "CC(=O)Oc1ccccc1C(O)=O", "602", "C3H7NO2"),
      "from" -> Array("name", "inchikey", "smiles", "cid", "sid"), //not directly used by cactus
      "result" -> Array("InChIKey=QNAYBMKLOCPYGJ-REOHCLBHSA-N", "InChIKey=QNAYBMKLOCPYGJ-REOHCLBHSA-N",
        "InChIKey=BSYNRYMUTXBXSQ-UHFFFAOYSA-N", "InChIKey=QNAYBMKLOCPYGJ-REOHCLBHSA-N",
        "InChIKey=QNAYBMKLOCPYGJ-REOHCLBHSA-N"))

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

    val toTest = Map(
      "keywords" -> Array("alanine", "alanine", "alanine", "alanine", "alanine", "alanine", "alanine", "alanine"),
      "to" -> Array("cid", "sid", "inchikey", "inchicode", "smiles", "molweight", "formula", "exactmass"),
      "result" -> Array("602", "", "InChIKey=QNAYBMKLOCPYGJ-REOHCLBHSA-N", "InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)",
        "CC(C(=O)O)N", "89.094", "C3H7NO2", "89.048"))

    for (x <- toTest("keywords").indices) {
      s"should convert ${toTest("keywords")(x)} from name to ${toTest("to")(x)} resulting in ${toTest("result")(x)} (output)" in {
        val hits = converter.doConvert(toTest("keywords")(x), "name", toTest("to")(x))

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
@ComponentScan(basePackageClasses = Array(classOf[CactusConverter]))
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class PCConverterTestConfiguration {
  @Bean
  def converter: PubChemConverter = new PubChemConverter()
}