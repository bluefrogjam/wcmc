package edu.ucdavis.fiehnlab.ms.carrot.core.db.mongo

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{IonMode, Target, Unknown}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.{Document, Field}
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

import scala.annotation.meta.field
import scala.collection.JavaConverters._

/**
  * provides easy access to query against a mongodb for targets
  * Created by wohlgemuth on 8/7/17.
  */
class MongoLibraryAccess @Autowired()(libraryRepository: ILibraryRepository, libraryName: String) extends LibraryAccess[Target] with LazyLogging {
  /**
    * loads all the spectra from the library
    *
    * @return
    */
  override def load: Iterable[Target] = {
    val lib = libraryRepository.findOneByName(libraryName)

    if (lib == null) {
      generateNoneExistingLibrary()
      load
    }
    else {
      lib.targets.asScala.map { t =>
        new Target {
          /**
            * the unique inchi key for this spectra
            */
          override val inchiKey: Option[String] = Option(t.inchiKey)
          /**
            * retention time in seconds of this target
            */
          override val retentionTimeInSeconds: Double = t.retentionTimeInSeconds
          /**
            * a name for this spectra
            */
          override val name: Option[String] = Option(t.name)
          /**
            * the mono isotopic mass of this spectra
            */
          override val monoIsotopicMass: Option[Double] = if (t.monoIsotopicMass == 0.0) None else Some(t.monoIsotopicMass)
          /**
            * is this a confirmed target
            */
          override val confirmedTarget: Boolean = t.confirmedTarget
          /**
            * is this target required for a successful retention index correction
            */
          override val requiredForCorrection: Boolean = t.requiredForCorrection
          /**
            * is this a retention index correction standard
            */
          override val isRetentionIndexStandard: Boolean = t.isRetentionIndexStandard
        }
      }
    }
  }

  /**
    * provides us with access to an empty library and it's default values
    */
  protected def generateNoneExistingLibrary(): Unit = {
    val lib = Library(libraryName, Unknown(), new util.ArrayList[LibraryTarget](), Matrix("default"))
    logger.debug(s"created: ${lib}")
    val res = libraryRepository.save(lib)
    logger.debug(s"saved: ${res}")
    logger.debug(s"we have now ${libraryRepository.count()} libraries")
  }

  /**
    * adds a list of targets to our library
    *
    * @param targets
    */
  override def add(targets: Iterable[Target]): Unit = {

    val lib = libraryRepository.findOneByName(libraryName)

    if (lib == null) {
      generateNoneExistingLibrary()
      add(targets)
    }
    else {
      logger.info(s"added ${targets.size} to ${lib}")
      val res = libraryRepository.save(lib.copy(targets = (lib.targets.asScala.toSeq ++ targets.toSeq.map { t: Target =>
        LibraryTarget(
          name = t.name.orNull,
          retentionTimeInSeconds = t.retentionTimeInSeconds,
          inchiKey = t.inchiKey.orNull,
          monoIsotopicMass = t.monoIsotopicMass.getOrElse(0.0),
          confirmedTarget = t.confirmedTarget,
          requiredForCorrection = t.requiredForCorrection,
          isRetentionIndexStandard = t.isRetentionIndexStandard
        )
      }).asJava))
      logger.info(s"result is: ${res}")
    }
  }
}

case class Matrix(
                   name: String
                 )

/**
  * defines a target library to be utilized by the system
  *
  * @param name
  * @param ionmode
  * @param targets
  * @param matrix
  */
@Document(collection = "target_library")
case class Library(@(Id@field)
                   name: String, ionmode: IonMode, targets: java.util.Collection[LibraryTarget], matrix: Matrix)

case class LibraryTarget(
                          /**
                            * a name for this spectra
                            */
                          name: String,

                          /**
                            * retention time in seconds of this target
                            */
                          retentionTimeInSeconds: Double,

                          /**
                            * the unique inchi key for this spectra
                            */
                          inchiKey: String,

                          /**
                            * the mono isotopic mass of this spectra
                            */
                          monoIsotopicMass: Double,

                          /**
                            * is this a confirmed target
                            */
                          confirmedTarget: Boolean,

                          /**
                            * is this target required for a successful retention index correction
                            */
                          requiredForCorrection: Boolean,

                          /**
                            * is this a retention index correction standard
                            */
                          isRetentionIndexStandard: Boolean
                        )

@Repository
trait ILibraryRepository extends PagingAndSortingRepository[Library, String] {
  def findOneByName(name: String): Library
}