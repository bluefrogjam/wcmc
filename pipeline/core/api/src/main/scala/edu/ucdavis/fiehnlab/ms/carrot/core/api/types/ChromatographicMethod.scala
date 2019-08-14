package edu.ucdavis.fiehnlab.ms.carrot.core.api.types

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{IonMode, NegativeMode, PositiveMode}

/**
  * Created by wohlgemuth on 8/15/17.
  */
case class ChromatographicMethod(
                                  name: String,
                                  instrument: Option[String] = None,
                                  column: Option[String] = None,
                                  ionMode: Option[IonMode] = None
                                )

/**
  * defined matrix of an experimental sample
  *
  * @param species
  * @param organ
  */
case class Matrix(
                   identifier: String,
                   species: String,
                   organ: String,
                   treatments: Seq[Treatment]
                 )

case class AcquisitionMethod(chromatographicMethod: ChromatographicMethod = ChromatographicMethod("default", None, None)) {
  override def toString: String = AcquisitionMethod.serialize(this)
}

object AcquisitionMethod {

  /**
    * loads a method form an encoded string
    *
    * @param name
    * @return
    */
  def deserialize(name: String): AcquisitionMethod = {

    val entries = name.split("\\s\\|\\s")
    assert(entries.size == 4)

    val methodName = entries(0)

    val instrument = {
      if (entries(1) == "unknown") None
      else Some(entries(1))
    }
    val column = {
      if (entries(2) == "unknown") None
      else Some(entries(2))
    }

    val ionMode = {
      if (entries(3) == "negative") NegativeMode()
      else PositiveMode()
    }

    AcquisitionMethod(
      chromatographicMethod = ChromatographicMethod(
        methodName,
        instrument,
        column,
        Some(ionMode)
      )
    )
  }

  /**
    * generates a loadable id from
    *
    * @param acquisitionMethod
    * @return
    */
  def serialize(acquisitionMethod: AcquisitionMethod): String = {


    val method = acquisitionMethod.chromatographicMethod
    val instrument = method.instrument match {
      case Some(x) =>
        x
      case _ => "unknown"
    }

    val column = method.column match {
      case Some(x) =>
        x
      case _ => "unknown"
    }

    val mode = method.ionMode match {
      case Some(x) if x.isInstanceOf[PositiveMode] =>
        "positive"
      case Some(x) if x.isInstanceOf[NegativeMode] =>
        "negative"
      case _ =>
        "unknown"
    }

    s"${method.name} | $instrument | $column | $mode"

  }
}

/**
  * associated treatment
  *
  * @param name
  * @param value
  */
class Treatment(
                 val name: String,
                 val value: Any
               )

/**
  * returns the associated id for this object
  */
trait Idable[T] {

  /**
    * internal id
    *
    * @return
    */
  def id: T
}
