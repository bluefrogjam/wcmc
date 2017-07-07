package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms

import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.RetentionTimeDifference
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SplashUtil

trait Feature {

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
  val ionMode: Option[IonMode] = None

  /**
    * accurate mass of this feature, if applicable
    */
  val accurateMass: Option[Ion] = None

  override def toString = s"Feature($scanNumber, $retentionTimeInSeconds, $accurateMass)"
}

/**
  * defines a MS Spectra
  */
trait MSSpectra extends Feature {

  /**
    * the msLevel of this spectra
    */
  val msLevel: Short = 1

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
    * how pure this spectra is
    */
  val purity: Option[Double]

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

  override def toString = f"MSSpectra(scanNumber=$scanNumber, basePeak=${basePeak.mass}%1.5f, tic=$tic%1.3f, retentionTime=$retentionTimeInSeconds%1.2f (s), retentionTime=$retentionTimeInMinutes%1.3f (min))"
}

/**
  * simple imple of an MSSpectra
  *
  * @param modelIons
  * @param purity
  * @param scanNumber
  * @param ions
  * @param retentionTimeInSeconds
  * @param msLevel
  */
final case class MSSpectraImpl(override val scanNumber: Int,
                               override val ions: Seq[Ion],
                               override val retentionTimeInSeconds: Double,
                               override val msLevel: Short = 1,
                               override val ionMode: Option[IonMode] = None,
                               override val modelIons: Option[Seq[Double]] = None,
                               override val purity: Option[Double] = None,
                               override val accurateMass: Option[Ion]
                              ) extends MSSpectra

/**
  * this defines a Library Spectra
  */
trait MSLibrarySpectra extends MSSpectra with Target {

  /**
    * this ion is used for quantification of data
    * and used for height calculations
    */
  val quantificationIon: Option[Double]

  /**
    * the retention index of this spectra
    */
  override def retentionTimeInMinutes: Double = super.retentionTimeInMinutes
}

/**
  * default impl for a library spectra
  *
  * @param scanNumber
  * @param ions
  * @param retentionTimeInSeconds
  * @param msLevel
  * @param ionMode
  * @param modelIons
  * @param purity
  * @param quantificationIon
  * @param name
  * @param inchiKey
  * @param monoIsotopicMass
  */
sealed case class MSLibrarySpectraImpl(
                                        override val scanNumber: Int,
                                        override val ions: Seq[Ion],
                                        override val retentionTimeInSeconds: Double,
                                        override val msLevel: Short = 1,
                                        override val quantificationIon: Option[Double],
                                        override val name: Option[String],
                                        override val inchiKey: Option[String],
                                        override val monoIsotopicMass: Option[Double],
                                        override val ionMode: Option[IonMode] = None,
                                        override val modelIons: Option[Seq[Double]] = None,
                                        override val purity: Option[Double] = None
                                      ) extends MSLibrarySpectra

/**
  * this defines a spectra, which has been corrected
  *
  * @param spectra
  * @param retentionIndex
  */
sealed case class CorrectedMSSpectra(spectra: MSSpectra, retentionIndex: Double) extends MSSpectra with CorrectedSpectra {
  override val scanNumber: Int = spectra.scanNumber
  override val ions: Seq[Ion] = spectra.ions
  override val retentionTimeInSeconds: Double = spectra.retentionTimeInSeconds
  override val msLevel: Short = spectra.msLevel
  override val modelIons: Option[Seq[Double]] = spectra.modelIons
  override val purity: Option[Double] = spectra.purity
  override val ionMode: Option[IonMode] = spectra.ionMode

}

/**
  * this defines a spectra, which has been annotated
  *
  * @param spectra
  * @param target
  * @param retentionIndex
  */
