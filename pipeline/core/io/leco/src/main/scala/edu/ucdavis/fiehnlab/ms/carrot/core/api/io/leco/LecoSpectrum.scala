package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.leco

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSSpectra, SimilaritySupport, SpectrumProperties}

/**
  * This provides us with an easy way to read LECO gcms samples into the system
  */
case class LecoSpectrum(
                         override val spectrum: Option[SpectrumProperties],
                         override val uniqueMass: Double,
                         override val sample: String,
                         override val purity: Option[Double],
                         override val scanNumber: Int,
                         override val retentionTimeInSeconds: Double
                       )
  extends MSSpectra with SimilaritySupport with UniqueMassSupport {


  lazy override val associatedScan: Option[SpectrumProperties] = spectrum
  override val massOfDetectedFeature: Option[Ion] = None
  override val ionMode: Option[IonMode] = Some(PositiveMode())

}
