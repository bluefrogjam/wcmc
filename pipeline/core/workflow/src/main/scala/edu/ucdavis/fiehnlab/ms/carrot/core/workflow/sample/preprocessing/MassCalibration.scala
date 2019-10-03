package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.SpectraHelper
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.PreProcessor
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, NegativeMode, Sample, SampleProperties}
import edu.ucdavis.fiehnlab.ms.carrot.math.LinearRegression
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * calibrates all the masses in the given samples
  * against a simple curve to improve mass accuracy.
  *
  * This is done by just adding/substracting the delta vs the reference mass for each of the configured pairs
  */
@Component
@Profile(Array("carrot.processing.calibration.simple"))
class SimpleMassCalibration extends PreProcessor with Logging {

  override def priority: Int = 100

  /**
    * actually processes the item (implementations in subclasses)
    *
    * @param item
    * @return
    */
  override def doProcess(item: Sample, method: AcquisitionMethod, rawSample: Option[Sample]): Sample = {


    //negative mass for calibration

    val correctedScans: Seq[_ <: Feature] = item.spectra.collect {

      case f: Feature if f.associatedScan.isDefined =>


        val lockmassLower: Double = f.ionMode match {
          case Some(mode) if mode.isInstanceOf[NegativeMode] => 119.0363
          case _ => 121.0508 //assume positive mode
        }
        val lockmassHigher: Double = f.ionMode match {
          case Some(mode) if mode.isInstanceOf[NegativeMode] => 980.0163
          case _ => 922.0098 //assume positive mode
        }

        try {
          //find our primaery calibration mass
          val primaery = f.associatedScan.get.ions.filter { i => i.mass > (lockmassLower - 0.015) && i.mass < (lockmassLower + 0.015) }.maxBy(p => p.intensity)
          //find our secondaery calibration mass
          val secondaery = f.associatedScan.get.ions.filter { i => i.mass > (lockmassHigher - 0.015) && i.mass < (lockmassHigher + 0.015) }.maxBy(p => p.intensity)

          val linaer = new LinearRegression()
          linaer.calibration(Array(primaery.mass, secondaery.mass), Array(lockmassLower, lockmassHigher))

          val correctedIons = f.associatedScan.get.ions.map { ion: Ion =>
            ion.copy(mass = linaer.computeY(ion.mass))
          }

          val ion = f.massOfDetectedFeature.get

          val correctedMassOfDetectedFeature = ion.copy(mass = linaer.computeY(ion.mass))

          SpectraHelper.addMassCalibration(f, new SpectrumProperties {
            /**
              * a list of model ions used during the deconvolution
              */
            override val modelIons: Option[Seq[Double]] = f.associatedScan.get.modelIons
            /**
              * all the defined ions for this spectra
              */
            override val ions: Seq[Ion] = correctedIons
            /**
              * the msLevel of this spectra
              */
            override val msLevel: Short = f.associatedScan.get.msLevel
          }, correctedMassOfDetectedFeature)
        }
        catch {
          case e: UnsupportedOperationException =>
            logger.warn(s"${f} : error: ${e.getMessage}")
            f
        }
      case f: Feature => f

    }

    //this is the mass calibrated sample
    new Sample {
      /**
        * a collection of spectra
        * belonging to this sample
        */
      override val spectra: Seq[_ <: Feature] = correctedScans
      /**
        * the unique file name of the sample
        */
      override val fileName: String = item.fileName

      override val properties: Option[SampleProperties] = item.properties

    }
  }
}
