package edu.ucdavis.fiehnlab.cts3.api.conversion

import edu.ucdavis.fiehnlab.cts3.api.Converter
import edu.ucdavis.fiehnlab.cts3.model.Hit

/**
  * Created by diego on 1/17/2018
  **/
class PubChemConverter extends Converter {
  private val OUTPUT_FORMAT = "json"  // txt, xml, csv, png
  private final val PUG_REST="https://pubchem.ncbi.nlm.nih.gov/rest/pug/"

  private var domain = "compound"


  private val properties = Seq("inchikey", "inchicode", "smiles", "molweight", "formula")

  override protected def requires: Map[String, String] = Map("name" -> "chemicalName", "inchikey" -> "inchikey", "inchicode" -> "inchicode",
    "smiles" -> "smiles", "formula" -> "formula", "cid" -> "cid", "sid" -> "sid")
  override protected def provides: Map[String, String] = Map("cid" -> "cids", "sid" -> "sids", "inchikey" -> "inchikey", "inchicode" -> "inchicode",
    "smiles" -> "smiles", "molweight" -> "MolecularWeight", "formula" -> "formula", "classification" -> "classification")



  override protected def doConvert(keyword: String, from: String, to: String): Seq[Hit] = {
    var keyFixed: String = ""

    if(from.eq("sid")) {
      domain = "substance"
    }

    val html = PUG_REST + s"/${domain}" + s"/${from}"

    Seq(Hit("","","","",1.0f))
  }

}
