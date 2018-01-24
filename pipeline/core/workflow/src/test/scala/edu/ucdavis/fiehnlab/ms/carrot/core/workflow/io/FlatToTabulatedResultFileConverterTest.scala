package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.WordSpec
import scala.collection.JavaConverters._

class FlatToTabulatedResultFileConverterTest extends WordSpec with LazyLogging {

  val converter = new FlatToTabulatedResultFileConverter

  "converter" must {

    "convert the given file" should {
      val result = converter.convert(getClass.getResourceAsStream("/results/flatresults.csv"))

      assert(result.split("\n").length == 7)
      assert(result.split("\n").head.split(",").length == 29)

      // Check that decimal formatting works
      assert(result.contains("retention time (min),0.78,"))
    }

    "convert the given file without decimal formatting" should {
      val result = converter.convert(getClass.getResourceAsStream("/results/flatresults.csv"), formatDecimals = false)

      assert(result.split("\n").length == 7)
      assert(result.split("\n").head.split(",").length == 29)

      // Check that decimal formatting is supressed
      assert(result.contains("retention time (min),0.7785,"))
    }
  }
}
