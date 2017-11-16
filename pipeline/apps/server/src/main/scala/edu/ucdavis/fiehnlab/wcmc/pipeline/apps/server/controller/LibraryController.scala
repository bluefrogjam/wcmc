package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod, Idable}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.db.mona.MonaLibraryTarget
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation._

/**
  * provides us with an easy way to create and query libraries
  * Created by wohlgemuth on 10/16/17.
  */
@CrossOrigin
@RestController
@RequestMapping(value = Array("/rest/library"))
class LibraryController extends LazyLogging {

  @Autowired
  val libraryAccess: LibraryAccess[Target] = null

  /**
    * this adds a new target to the specified library
    */
  @RequestMapping(value = Array(""), method = Array(RequestMethod.POST))
  def addTarget(@RequestBody target: AddTarget): Unit = {

    val t = target.buildTarget

    val method = target.buildMethod

    val existingTargets = libraryAccess.load(method)

    logger.info(s"library contains currently ${existingTargets.size} targets")

    val hasExistings = existingTargets.filter(_.equals(t))

    if (hasExistings.isEmpty) {
      libraryAccess.add(t, method, None)
    }
    else {
      logger.info(s"target already existed: $t")
      logger.debug(s"\t=>existing target was: ${hasExistings}")
      throw new ResourceAlreadyExistException
    }
  }

  /**
    * updates a given target
    *
    * @param target
    */
  @RequestMapping(value = Array("{library}"), method = Array(RequestMethod.PUT))
  def updateTarget(@PathVariable("library") id: String, @RequestBody target: TargetExtended): Iterable[Target] = {
    logger.info(s"Update requested: $target")
    val result = libraryAccess.libraries.collectFirst {
      case x: AcquisitionMethod if x.chromatographicMethod.isDefined && x.chromatographicMethod.get.name == id =>
        x
    }

    if(result.isDefined){
      logger.info(s"Result: $result")
      libraryAccess.update(target,result.get)

      Array(target)
    }
    else{
      throw new ResourceNotFoundException
    }

  }


  @RequestMapping(value = Array("{library}"), method = Array(RequestMethod.GET))
  def listTargets(@PathVariable("library") id: String): Iterable[Target] = {

    val result = libraryAccess.libraries.collectFirst {
      case x: AcquisitionMethod if x.chromatographicMethod.isDefined && x.chromatographicMethod.get.name == id =>
        libraryAccess.load(x)
    }

    if (result.isDefined) {
      result.get
    }
    else {
      Seq.empty[Target]
    }
  }

  @RequestMapping(value = Array(""), method = Array(RequestMethod.GET))
  def listLibraries(): Seq[AcquisitionMethod] = {
    libraryAccess.libraries
  }
}

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ResourceNotFoundException extends RuntimeException

@ResponseStatus(value = HttpStatus.CONFLICT)
class ResourceAlreadyExistException extends RuntimeException
/**
  * utilized to update fields of the given target
  *
  * @param target
  */
case class UpdateTarget(target: Target, library: String)

case class TargetExtended(override var confirmed: Boolean,
                          id: String,
                          override var inchiKey: Option[String],
                          override val ionMode: IonMode,
                          override var isRetentionIndexStandard: Boolean,
                          msmsSpectrum: Option[SpectrumExtended],
                          override var name: Option[String],
                          override val precursorMass: Option[Double],
                          override var requiredForCorrection: Boolean,
                          override val retentionIndex: Double,
                          override val retentionTimeInSeconds: Double,
                          override val spectrum: Option[SpectrumExtended]
                         ) extends Target with Idable[String] {
  override def toString = f"Target(id=${id}, name=${name.getOrElse("None")}, retentionTime=$retentionTimeInMinutes (min), retentionTime=$retentionIndex (s), inchiKey=${inchiKey.getOrElse("None")}, monoIsotopicMass=${precursorMass.getOrElse("None")})"

}

case class SpectrumExtended(override val ions: Seq[Ion],
                            override val modelIons: Option[Seq[Double]],
                            override val msLevel: Short
                           ) extends SpectrumProperties{

}

/**
  * specific class to add a target
  *
  * @param targetName
  * @param precursor
  * @param retentionTime
  * @param library
  * @param riMarker
  * @param mode
  */
case class AddTarget(targetName: String, precursor: Double, retentionTime: Double, library: String, riMarker: Boolean, mode: String) {

  /**
    * builds the associated acquition method
    *
    * @return
    */
  def buildMethod: AcquisitionMethod = {
    val target: AddTarget = this


    val ionMode: IonMode = target.mode.toLowerCase match {
      case "+" => PositiveMode()
      case "positive" => PositiveMode()
      case _ => NegativeMode()
    }

    AcquisitionMethod(
      Some(
        ChromatographicMethod(
          //TODO possibile bug later down the line, due to explicietly set to none for instrument and column
          name = target.library, None, None, ionMode = Some(ionMode)
        )
      )
    )
  }

  /**
    * builds a target out of the given information
    *
    * @return
    */
  def buildTarget: Target = {
    val target: AddTarget = this

    new Target {
      /**
        * the unique inchi key for this spectra
        */
      override var inchiKey: Option[String] = None
      /**
        * retention time in seconds of this target
        */
      override val retentionIndex: Double = target.retentionTime
      /**
        * is this a confirmed target
        */
      override var confirmed: Boolean = true
      /**
        * the mono isotopic mass of this spectra
        */
      override val precursorMass: Option[Double] = Some(target.precursor)
      /**
        * a name for this spectra
        */
      override var name: Option[String] = Some(target.targetName)
      /**
        * is this target required for a successful retention index correction
        */
      override var requiredForCorrection: Boolean = false
      /**
        * is this a retention index correction standard
        */
      override var isRetentionIndexStandard: Boolean = target.riMarker
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
    }
  }
}

case class Library(name: String, ionMode: String)