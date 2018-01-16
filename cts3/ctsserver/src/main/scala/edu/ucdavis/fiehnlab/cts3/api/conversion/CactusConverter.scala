package edu.ucdavis.fiehnlab.cts3.api.conversion

import java.net.URLEncoder

import edu.ucdavis.fiehnlab.cts3.api.Converter
import edu.ucdavis.fiehnlab.cts3.model.Hit
import org.springframework.stereotype.Component

import scala.io.Source

/**
  * Created by diego on 1/12/2018
  **/
@Component
class CactusConverter extends Converter {
  private def provides = List("stdinchikey","stdinchi","sdf","smiles","names","IUPACName","cas","csid","molweight","formula")
  private def requires = List("name","names","stdinchikey","stdinchi","smiles")
  private def CACTUS_API = "https://cactus.nci.nih.gov/chemical/structure/"


  override def doConvert(keyword: String, from: String, to: String): Seq[Hit] = {
    val html = Source.fromURL(CACTUS_API + keyword + "/" + to)
    Seq(new Hit(keyword, from, to, html.mkString.replace("InChIKey=", ""), 1.0f))
  }

  override def canConvert(from:String, to:String): Boolean = {
    requires.contains(from) && provides.contains(to)
  }
}
