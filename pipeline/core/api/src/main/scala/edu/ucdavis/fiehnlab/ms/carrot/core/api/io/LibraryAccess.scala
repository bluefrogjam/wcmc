package edu.ucdavis.fiehnlab.ms.carrot.core.api.io

import java.io._

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{RetentionIndexTarget, Target}

import scala.io.Source


/**
  * Created by wohlg_000 on 4/22/2016.
  */
trait LibraryAccess[T <: Target] {

  /**
    * loads all the spectra from the library
    *
    * @return
    */
  def load: Iterable[T]

  /**
    * adds a new target to the internal list of targets
    *
    * @param target
    */
  def add(target: T): Unit = {
    add(Seq(target))
  }

  /**
    * adds a list of targets
    *
    * @param targets
    */
  def add(targets: Iterable[T])
}

/**
  * it's a simple file based reader to get access to targets in a stream
  *
  * @param file
  */
class TxtStreamLibraryAccess[T <: Target](file: File, val seperator: String = "\t") extends LibraryAccess[T] {

  /**
    * loads all the spectra from the library
    *
    * @return
    */
  override def load: Iterable[T] = {
    val result = Source.fromFile(file).getLines().collect {

      case x: String =>
        if (!x.startsWith("#")) {

          val temp = x.split(seperator)

          if (temp.length == 3) {
            new RetentionIndexTarget {
              override val monoIsotopicMass: Option[Double] = Some(temp(1).toDouble)
              override val name: Option[String] = Some(temp(2))
              override val retentionTimeInSeconds: Double = temp(0).toDouble * 60
              override val inchiKey: Option[String] = None
              override val required: Boolean = false
            }
          }
          else if (temp.length == 2) {
            new RetentionIndexTarget {
              override val monoIsotopicMass: Option[Double] = Some(temp(1).toDouble)
              override val name: Option[String] = None
              override val retentionTimeInSeconds: Double = temp(0).toDouble * 60
              override val inchiKey: Option[String] = None
              override val required: Boolean = false
            }
          }
          else if (temp.length == 4) {
            new RetentionIndexTarget {
              override val monoIsotopicMass: Option[Double] = Some(temp(1).toDouble)
              override val name: Option[String] = Some(temp(2))
              override val retentionTimeInSeconds: Double = temp(0).toDouble * 60
              override val inchiKey: Option[String] = None
              override val required: Boolean = temp(3).toBoolean
            }
          }
          else if (temp.length == 5) {
            new RetentionIndexTarget {
              override val monoIsotopicMass: Option[Double] = Some(temp(1).toDouble)
              override val name: Option[String] = Some(temp(2))
              override val retentionTimeInSeconds: Double = temp(0).toDouble * 60
              override val required: Boolean = temp(3).toBoolean
              override val inchiKey: Option[String] = Some(temp(4))
            }
          }

          else {
            throw new IOException("unsupported file format discovered!")
          }
        }
    }.collect {
      case x: T => x
    }.toList

    result
  }

  /**
    * adds a new target to the internal list of targets
    *
    * @param targets
    */
  override def add(targets: Iterable[T]): Unit = {

    val out = new FileWriter(file, true)

    targets.foreach { target =>

      val required = target match {
        case x: RetentionIndexTarget => x.required
        case _ => ""
      }


      out.write(
        s"""|${target.retentionTimeInSeconds}
               |$seperator
               |${target.monoIsotopicMass}
               |$seperator${target.name.getOrElse("unknown")}
               |$seperator
               |$required
               |$seperator
               |${target.inchiKey.getOrElse("")}
               |\n""".stripMargin
      )
    }

    out.flush()
    out.close()
  }
}