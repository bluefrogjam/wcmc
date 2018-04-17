package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil

trait Feature extends AccurateMassSupport {

  /**
    * the associated sample
    */
  val sample:String

  /**
    * how pure this spectra is
    */
  val purity: Option[Double]
  /**
    * the local scan number
    */
  val scanNumber: Int

  /**
    * the retention time of this spectra. It should be provided in seconds!
    */
  val retentionTimeInSeconds: Double

  /**
    * provides us with the retention time in minutes
    *
    * @return
    */
  def retentionTimeInMinutes: Double = retentionTimeInSeconds / 60

  /**
    * specified ion mode for the given feature
    */
  val ionMode: Option[IonMode]

  /**
    * the associated complete scan for this feature
    */
  val associatedScan : Option[SpectrumProperties]

  /**
    * accurate mass of this feature, if applicable
    */
  val massOfDetectedFeature: Option[Ion]

  override def toString = s"Feature($scanNumber, $retentionTimeInMinutes, ${massOfDetectedFeature.get.mass}, ${associatedScan})"

  /**
    * returns the accurate mass, of this trait
    *
    * @return
    */
  override def accurateMass: Option[Double] = if (massOfDetectedFeature.isDefined) {
    Option(massOfDetectedFeature.get.mass)
  }
  else {
    None
  }

}

/**
  * commonly shared properties of a spectrum
  */
trait SpectrumProperties {

  /**
    * the msLevel of this spectra
    */
  val msLevel: Short

  /**
    * base peak for this spectra
    */
  lazy val basePeak: Ion = ions.maxBy(_.intensity)

  /**
    * computes the tic for this spectra
    *
    * @return
    */
  lazy val tic: Double = ions.map(_.intensity).sum

  /**
    * a list of model ions used during the deconvolution
    */
  val modelIons: Option[Seq[Double]]

  /**
    * all the defined ions for this spectra
    */
  val ions: Seq[Ion]

  /**
    * the unique splash for this spectra
    */
  final def splash: String = SplashUtil.splash(spectraString, SpectraType.MS)

  /**
    * generates a spectral string representation for us
    *
    * @return
    */
  final def spectraString: String = ions.sortBy(_.mass).map { ion: Ion =>
    f"${ion.mass}%1.6f:${ion.intensity}%1.5f"

  }.mkString(" ")

  /**
    * provides us with a relative spectra string
    */
  def relativeSpectra: Seq[Ion] = {

    val maxIntensity: Float = ions.maxBy(_.intensity).intensity

    ions.map(x => Ion(x.mass, 100 * x.intensity / maxIntensity))
  }

  override def toString = s"SpectrumProperties($msLevel, $modelIons, $ions)"
}

/**
  * defines a MS Spectra
  */
trait MSSpectra extends Feature  {

  override def toString = f"MSSpectra(scanNumber=$scanNumber, retentionTime=$retentionTimeInSeconds%1.2f (s), retentionTime=$retentionTimeInMinutes%1.3f (min))"
}

/**
  * this defines an MSMS spectra
  */
trait MSMSSpectra extends MSSpectra with SimilaritySupport{

  /**
    * the observed pre cursor ion
    */
  val precursorIon: Double
}

/**
  * Marks a Spectrum as acquired in Centroid mode
  */
trait Centroided extends MSSpectra

/**
  * Marks a Spectrum as acquired in Profile mode
  */
trait Profiled extends MSSpectra


/**
  * a corrected spectra
  */
trait CorrectedSpectra {
  val retentionIndex: Double
}

/**
  * we support accurate masses
  */
trait AccurateMassSupport {

  /**
    * returns the accurate mass, of this trait
    *
    * @return
    */
  def accurateMass: Option[Double]
}

/**
  * we support similarity searches
  */
trait SimilaritySupport {

  /**
    * associated spectrum propties if applicable
    */
  val spectrum: Option[SpectrumProperties]

}

