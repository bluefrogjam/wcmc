package edu.ucdavis.fiehnlab.ms.carrot.core.api.io

import java.io._

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties

import scala.io.Source


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
    * adds a new target to the internal list of targets
    *
    * @param target
    */
  def add(target: T,acquisitionMethod: AcquisitionMethod,sample:Option[Sample]): Unit = {
    add(Seq(target),acquisitionMethod,sample)
  }

  /**
    * this will update the existing target with the provided values
    * @param target
    * @param acquisitionMethod
    */
  def update(target: T, acquisitionMethod: AcquisitionMethod):Boolean

  /**
    * deletes a specified target from the library
    * @param target
    * @param acquisitionMethod
    */
  def delete(target: T, acquisitionMethod: AcquisitionMethod) : Unit

  /**
    * deletes the complete library
    */
  def deleteAll : Unit = {
    libraries.foreach{ x =>
      load(x).foreach( y => delete(y,x))
    }
  }
  /**
    * adds a list of targets
    *
    * @param targets
    */
  def add(targets: Iterable[T],acquisitionMethod: AcquisitionMethod,sample:Option[Sample] = None)

  /**
    * returns all associated acuqisiton methods for this library
    * @return
    */
  def libraries : Seq[AcquisitionMethod]
}

/**
  * it's a simple file based reader to get access to targets in a stream
  *
  * @param file
  */
class TxtStreamLibraryAccess[T <: Target](file: File, val seperator: String = "\t") extends LibraryAccess[T] with LazyLogging{

  /**
    * returns all associated acuqisiton methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = AcquisitionMethod(None) :: List()

  /**
    * loads all the spectra from the library
    *
    * @return
    */
  override def load(acquisitionMethod: AcquisitionMethod): Iterable[T] = {
    val result = Source.fromFile(file).getLines().collect {

      case x: String =>
        if (!x.startsWith("#")) {

          val temp = x.split(seperator)

          if (temp.length == 3) {
            new Target {
              override val precursorMass: Option[Double] = Some(temp(1).toDouble)
              override var name: Option[String] = Some(temp(2))
              override val retentionIndex: Double = temp(0).toDouble * 60
              override var inchiKey: Option[String] = None
              override var requiredForCorrection: Boolean = false
              override var isRetentionIndexStandard: Boolean = false
              /**
                * is this a confirmed target
                */
              override var confirmed: Boolean = true
              /**
                * associated spectrum propties if applicable
                */
              override val spectrum: Option[SpectrumProperties] = None
            }
          }
          else if (temp.length == 2) {
            new Target {
              override val precursorMass: Option[Double] = Some(temp(1).toDouble)
              override var name: Option[String] = None
              override val retentionIndex: Double = temp(0).toDouble * 60
              override var inchiKey: Option[String] = None
              override var requiredForCorrection: Boolean = false
              override var isRetentionIndexStandard: Boolean = false
              override var confirmed: Boolean = true
              override val spectrum: Option[SpectrumProperties] = None

            }
          }
          else if (temp.length == 4) {
            new Target {
              override val precursorMass: Option[Double] = Some(temp(1).toDouble)
              override var name: Option[String] = Some(temp(2))
              override val retentionIndex: Double = temp(0).toDouble * 60
              override var inchiKey: Option[String] = None
              override var requiredForCorrection: Boolean = false
              override var isRetentionIndexStandard: Boolean = temp(3).toBoolean
              override var confirmed: Boolean = true
              override val spectrum: Option[SpectrumProperties] = None

            }
          }
          else if (temp.length == 5) {
            new Target {
              override val retentionIndex: Double = temp(0).toDouble * 60
              override val precursorMass: Option[Double] = Some(temp(1).toDouble)
              override var name: Option[String] = Some(temp(2))
              override var inchiKey: Option[String] = None
              override var requiredForCorrection: Boolean = false
              override var isRetentionIndexStandard: Boolean = temp(4).toBoolean
              override var confirmed: Boolean = true
              override val spectrum: Option[SpectrumProperties] = None

            }
          }

          else {
            logger.info(s"target line is: ${temp.mkString(" ")}")
            throw new IOException("unsupported file format discovered!")
          }
        }
    }.collect {
      case x: T => x
    }.toList

    result
  }

  /**
    * adds a new target to the internal list of targets
    *
    * @param targets
    */
  override def add(targets: Iterable[T],acquisitionMethod: AcquisitionMethod,sample:Option[Sample]): Unit = {

    logger.info(s"updating library at: ${file.getAbsolutePath}")
    val out = new FileWriter(file, true)

    targets.foreach { target =>

      writeTarget(out, target)
    }

    out.flush()
    out.close()
  }

  private def writeTarget(out: FileWriter, target: T) = {
    out.write(
      s"""${target.retentionIndex}$seperator${target.precursorMass.get}$seperator${target.name.getOrElse("unknown")}$seperator${target.requiredForCorrection}$seperator${target.inchiKey.getOrElse("")}\n"""
    )
  }

  /**
    * deletes a specified target from the library
    *
    * @param target
    * @param acquisitionMethod
    */
  override def delete(target: T, acquisitionMethod: AcquisitionMethod): Unit = {

    logger.info(s"updating library at: ${file.getAbsolutePath}")
    val out = new FileWriter(file, false)

    load(acquisitionMethod).filterNot(_.equals(target)).foreach{ x =>
      writeTarget(out,x)
    }


    out.flush()
    out.close()
  }

  /**
    * this will update the existing target with the provided values
    *
    * @param target
    * @param acquisitionMethod
    */
  override def update(target: T, acquisitionMethod: AcquisitionMethod): Boolean = false
}