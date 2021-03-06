package edu.ucdavis.fiehnlab.ms.carrot.core.api.filter

import org.springframework.context.ApplicationContext

/**
  * Created by wohlg_000 on 4/22/2016.
  */
trait Filter[T] {

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    * it supports the basic json logging model
    * to allow for easier discovery of annotation based bugs
    */
  final def include(spectra: T, applicationContext: ApplicationContext): Boolean = {
    val result = doIncludeWithDetails(spectra, applicationContext)

    result._1
  }

  /**
    * a method which should be overwritten, if the filter can provide details why it failed
    *
    * @param spectra
    * @param applicationContext
    * @return
    */
  protected def doIncludeWithDetails(spectra: T, applicationContext: ApplicationContext): (Boolean, Any) = {
    (doInclude(spectra, applicationContext), "no details available")
  }

  /**
    * this implementation should be overwritten if the filter cannot provide information why it failed
    *
    * @param spectra
    * @param applicationContext
    * @return
    */
  protected def doInclude(spectra: T, applicationContext: ApplicationContext): Boolean = false

  /**
    * this returns true if the spectra should be excluded
    * or false if it should be included. Just a little convenient helper method
    *
    * @param sSpectra
    * @return
    */
  final def exclude(sSpectra: T, applicationContext: ApplicationContext): Boolean = !include(sSpectra, applicationContext)
}

/**
  * abstract filter which provides some help with accurate masses
  *
  * @param massAccuracy
  * @tparam T
  */
abstract class MassFilter[T](massAccuracy: Double) extends Filter[T] {

  final def isNominal: Boolean = massAccuracy == 0.0

  /**
    * evaluates if these two masses are the same
    *
    * @param targetMass
    * @param featureMass
    * @return
    */
  def sameMass(targetMass: Double, featureMass: Double): Boolean = {
    if (!isNominal) {
      Math.abs(targetMass - featureMass) <= massAccuracy
    }
    else {
      Math.floor(featureMass + 0.2) == Math.floor(targetMass + 0.2)
    }

  }
}




















