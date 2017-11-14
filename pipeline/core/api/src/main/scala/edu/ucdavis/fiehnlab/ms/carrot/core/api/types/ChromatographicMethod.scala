package edu.ucdavis.fiehnlab.ms.carrot.core.api.types

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.IonMode

/**
  * Created by wohlgemuth on 8/15/17.
  */
case class ChromatographicMethod(
                                  name: String,
                                  instrument: Option[String],
                                  column: Option[String],
                                  ionMode: Option[IonMode]
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

case class AcquisitionMethod(chromatographicMethod: Option[ChromatographicMethod] = None)

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