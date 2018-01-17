package edu.ucdavis.fiehnlab.cts3.api.conversion

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.cts3.api.Converter
import edu.ucdavis.fiehnlab.cts3.model._
import org.springframework.stereotype.Service

import scala.io.Source

/**
  * Created by diego on 1/12/2018
  **/
@Service
class CactusConverter extends Converter with LazyLogging {
  private def requires = Map("name" -> "name", "inchikey" -> "stdinchikey", /*"inchicode" -> "stdinchi",*/ "smiles" -> "smiles")

  private def provides = Map("inchikey" -> "stdinchikey", "inchicode" -> "stdinchi", "sdf" -> "sdf",
    "smiles" -> "smiles", "cas" -> "cas", "molweight" -> "mw", "formula" -> "formula", "iupacname" -> "iupac_name")
  private def CACTUS_API = "https://cactus.nci.nih.gov/chemical/structure/"

  override final def priority: Int = (Int.MaxValue * 0.5).toInt

  override def doConvert(keyword: String, from: String, to: String): Seq[Hit] = {
    var keywordFixed = ""

    logger.debug(s"Request: $keyword, $from, $to")

    // https://cactus.nci.nih.gov/chemical/structure_documentation
    if (to.eq("smiles")) {
      keywordFixed = keyword.replaceAll("#", "%23")
    } else {
      keywordFixed = keyword
    }

    val html = Source.fromURL(CACTUS_API + keywordFixed + "/" + provides(to))

    Seq(new Hit(keywordFixed, from, to, html.mkString, 1.0f))
  }

  override def canConvert(from:String, to:String): Boolean = {
    requires.keySet.contains(from) && provides.keySet.contains(to)
  }
}
