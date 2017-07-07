package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSLibrarySpectra, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSpectra, Ion, IonMode}
import org.scalatest.WordSpec

/**
  * Created by wohlgemuth on 6/28/16.
  */
class RetentionIndexAnnotationTest extends WordSpec {

  "RetentionIndexAnnotationTest" should {

    val test = new RetentionIndexAnnotation(5)
    "isMatch" in {

      assert(test.isMatch(

        new MSSpectra with CorrectedSpectra{
          override val purity: Option[Double] = None
        override val ionMode: Option[IonMode] = None
        override val scanNumber: Int = 1
        override val ions: Seq[Ion] = Seq.empty
        override val modelIons: Option[Seq[Double]] = None
        override val msLevel: Short = 1
        override val retentionTimeInSeconds: Double = 200
          override val retentionIndex: Double = retentionTimeInSeconds
        },

        new MSLibrarySpectra {override val quantificationIon: Option[Double] = None
          override val purity: Option[Double] = None
          override val ionMode: Option[IonMode] = None
          override val scanNumber: Int = 1
          override val ions: Seq[Ion] = Seq.empty
          override val modelIons: Option[Seq[Double]] = None
          override val msLevel: Short = 1
          override val monoIsotopicMass: Option[Double] = None
          override val name: Option[String] = None
          override val inchiKey: Option[String] = None
          override val retentionTimeInSeconds: Double = 204.5f

        }))

    }

    "isNoMatch" in {

      assert(!test.isMatch(

        new MSSpectra with CorrectedSpectra{
          override val purity: Option[Double] = None
          override val ionMode: Option[IonMode] = None
          override val scanNumber: Int = 1
          override val ions: Seq[Ion] = Seq.empty
          override val modelIons: Option[Seq[Double]] = None
          override val msLevel: Short = 1
          override val retentionTimeInSeconds: Double = 200
          override val retentionIndex: Double = retentionTimeInSeconds

        },

        new MSLibrarySpectra {override val quantificationIon: Option[Double] = None
          override val purity: Option[Double] = None
          override val ionMode: Option[IonMode] = None
          override val scanNumber: Int = 1
          override val ions: Seq[Ion] = Seq.empty
          override val modelIons: Option[Seq[Double]] = None
          override val msLevel: Short = 1
          override val monoIsotopicMass: Option[Double] = None
          override val name: Option[String] = None
          override val inchiKey: Option[String] = None
          override val retentionTimeInSeconds: Double = 205.5f
        }))

    }

  }


}
