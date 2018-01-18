package edu.ucdavis.fiehnlab.cts3.api.conversion

import java.net.URLEncoder

import edu.ucdavis.fiehnlab.cts3.api.Converter
import edu.ucdavis.fiehnlab.cts3.model.{Hit, Stats}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestOperations

/**
  * Created by diego on 1/17/2018
  **/
class PubChemConverter extends Converter {
  @Autowired
  val template: RestOperations = null

  private val OUTPUT_FORMAT = "json"  // txt, xml, csv, png
  private val PUG_REST="https://pubchem.ncbi.nlm.nih.gov/rest/pug"
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
    var keyFixed: String = URLEncoder.encode(keyword, "utf-8").replaceAll("%2F", ".")

    if(from.eq("sid")) {
      domain = "substance"
    } else {
      domain = "compound"
    }

    if(properties.contains(to)) {
      operation = s"property/${provides(to)}"
    } else {
      operation = provides(to)
    }

    val html = PUG_REST + s"/$domain/${requires(from)}/$keyFixed/$operation/$OUTPUT_FORMAT"

    logger.info(s"Calling PUG: $html")
    val start = System.nanoTime()
    val response = template.getForEntity(html, classOf[String])
    val time = System.nanoTime() - start

    logger.info (s"TIME: ${time/NANOS_2_MILIS}ms")
    delay(time/NANOS_2_MILIS)

    if(response.getStatusCode.eq(HttpStatus.OK)) {
      logger.debug(response.getBody)
      Seq(new Hit(keyword, from, to, response.getBody, 1.0f) with Stats {
        override val timing: Long = time / NANOS_2_MILIS
        override val converter = this.getClass
      })
    } else {
      logger.warn("PubChem found NO hits")
      Seq(new Hit(keyword,from,to,"",0.0f) with Stats{
        override val timing: Long = time / NANOS_2_MILIS
        override val converter = this.getClass
      })
    }
  }

  private def delay(timeInMillis: Long): Unit = {

    if(timeInMillis < 200) { // 5 requests per second max (time is in nanos)
      logger.info(s"delaying for ${200 - timeInMillis}ms")
      Thread.sleep( - timeInMillis)
    }

  }
}
