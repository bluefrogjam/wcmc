package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.{File, FileWriter, IOException}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{IonMode, Sample, Target}
import org.apache.logging.log4j.scala.Logging

import scala.io.Source

/**
  * it's a simple file based reader to get access to targets in a stream
  *
  * @param file
  */
class TxtStreamLibraryAccess[T <: Target](file: File, val seperator: String = "\t") extends LibraryAccess[T] with Logging {

  /**
    * returns all associated acuqisiton methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = Seq.empty

  /**
   * loads all the spectra from the library
   *
   * @return
   */
  override def load(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean]): Iterable[T] = {
    if (file.getName.split("\\.").head.equals(acquisitionMethod.chromatographicMethod.name)) {
      val result = Source.fromFile(file).getLines().collect {

        case x: String if !x.startsWith("#") =>

          val temp = x.split(seperator)

          if (temp.length == 3) {
            new Target {
              override val precursorMass: Option[Double] = Some(temp(1).toDouble)
              override var name: Option[String] = Some(temp(2))
              override val retentionIndex: Double = temp(0).toDouble * 60
              override var inchiKey: Option[String] = None
              override var requiredForCorrection: Boolean = false
              override var isRetentionIndexStandard: Boolean = false
              override var confirmed: Boolean = true
              override val spectrum: Option[SpectrumProperties] = None
              override val uniqueMass: Option[Double] = None
              override val ionMode: IonMode = acquisitionMethod.chromatographicMethod.ionMode.get
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
              override val uniqueMass: Option[Double] = None
              override val ionMode: IonMode = acquisitionMethod.chromatographicMethod.ionMode.get
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
              override val uniqueMass: Option[Double] = None
              override val ionMode: IonMode = acquisitionMethod.chromatographicMethod.ionMode.get
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
              override val uniqueMass: Option[Double] = None
              override val ionMode: IonMode = acquisitionMethod.chromatographicMethod.ionMode.get
            }
          }

          else {
            logger.info(s"target line is: ${temp.mkString(" ")}")
            throw new IOException("unsupported file format discovered!")
          }
      }.collect {
        case x: T => x
      }.toList

      result
    }
    else {
      logger.info(s"defined acquisition method of name ${acquisitionMethod.chromatographicMethod.name} was not found in this library implementation!")
      Seq.empty
    }
  }

  /**
    * adds a new target to the internal list of targets
    *
    * @param targets
    */
  override def add(targets: Iterable[T], acquisitionMethod: AcquisitionMethod, sample: Option[Sample]): Unit = {

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

    load(acquisitionMethod).filterNot(_.equals(target)).foreach { x =>
      writeTarget(out, x)
    }


    out.flush()
    out.close()
  }

  override def deleteLibrary(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean]): Unit = {}

  /**
    * this will update the existing target with the provided values
    *
    * @param target
    * @param acquisitionMethod
    */
  override def update(target: T, acquisitionMethod: AcquisitionMethod): Boolean = false
}