sealed case class AnnotatedMSSpectra[T <: Target](spectra: MSSpectra with CorrectedSpectra, target: T, retentionIndex: Double, override val massAccuracyPPM: Option[Double], override val massAccuracy: Option[Double]) extends MSSpectra with AnnotatedSpectra with CorrectedSpectra {
  override val scanNumber: Int = spectra.scanNumber
  override val ions: Seq[Ion] = spectra.ions
  override val retentionTimeInSeconds: Double = spectra.retentionTimeInSeconds
  override val msLevel: Short = spectra.msLevel
  override val modelIons: Option[Seq[Double]] = spectra.modelIons
  override val purity: Option[Double] = spectra.purity
  override val ionMode: Option[IonMode] = spectra.ionMode
  override lazy val retentionIndexDistance: Option[Double] = Some(RetentionTimeDifference.inSeconds(target, this))

}

/**
  * this defines an MSMS spectra
  */
trait MSMSSpectra extends MSSpectra {

  /**
    * the observed pre cursor ion
    */
  val precursorIon: Double


  /**
    * the msLevel of this spectra. Can be adjusted by actual sample implementation, using overrides
    */
  override val msLevel: Short = 2
}


/**
  * this defines a spectra, which has been corrected
  *
  * @param spectra
  * @param retentionIndex
  */
sealed case class CorrectedMSMSSpectra(spectra: MSMSSpectra, retentionIndex: Double) extends MSMSSpectra with CorrectedSpectra {
  override val scanNumber: Int = spectra.scanNumber
  override val ions: Seq[Ion] = spectra.ions
  override val retentionTimeInSeconds: Double = spectra.retentionTimeInSeconds
  override val msLevel: Short = spectra.msLevel
  override val precursorIon: Double = spectra.precursorIon
  override val modelIons: Option[Seq[Double]] = spectra.modelIons
  override val purity: Option[Double] = spectra.purity
  override val ionMode: Option[IonMode] = spectra.ionMode
}

/**
  * this defines a spectra, which has been annotated to a target
  *
  * @param spectra
  * @param target
  * @param retentionIndex
  * @tparam T
  */
sealed case class AnnotatedMSMSSpectra[T <: Target](spectra: MSMSSpectra with CorrectedSpectra, target: T, retentionIndex: Double, override val massAccuracyPPM: Option[Double], override val massAccuracy: Option[Double]) extends MSMSSpectra with AnnotatedSpectra with CorrectedSpectra {
  override val scanNumber: Int = spectra.scanNumber
  override val ions: Seq[Ion] = spectra.ions
  override val retentionTimeInSeconds: Double = spectra.retentionTimeInSeconds
  override val msLevel: Short = spectra.msLevel
  override val modelIons: Option[Seq[Double]] = spectra.modelIons
  override val purity: Option[Double] = spectra.purity
  override val ionMode: Option[IonMode] = spectra.ionMode
  override val precursorIon: Double = spectra.precursorIon
  override lazy val retentionIndexDistance: Option[Double] = Some(RetentionTimeDifference.inSeconds(target, this))
}

/**
  * an MSMS Library Spectra
  */
trait MSMSLibrarySpectra extends MSLibrarySpectra with MSMSSpectra

/**
  * default impl
  *
  * @param scanNumber
  * @param ions
  * @param retentionTimeInSeconds
  * @param msLevel
  * @param ionMode
  * @param modelIons
  * @param purity
  * @param precursorIon
  */
sealed case class MSMSSpectraImpl(
                                   override val scanNumber: Int,
                                   override val ions: Seq[Ion],
                                   override val retentionTimeInSeconds: Double,
                                   override val msLevel: Short = 1,
                                   override val ionMode: Option[IonMode] = None,
                                   override val modelIons: Option[Seq[Double]] = None,
                                   override val purity: Option[Double] = None,
                                   override val precursorIon: Double,
                                   override val accurateMass: Option[Ion]

                                 ) extends MSMSSpectra


/**
  * Marks a Spectrum as acquired in Centroid mode
  */
trait Centroided extends MSSpectra

/**
  * Marks a Spectrum as acquired in Profile mode
  */
trait Profiled extends MSSpectra
