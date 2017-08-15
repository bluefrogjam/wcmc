package edu.ucdavis.fiehnlab.ms.carrot.core.api.types

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.IonMode

/**
  * Created by wohlgemuth on 8/15/17.
  */
class ChromatographicMethod(
                             val name: String,
                             val instrument: Option[String],
                             val column: Option[String],
                             val ionMode: Option[IonMode]
                           )

/**
  * defined matrix of an experimental sample
  *
  * @param species
  * @param organ
  */
class Matrix(
              val species: Option[String],
              val organ: Option[String],
              val treatments: Option[Seq[Treatment]] = None
            )

class AcquisitionMethod(chromatographicMethod: Option[ChromatographicMethod], matrix: Option[Matrix])

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