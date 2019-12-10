package edu.ucdavis.fiehnlab.ms.carrot.core.api.io

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget, Sample, Target}
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConverters._

/**
  * Created by wohlg_000 on 4/22/2016.
  */
trait LibraryAccess[T <: Target] extends Logging {

  /**
   * loads all the spectra from the library
   * applicable for the given acquistion method
   *
   * @return
   */
  def load(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean] = Some(true)): Iterable[T]

  /**
    * this will update the existing target with the provided values in the selected method
    *
    * @param target
    * @param acquisitionMethod
    */
  def update(target: T, acquisitionMethod: AcquisitionMethod): Boolean

  /**
    * deletes a specified target from the acquisition method
    *
    * @param target
    * @param acquisitionMethod
    */
  def delete(target: T, acquisitionMethod: AcquisitionMethod): Unit

  /**
    * deletes the complete library
    */
  def deleteAll: Unit = {
    logger.warn(s"deleting all libraries")
    libraries.foreach { x =>
      logger.debug(s"\t${x}")
      load(x).foreach(y => delete(y, x))
    }
  }

  /**
    * adds a list of targets
    *
    * @param targets
    */
  def add(targets: Iterable[T], acquisitionMethod: AcquisitionMethod, sample: Option[Sample] = None)

  /**
    * returns all associated acquisition methods for this library
    *
    * @return
    */
  def libraries: Seq[AcquisitionMethod]

  /**
   * deletes the specified acquisition method from the list
   *
   * @param acquisitionMethod
   */
  def deleteLibrary(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean])

  override def toString = s"${getClass.getName}(\n\n${libraries.mkString("\n\t")}\n)"
}

/**
  * a read only implementation, which ignores any write access
  *
  * @tparam T
  */
trait ReadonlyLibrary[T <: Target] extends LibraryAccess[T] {

  /**
    * this will update the existing target with the provided values
    *
    * @param target
    * @param acquisitionMethod
    */
  override def update(target: T, acquisitionMethod: AcquisitionMethod): Boolean = false

  /**
    * deletes a specified target from the library
    *
    * @param target
    * @param acquisitionMethod
    */
  override def delete(target: T, acquisitionMethod: AcquisitionMethod): Unit = {}

  override def deleteLibrary(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean] = None): Unit = {}

  /**
    * adds a list of targets
    *
    * @param targets
    */
  override def add(targets: Iterable[T], acquisitionMethod: AcquisitionMethod, sample: Option[Sample]): Unit = {}

  /**
    * adds the first annotation target
    */
}

trait ReadWriteLibrary[T <: Target] extends LibraryAccess[T] {

  /**
    * this will update the existing target with the provided values in the selected method
    *
    * @param target
    * @param acquisitionMethod
    */
  override def update(target: T, acquisitionMethod: AcquisitionMethod): Boolean = ???

  /**
    * deletes a specified target from the acquisition method
    *
    * @param target
    * @param acquisitionMethod
    */
  override def delete(target: T, acquisitionMethod: AcquisitionMethod): Unit = ???

  /**
    * adds a list of targets
    *
    * @param targets
    */
  override def add(targets: Iterable[T], acquisitionMethod: AcquisitionMethod, sample: Option[Sample]): Unit = ???

}

final class DelegateLibraryAccess[T <: Target] @Autowired()(delegates: java.util.List[LibraryAccess[T]]) extends LibraryAccess[T] with Logging {
  logger.debug("==== creating delegate library ====")

