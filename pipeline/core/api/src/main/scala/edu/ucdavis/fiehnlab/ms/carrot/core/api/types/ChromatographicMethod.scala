package edu.ucdavis.fiehnlab.ms.carrot.core.api.types

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.IonMode

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

case class AcquisitionMethod(chromatographicMethod: ChromatographicMethod = ChromatographicMethod("default", None, None))

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