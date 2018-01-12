package edu.ucdavis.fiehnlab.ms.carrot.core.api.filter

import com.typesafe.scalalogging.LazyLogging

/**
  * Created by wohlg_000 on 4/22/2016.
  */
trait Filter[T] extends LazyLogging {

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  def include(spectra: T): Boolean

  /**
    * this returns true if the spectra should be excluded
    * or false if it should be included. Just a little convenient helper method
    *
    * @param sSpectra
    * @return
    */
  final def exclude(sSpectra: T): Boolean = !include(sSpectra)
}






