  /**
   * loads all the spectra from the library
   * applicable for the given acquisition method
   *
   * @return
   */
  override def load(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean]): Iterable[T] = {
    logger.debug(s"\tLoading method: ${acquisitionMethod.toString}")
    val targets = delegates.asScala.find(_.load(acquisitionMethod).nonEmpty)

    if (targets.isDefined) {
      targets.get.load(acquisitionMethod, confirmed)
    }
    else {
      Seq.empty
    }
  }

  /**
    * this will update the existing target with the provided values
    *
    * @param target
    * @param acquisitionMethod
    */
  override def update(target: T, acquisitionMethod: AcquisitionMethod): Boolean = {
    val targets = delegates.asScala.filter(!_.isInstanceOf[ReadonlyLibrary[T]]).find(_.load(acquisitionMethod).nonEmpty)

    if (targets.isDefined) {
      targets.get.update(target, acquisitionMethod)
    }
    else {
      false
    }
  }

  /**
    * deletes a specified target from the library
    *
    * @param target
    * @param acquisitionMethod
    */
  override def delete(target: T, acquisitionMethod: AcquisitionMethod): Unit = {
    val targets = delegates.asScala.filter(!_.isInstanceOf[ReadonlyLibrary[T]]).find(_.load(acquisitionMethod).nonEmpty)

    if (targets.isDefined) {
      targets.get.delete(target, acquisitionMethod)
    }

  }

  def deleteLibrary(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean]): Unit = {
    libraries.filter(_.chromatographicMethod == acquisitionMethod.chromatographicMethod).foreach(am =>
      if (!am.isInstanceOf[ReadonlyLibrary[T]])
        deleteLibrary(am, confirmed)
    )
  }

  /**
    * adds a list of targets
    *
    * @param targets
    */
  override def add(targets: Iterable[T], acquisitionMethod: AcquisitionMethod, sample: Option[Sample]): Unit = {

    delegates.asScala
        .filterNot(_.isInstanceOf[ReadonlyLibrary[T]])
        .filterNot(_.isInstanceOf[DelegateLibraryAccess[T]])
        .foreach { lib => {
          logger.debug(s"adding target to ${lib.getClass.getSimpleName}")
          lib.add(targets, acquisitionMethod, sample)
        }
        }
  }

  /**
    * returns all associated acquisition methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = {
    val begin = System.currentTimeMillis()
    try {

      delegates.asScala.flatMap(_.libraries)
    }
    finally {
      logger.info(s"computing all libraries took ${System.currentTimeMillis() - begin} ms")
    }
  }

  override def toString = s"DelegateLibraryAccess(\n\t${this.delegates}\n)"
}

final class MergeLibraryAccess @Autowired()(correction: DelegateLibraryAccess[CorrectionTarget], annotation: DelegateLibraryAccess[AnnotationTarget]) extends LibraryAccess[Target] with Logging {
  logger.info(s"creating merged library, based on ${correction} for correction and ${annotation} for annotation")

  /**
   * loads all the spectra from the library
   * applicable for the given acquistion method
   *
   * @return
   */
  override def load(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean]): Iterable[Target] = {
    logger.debug(s"Loading method: ${acquisitionMethod.toString}")
    this.correction.load(acquisitionMethod, confirmed) ++ this.annotation.load(acquisitionMethod, confirmed)
  }

  /**
    * this will update the existing target with the provided values in the selected method
    *
    * @param target
    * @param acquisitionMethod
    */
  override def update(target: Target, acquisitionMethod: AcquisitionMethod): Boolean = this.annotation.update(target.asInstanceOf[AnnotationTarget], acquisitionMethod)

  /**
    * deletes a specified target from the acquisition method
    *
    * @param target
    * @param acquisitionMethod
    */
  override def delete(target: Target, acquisitionMethod: AcquisitionMethod): Unit = this.annotation.delete(target.asInstanceOf[AnnotationTarget], acquisitionMethod)

  /**
    * adds a list of targets
    *
    * @param targets
    */
  override def add(targets: Iterable[Target], acquisitionMethod: AcquisitionMethod, sample: Option[Sample]): Unit = {
    this.annotation.add(targets.map(_.asInstanceOf[AnnotationTarget]), acquisitionMethod, sample)
  }

  /**
    * returns all associated acquisition methods for this library
    *
    * @return
    */

  // !!! Correction library might have different name than annotation library
  override def libraries: Seq[AcquisitionMethod] = annotation.libraries

  def correctionLibraries(acquisitionMethod: AcquisitionMethod): Iterable[CorrectionTarget] = {
    this.correction.load(acquisitionMethod)
  }

  def annotationLibraries(acquisitionMethod: AcquisitionMethod): Iterable[AnnotationTarget] = {
    this.annotation.load(acquisitionMethod)
  }

  override def toString = s"MergeLibraryAccess(\n\tannotation: ${annotation.toString}\n\tcorrection:${correction.toString})"

  /**
   * deletes the specified acquisition method from the list
   *
   * @param acquisitionMethod
   */
  override def deleteLibrary(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean]): Unit = {
    libraries.filter(_.chromatographicMethod == acquisitionMethod.chromatographicMethod).foreach(am =>
      if (!am.isInstanceOf[ReadonlyLibrary[AnnotationTarget]])
        deleteLibrary(am, confirmed)
    )
  }
}
