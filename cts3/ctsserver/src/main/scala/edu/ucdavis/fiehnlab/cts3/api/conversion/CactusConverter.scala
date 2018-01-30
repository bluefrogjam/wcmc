package edu.ucdavis.fiehnlab.cts3.api.conversion

import edu.ucdavis.fiehnlab.cts3.api.Converter
import edu.ucdavis.fiehnlab.cts3.model._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.{HttpClientErrorException, RestOperations}


/**
  * Created by diego on 1/12/2018
  **/
@Component
class CactusConverter extends Converter {
  logger.debug("Creating CactusConverter")

  @Autowired
  val template: RestOperations = null

  private def CACTUS_API = "https://cactus.nci.nih.gov/chemical/structure/"

  override final def requires = Map("name" -> "name", "inchikey" -> "stdinchikey", "smiles" -> "smiles")

  override final def provides = Map("inchikey" -> "stdinchikey", "inchicode" -> "stdinchi", "sdf" -> "sdf",
    "smiles" -> "smiles", "cas" -> "cas", "molweight" -> "mw", "formula" -> "formula", "iupacname" -> "iupac_name")

  override final def priority: Int = (Int.MaxValue * 0.5).toInt

  override final def doConvert(keyword: String, from: String, to: String): Seq[Hit] = {
    var keywordFixed = ""

    logger.debug(s"Request: $keyword, $from, $to")

    // https://cactus.nci.nih.gov/chemical/structure_documentation
    if (to.eq("smiles")) {
      keywordFixed = keyword.replaceAll("#", "%23")
    } else {
      keywordFixed = keyword
    }

    val html = s"${CACTUS_API}/{keywordFixed}/{to}"
    try {
      val response = template.getForEntity(html, classOf[String], keywordFixed, provides(to))

      Seq(Hit(keywordFixed, from, to, response.getBody, 1.0f))
    } catch {
      case ex: HttpClientErrorException if ex.getMessage.contains("404") => {
        logger.warn(s"Cactus response: ${ex.getMessage}")
      }
        Seq.empty
    }
  }
}
