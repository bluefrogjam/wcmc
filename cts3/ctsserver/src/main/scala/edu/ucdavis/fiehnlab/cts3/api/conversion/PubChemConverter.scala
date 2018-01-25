package edu.ucdavis.fiehnlab.cts3.api.conversion

import java.net.URLEncoder

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.cts3.api.Converter
import edu.ucdavis.fiehnlab.cts3.model.{Hit, Stats}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Caching
import org.springframework.http.client.{ClientHttpRequest, ClientHttpResponse}
import org.springframework.http.{HttpMethod, HttpStatus, ResponseEntity}
import org.springframework.web.client.{HttpClientErrorException, RequestCallback, ResponseExtractor, RestOperations}

/**
  * Created by diego on 1/17/2018
  **/
class PubChemConverter extends Converter {
  @Autowired
  val template: RestOperations = null
  @Autowired
  val objectMapper: ObjectMapper = null

  private val OUTPUT_FORMAT = "json" // txt, xml, csv, png
  private val PUG_REST = "https://pubchem.ncbi.nlm.nih.gov/rest/pug"
  private val NANOS_2_MILIS: Long = 1000000

  private var domain = ""
  private var operation = ""

  private val properties = Seq("inchikey", "inchicode", "smiles", "molweight", "formula", "exactmass")


  //conversion from formula is async; from sdf and inchicode are POST
  override protected def requires: Map[String, String] = Map("name" -> "name", "inchikey" -> "inchikey",
    "smiles" -> "smiles", "cid" -> "cid", "sid" -> "sid")

  override protected def provides: Map[String, String] = Map("cid" -> "cids", "sid" -> "sids", "inchikey" -> "InChIKey", "inchicode" -> "InChI",
    "smiles" -> "CanonicalSMILES", "molweight" -> "MolecularWeight", "formula" -> "MolecularFormula", "exactmass" -> "ExactMass")


  override final def doConvert(keyword: String, from: String, to: String): Seq[Hit] = {
    var keyFixed: String = keyword.replaceAll("%2F", ".")

    if (from.eq("sid")) {
      domain = "substance"
    } else {
      domain = "compound"
    }

    if (properties.contains(to)) {
      operation = s"property/${provides(to)}"
    } else {
      operation = provides(to)
    }

    val html = PUG_REST + s"/$domain/${requires(from)}/$keyFixed/$operation/$OUTPUT_FORMAT"

    logger.info(s"Convert $keyword from $from to $to")
    logger.info(s"Calling PUG: $html")
    val start = System.nanoTime()
    val response = callPug(html, to)
    val time = System.nanoTime() - start
    logger.debug(s"+++ RESPONSE: ${response.getOrElse(" Empty response ")} +++")

    delay(time / NANOS_2_MILIS)

    if (response.isDefined && response.get.getStatusCode.eq(HttpStatus.OK)) {
      var data = ""

      to match {
        case "sid" =>
          val result = response.get.getBody.asInstanceOf[SidResponse]
          logger.debug(s"=== RESULT: $result ===")
          data = result.InformationList.Information.slice(0,4).map(item => item.SID.slice(0,4).toSet.mkString(",")).toSet.mkString(",")
          logger.info(s"--- DATA: $data ---")
        case "cid" =>
          val result = response.get.getBody.asInstanceOf[CidResponse]
          logger.debug(s"=== RESULT: $result ===")
          data = result.IdentifierList.CID.toSet.mkString(",")
          logger.info(s"--- DATA: $data ---")
        case _ =>
          val result = response.get.getBody.asInstanceOf[PropertyResponse]
          logger.debug(s"=== RESULT: $result ===")
          data = result.PropertyTable.Properties.slice(0,4).map(item => item.get(provides(to))).toSet.mkString(",")
          logger.info(s"--- DATA: $data ---")
      }

      Seq(new Hit(keyword, from, to, data, 1.0f) with Stats {
        override val timing: Long = time / NANOS_2_MILIS
        override val converter: Class[_] = this.getClass
      })
    } else {
      logger.warn("PubChem found NO hits")
      Seq(new Hit(keyword, from, to, "", 0.0f) with Stats {
        override val timing: Long = time / NANOS_2_MILIS
        override val converter: Class[_] = this.getClass
      })
    }
  }

  private def callPug(html: String, to: String): Option[ResponseEntity[_]] = {
    try {
      to match {
        case "cid" =>
          Some(template.getForEntity(html, classOf[CidResponse]))
        case "sid" =>
          Some(template.getForEntity(html, classOf[SidResponse]))
        case _ =>
          Some(template.getForEntity(html, classOf[PropertyResponse]))
      }
    } catch {
      case ex: HttpClientErrorException =>
        logger.error(ex.getMessage + s" calling $html")
        None
    }
  }

  private def delay(timeInMilis: Long): Unit = {

    if (timeInMilis < 200) { // 5 requests per second max
      logger.info(s"delaying for ${200 - timeInMilis}ms")
      Thread.sleep(200 - timeInMilis)
    }

  }
}

case class PropertyResponse(PropertyTable: PropertyTable)

case class PropertyTable(Properties: Seq[PubChemProperties])

case class PubChemProperties(
                              CID: Long,
                              InChI: String,
                              InChIKey: String,
                              MolecularWeight: Double,
                              ExactMass: Double,
                              MolecularFormula: String,
                              CanonicalSMILES: String) {

  def get(field: String): String = {
    field match {
      case "CID" => CID.toString
      case "InChI" => InChI
      case "InChIKey" => InChIKey
      case "MolecularFormula" => MolecularFormula
      case "CanonicalSMILES" => CanonicalSMILES
      case "MolecularWeight" => MolecularWeight.toString
      case "ExactMass" => ExactMass.toString
      case _ => ""
    }
  }
}

case class CidResponse(IdentifierList: IdentifierList)

case class IdentifierList(CID: Seq[Int])

case class SidResponse(InformationList: InformationList)

case class InformationList(Information: Seq[Information])

case class Information(CID: Long, SID: Seq[Int])
