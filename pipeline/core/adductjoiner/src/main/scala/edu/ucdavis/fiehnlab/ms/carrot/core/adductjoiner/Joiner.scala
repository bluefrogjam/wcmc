package edu.ucdavis.fiehnlab.ms.carrot.core.adductjoiner

import joinery.DataFrame
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

/**
  * Created by diego on 2/26/2018
  **/
@Component
class Joiner {
  def join(targets: DataFrame[Object], col: String): Unit = {
    val tmp = targets.map().asScala
    val data = tmp.map(entry => {
      Annotation(entry._1.toString, Double.unbox(entry._2.get(0)), Double.unbox(entry._2.get(1)), Double.unbox(entry._2.get(2)).toInt, col)
    })

    val sorted = data.groupBy(_.inchikey).toSeq
    sorted.filterNot(_._1 == "").map(s => Map[String, Annotation](s._1 -> filterAnnotations(s._2.asInstanceOf[Seq[Annotation]])))

  }

  def filterAnnotations(annots: Seq[Annotation]): Annotation = {
    if (annots.lengthCompare(1) > 0) {
      println("+++++++++")
      annots.sortBy(_.adduct).groupBy(_.name).map(x => Map(x._1 ->
        Annotation("",
          x._2.map(_.mass).min,
          x._2.head.rt_min,
          x._2.map(_.intensity).sum,
          x._2.head.sample,
          x._1,
          x._2.map(_.adduct).aggregate((a: String, b: String) => s"${a}_${b}"),
          x._2.head.inchikey)
      )
      ).foreach(println)
      println("=========")
      Annotation("", 0, 0, 0, "")
    } else {
      println(s"+++++++++\n${annots.head}\n=========")
      annots.head
    }
  }
}

object Annotation {
  def apply(token: String, mass: Double, rt_min: Double, intensity: Int, sample: String): Annotation = {
    new Annotation(
      token = token,
      mass = mass,
      rt_min = rt_min,
      intensity = intensity,
      sample = sample,
      name = token.replace("  ", " ").trim.split("\\s\\[").lift(0).getOrElse("").trim,
      adduct = if (token contains ("[")) {
        println(s"++ ${token} ++}"); token.substring(token.indexOf("["), token.indexOf("]+") + 2).trim
      } else {
        println(token); ""
      },
      inchikey = token.trim.split("(?<!1)_").lift(1).getOrElse("")
    )
  }
}

case class Annotation(token: String = "",
                      mass: Double,
                      rt_min: Double,
                      intensity: Int,
                      sample: String,
                      name: String,
                      adduct: String,
                      inchikey: String
                     ) {
  //  name.concat("_").concat(adduct).concat("_").concat(inchikey)
}
