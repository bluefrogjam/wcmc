package edu.ucdavis.fiehnlab.cts3.api.conversion

import edu.ucdavis.fiehnlab.cts3.model.Hit
import org.scalatest.{Matchers, PropSpec, WordSpec}
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.prop.TableDrivenPropertyChecks

/**
  * Created by diego on 1/12/2018
  **/
class CactusConverterTest extends WordSpec with Matchers with LazyLogging {
  val converter = new CactusConverter()

  "CactusConverter" should {

    "Allow name 2 inchikey conversions" in {
      converter.canConvert("name", "stdinchikey") shouldBe true
    }

    "Deny conversion from invalid reqirement" in {
      converter.canConvert("kegg", "stdinchikey") shouldBe false
    }

    "Deny conversion to invalid provision" in {
      converter.canConvert("smiles", "kegg") shouldBe false
    }

    val data = Map(
      "keywords"  -> Array("alanine", "CC(=O)Oc1ccccc1C(O)=O"),
      "from"      -> Array("name","smiles"),    //not directly used by cactus
      "to"        -> Array("stdinchikey","stdinchikey"),
      "result"    -> Array("QNAYBMKLOCPYGJ-REOHCLBHSA-N","BSYNRYMUTXBXSQ-UHFFFAOYSA-N"))

    for(x <- data("keywords").indices) {
      s"should convert ${data("keywords")(x)} from ${data("from")(x)} to ${data("to")(x)} resulting in ${data("result")(x)}" in {
        val hits = converter.doConvert(data("keywords")(x), data("from")(x), data("to")(x))

        hits should have size 1
        hits.head shouldBe a[Hit]

        val hit = hits.head
        hit.from shouldEqual data("from")(x)
        hit.to shouldEqual data("to")(x)
        hit.result shouldEqual data("result")(x)
        hit.score shouldEqual 1.0
      }
    }
  }
}
