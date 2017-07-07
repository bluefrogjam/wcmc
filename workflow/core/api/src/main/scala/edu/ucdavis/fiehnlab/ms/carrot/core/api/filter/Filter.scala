package edu.ucdavis.fiehnlab.ms.carrot.core.api.filter

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.{MassAccuracy, Similarity}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSLibrarySpectra, MSMSSpectra, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSpectra, Ion, Target}

/**
  * Created by wohlg_000 on 4/22/2016.
  */
trait Filter[T <: Feature] extends LazyLogging {

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

/**
  * this class only includes MS Spectra, but no MSMS
  */
class IncludesMSSpectraOnly extends Filter[MSSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = spectra.msLevel == 1
}

/**
  * only includes MSMS spectra
  */
class IncludesMSMSSpectraOnly extends Filter[MSSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = spectra.isInstanceOf[MSMSSpectra] && spectra.msLevel > 1
}

/**
  * includes spectra with the given level
  *
  * @param level
  */
class IncludesMSLevelSpectra(val level: Short) extends Filter[MSSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = spectra.msLevel.equals(level)
}

/**
  * includes all spectra, having the base peak defined in the list of basePeaks, with the accuracy between Peak +/- accuracy
  *
  * @param basePeaks
  * @param accuracy
  */
class IncludesBasePeakSpectra(val basePeaks: List[Double], val accuracy: Double = 0.00005) extends Filter[MSSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = {
    basePeaks.exists { peak =>
      logger.debug(s"basePeak of spectra is ${spectra.basePeak.mass} compared to ${peak}")
      val result = peak > (spectra.basePeak.mass - accuracy) && peak < (spectra.basePeak.mass + accuracy)

      logger.debug(s"it's considered to be accepted: ${result} with an accuracy of ${accuracy}")
      result
    }
  }
}

/**
  * excludes all spectra with the specific base peak
  *
  * @param basePeaks
  * @param accuracy
  */
class ExcludeBasePeakSpectra(override val basePeaks: List[Double], override val accuracy: Double = 0.00005) extends IncludesBasePeakSpectra(basePeaks = basePeaks, accuracy = accuracy) {
  override def include(spectra: MSSpectra): Boolean = {
    basePeaks.exists { peak =>
      logger.debug(s"basePeak of spectra is ${spectra.basePeak.mass} compared to ${peak}")
      val result = !(peak > (spectra.basePeak.mass - accuracy) && peak < (spectra.basePeak.mass + accuracy))

      logger.debug(s"it's considered to be accepted: ${result} with an accuracy of ${accuracy}")
      result
    }
  }
}

/**
  * Include the spectra, if any ion of it's ions has a mass between any of the required ions +/- the mass accuracy
  */
class IncludesByPeakHeight(val peaks: List[Ion], val massAccuracy: Double = 0.0005, val minIntensity: Float = 0.0f) extends Filter[MSSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = {
    peaks.foreach { ion =>
      if (ion.mass > 0) {
        spectra.ions.foreach { spectraIon =>
          if ((spectraIon.mass > ion.mass - massAccuracy) && ion.mass < (ion.mass + massAccuracy) && ion.intensity > minIntensity) {
            return true
          }
        }
      }
    }
    false
  }
}

/**
  * is the quant ion included in the given spectra
  *
  * @param librarySpectra related library spectra
  * @param massAccuracy
  */
class IncludesQuantIon(librarySpectra: MSLibrarySpectra, massAccuracy: Double = 0.00005) extends IncludesByPeakHeight(List {
  Ion(librarySpectra.quantificationIon.getOrElse(0), 0)
}, massAccuracy, 0.03f)

/**
  * returns true, if the spectras retention time is in the defined window
  *
  * @param timeInSeconds
  * @param window
  */
class IncludeByRetentionTimeWindow(val timeInSeconds: Double, val window: Double = 5) extends Filter[Feature] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: Feature): Boolean = {
    val min = timeInSeconds - window
    val max = timeInSeconds + window

    spectra.retentionTimeInSeconds > min && spectra.retentionTimeInSeconds < max
  }
}

/**
  * includes by retention index time window
  * @param timeInSeconds
  * @param window
  */
class IncludeByRetentionIndexTimeWindow(val timeInSeconds: Double, val window: Double = 5) extends Filter[Feature with CorrectedSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: Feature with CorrectedSpectra): Boolean = {
    val min = timeInSeconds - window
    val max = timeInSeconds + window

    spectra.retentionIndex > min && spectra.retentionIndex < max
  }
}
class IncludeByMassRange(val mass: Double, val window: Double) extends Filter[MSSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = MassAccuracy.findClosestIon(spectra, mass).orNull != null
}

class IncludeByMassRangePPM(val target: Target, val windowInPPM: Double) extends Filter[Feature] with LazyLogging{
  logger.debug(s"mass window is ${windowInPPM} for ${target}")

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: Feature): Boolean = {
    val error = MassAccuracy.calculateMassErrorPPM(spectra, target)
    logger.debug(s"mass error is: ${error} for ${spectra} and ${target}")

    if(error.isDefined){
      val result  = error.get < windowInPPM

      logger.debug(s"\t=> accepted $result")
      result
    }
    else{
      false
    }
  }
}

/**
  * checks if the spectra matches the given similarity, based on the cutoff
  *
  * @param origin
  * @param cutoff needs to be less than 1
  */
class IncludeBySimilarity(val origin: MSSpectra, val cutoff: Double) extends Filter[MSSpectra] {

  assert(cutoff <= 1)

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = {
    val result = Similarity.compute(spectra, origin)

    assert(result <= 1.0)

    result >= cutoff

  }
}