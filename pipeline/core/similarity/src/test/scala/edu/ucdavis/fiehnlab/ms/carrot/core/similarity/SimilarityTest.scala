package edu.ucdavis.fiehnlab.ms.carrot.core.similarity

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, IonMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, SpectrumProperties}
import org.scalatest.WordSpec

class SimilarityTest extends WordSpec {

  def buildTestFeature(spectrum: Seq[Ion]): Feature = {
    new Feature {
      override val sample: String = null
      override val purity: Option[Double] = None
      override val signalNoise: Option[Double] = None
      override val uniqueMass: Option[Double] = None
      override val scanNumber: Int = -1
      override val retentionTimeInSeconds: Double = 0
      override val ionMode: Option[IonMode] = None
      override val associatedScan: Option[SpectrumProperties] = Some(new SpectrumProperties {
        override val msLevel: Short = 1
        override val modelIons: Option[Seq[Double]] = None
        override val ions: Seq[Ion] = spectrum
      })
      override val massOfDetectedFeature: Option[Ion] = None
      override val metadata: Map[String, AnyRef] = Map()
    }
  }
}
