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
               identifier: Option[String],
               species: Option[String],
               organ: Option[String],
               treatments: Option[Seq[Treatment]] = None
            )

case class AcquisitionMethod(chromatographicMethod: Option[ChromatographicMethod])

/**
  * associated treatment
  *
  * @param name
  * @param value
  */
class Treatment(
                 val name: String,
                 val value: Option[Any]
               )