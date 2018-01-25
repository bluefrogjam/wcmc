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
  * Created by diego on 1/12/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[CctsConverterTestConfiguration]))
class CactusConverterTest extends WordSpec with Matchers with LazyLogging {
  @Autowired
  val converter: CactusConverter = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CactusConverter" should {

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
      "keywords" -> Array("alanine", "CC(=O)Oc1ccccc1C(O)=O", "QNAYBMKLOCPYGJ-REOHCLBHSA-N"),
      "from" -> Array("name", "smiles", "inchikey"), //not directly used by cactus
      "result" -> Array("InChIKey=QNAYBMKLOCPYGJ-REOHCLBHSA-N", "InChIKey=BSYNRYMUTXBXSQ-UHFFFAOYSA-N", "InChIKey=QNAYBMKLOCPYGJ-REOHCLBHSA-N"))

    for (x <- fromTest("keywords").indices) {
      s"should convert ${fromTest("keywords")(x)} from ${fromTest("from")(x)} to inchikey resulting in ${fromTest("result")(x)}" in {
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

    val alanineSDF: String = "C3H7NO2\nAPtclcactv01241812303D 0   0.00000     0.00000\n \n 13 12  0  0  1  0  0  0  0  0999 V2000\n   -1.5312    0.9685    0.3862 N   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.6990   -0.2417    0.4182 C   0  0  1  0  0  0  0  0  0  0  0  0\n   -0.7087   -0.6627    1.4235 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.2545   -1.2689   -0.5703 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.7151    0.1108    0.0348 C   0  0  0  0  0  0  0  0  0  0  0  0\n    0.9398    1.1301   -0.5739 O   0  0  0  0  0  0  0  0  0  0  0  0\n    1.7250   -0.7076    0.3694 O   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.5385    1.3780   -0.5360 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -2.4691    0.7709    0.7015 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -1.2449   -0.8478   -1.5756 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -0.6371   -2.1668   -0.5465 H   0  0  0  0  0  0  0  0  0  0  0  0\n   -2.2774   -1.5238   -0.2930 H   0  0  0  0  0  0  0  0  0  0  0  0\n    2.6146   -0.4392    0.1017 H   0  0  0  0  0  0  0  0  0  0  0  0\n  1  2  1  0  0  0  0\n  2  3  1  0  0  0  0\n  2  4  1  0  0  0  0\n  2  5  1  0  0  0  0\n  5  6  2  0  0  0  0\n  5  7  1  0  0  0  0\n  1  8  1  0  0  0  0\n  1  9  1  0  0  0  0\n  4 10  1  0  0  0  0\n  4 11  1  0  0  0  0\n  4 12  1  0  0  0  0\n  7 13  1  0  0  0  0\nM  END\n$$$$"
    val toTest = Map(
      "keywords" -> Array("aspirine", "alanine", "aspirine", "aspirine", "alanine", "alanine", "alanine"),
      "to" -> Array("inchicode", "sdf", "smiles", "cas", "molweight", "formula", "iupacname"), //not directly used by cactus
      "result" -> Array("InChI=1S/C9H8O4/c1-6(10)13-8-5-3-2-4-7(8)9(11)12/h2-5H,1H3,(H,11,12)",
        alanineSDF, "CC(=O)Oc1ccccc1C(O)=O", "50-78-2\n11126-35-5\n11126-37-7\n2349-94-2\n26914-13-6\n98201-60-6", "89.0938", "C3H7NO2",
        "(2S)-2-aminopropanoic acid"))

    for (x <- toTest("keywords").indices) {
      s"should convert ${toTest("keywords")(x)} from name to ${toTest("to")(x)} resulting in ${toTest("result")(x)}" in {
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
class CctsConverterTestConfiguration {
  @Bean
  def converter: CactusConverter = new CactusConverter()
}
