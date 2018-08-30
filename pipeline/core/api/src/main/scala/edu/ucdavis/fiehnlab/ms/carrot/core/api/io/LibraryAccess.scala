package edu.ucdavis.fiehnlab.ms.carrot.core.api.io

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget, Sample, Target}
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConverters._

/**
  * Created by wohlg_000 on 4/22/2016.
  */
trait LibraryAccess[T <: Target] {

  /**
    * loads all the spectra from the library
    * applicable for the given acquistion method
    *
    * @return
    */
  def load(acquisitionMethod: AcquisitionMethod): Iterable[T]

  /**
    * adds a new target to the internal list of targets for the selected method
    *
    * @param target
    */
  def add(target: T, acquisitionMethod: AcquisitionMethod, sample: Option[Sample]): Unit = {
    add(Seq(target), acquisitionMethod, sample)
  }

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
    libraries.foreach { x =>
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
  def deleteLibrary(acquisitionMethod: AcquisitionMethod) = {}

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

  /**
    * adds a list of targets
    *
    * @param targets
    */
  override def add(targets: Iterable[T], acquisitionMethod: AcquisitionMethod, sample: Option[Sample]): Unit = {}

}

class DelegateLibraryAccess[T <: Target] @Autowired()(delegates: java.util.List[LibraryAccess[T]]) extends LibraryAccess[T] {

  /**
    * loads all the spectra from the library
    * applicable for the given acquistion method
    *
    * @return
    */
  override def load(acquisitionMethod: AcquisitionMethod): Iterable[T] = {
    val targets = delegates.asScala.find(_.load(acquisitionMethod).nonEmpty)

    if (targets.isDefined) {
      targets.get.load(acquisitionMethod)
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
    else {
      false
    }

  }

  /**
    * adds a list of targets
    *
    * @param targets
    */
  override def add(targets: Iterable[T], acquisitionMethod: AcquisitionMethod, sample: Option[Sample]): Unit = {
    val t = delegates.asScala.filter(!_.isInstanceOf[ReadonlyLibrary[T]]).find(_.load(acquisitionMethod).nonEmpty)

    if (t.isDefined) {
      t.get.add(targets, acquisitionMethod, sample)
    }

  }

  /**
    * returns all associated acuqisiton methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = {
    delegates.asScala.flatMap(_.libraries)
  }

  override def toString = s"DelegateLibraryAccess(\n\t\t${this.delegates}\n)"
}

class MergeLibraryAccess @Autowired()(correction: LibraryAccess[CorrectionTarget], annotation: LibraryAccess[AnnotationTarget]) extends LibraryAccess[Target] {
  /**
    * loads all the spectra from the library
    * applicable for the given acquistion method
    *
    * @return
    */
  override def load(acquisitionMethod: AcquisitionMethod): Iterable[Target] = {
    this.correction.load(acquisitionMethod) ++ this.annotation.load(acquisitionMethod)
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
    this.annotation.add(targets.map(_.asInstanceOf[AnnotationTarget]), acquisitionMethod)
  }

  /**
    * returns all associated acquisition methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = annotation.libraries.filter(correction.libraries.contains(_))

  override def toString = s"MergeLibraryAccess(\n\n\tannotation: ${annotation.toString}\n\n\tcorrection:${correction.toString})"
}
