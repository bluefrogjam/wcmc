package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._

/**
  * provides us with an easy way to create and query libraries
  * Created by wohlgemuth on 10/16/17.
  */
@CrossOrigin
@RestController
@RequestMapping(value = Array("/rest/library"))
class LibraryController {

  @Autowired
  val libraryAccess: LibraryAccess[Target] = null

  /**
    * this adds a new target to the specified library
    */
  @RequestMapping(value = Array(""), method = Array(RequestMethod.POST))
  def addTarget(@RequestBody target: AddTarget): Unit = {

    val ionMode: IonMode = target.mode.toLowerCase match {
      case "+" => PositiveMode()
      case "positive" => PositiveMode()
      case _ => NegativeMode()
    }

    libraryAccess.add(
      new Target {
        /**
          * the unique inchi key for this spectra
          */
        override val inchiKey: Option[String] = None
        /**
          * retention time in seconds of this target
          */
        override val retentionIndex: Double = target.retentionTime
        /**
          * is this a confirmed target
          */
        override val confirmed: Boolean = true
        /**
          * the mono isotopic mass of this spectra
          */
        override val precursorMass: Option[Double] = Some(target.precursor)
        /**
          * a name for this spectra
          */
        override val name: Option[String] = Some(target.targetName)
        /**
          * is this target required for a successful retention index correction
          */
        override val requiredForCorrection: Boolean = false
        /**
          * is this a retention index correction standard
          */
        override val isRetentionIndexStandard: Boolean = target.riMarker
        /**
          * associated spectrum propties if applicable
          */
        override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
          /**
            * a list of model ions used during the deconvolution
            */
          override val modelIons: Option[Seq[Double]] = None
          /**
            * all the defined ions for this spectra
            */
          override val ions: Seq[Ion] = Seq(Ion(target.precursor, 100.0))
          /**
            * the msLevel of this spectra
            */
          override val msLevel: Short = 1
        })
      },
      AcquisitionMethod(Some(ChromatographicMethod(
        name = target.library, None, None, ionMode = Some(ionMode)))),
      None
    )
  }

  @RequestMapping(value = Array(""), method = Array(RequestMethod.GET))
  def listLibraries(): Seq[String] = {
    libraryAccess.libraries.map{ x =>
      x.chromatographicMethod match {
        case Some(o) => o.name
        case _ => "default"
      }
    }
  }
}

case class AddTarget(targetName: String, precursor: Double, retentionTime: Double, library: String, riMarker: Boolean, mode: String)