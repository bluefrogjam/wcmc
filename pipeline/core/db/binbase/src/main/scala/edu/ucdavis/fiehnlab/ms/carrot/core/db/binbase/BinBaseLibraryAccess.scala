package edu.ucdavis.fiehnlab.ms.carrot.core.db.binbase

import java.sql.{Connection, DriverManager, ResultSet}
import java.util
import java.util.Properties

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, ReadonlyLibrary}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.correction.GCMSCorrectionTarget
import javax.validation.constraints.{NotBlank, NotEmpty}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

/**
  * provides us with a read only access to the binbase database library
  * located at the specified properties
  */
@Component
@Profile(Array("carrot.gcms.library.binbase"))
class BinBaseLibraryAccess @Autowired()(config: BinBaseConnectionProperties, correction: LibraryAccess[GCMSCorrectionTarget]) extends ReadonlyLibrary[Target] with LazyLogging {
  /**
    * loads all the spectra from the library
    * applicable for the given acquistion method
    *
    * @return
    */
  override def load(acquisitionMethod: AcquisitionMethod): Iterable[Target] = {
    acquisitionMethod.chromatographicMethod.column match {
      case Some(column) =>

        val connection = generateConnection(column)
        try {
          val res = connection.createStatement().executeQuery("select * from bin where bin_id not in (select bin_id from standard)")

          val annotations = new Iterator[Target] {
            def hasNext = res.next()

            def next() = BinBaseTarget(res).asInstanceOf[Target]
          }.toSeq

          val correctionMarkers = correction.load(acquisitionMethod)

          annotations ++ correctionMarkers
        }
        finally {
          connection.close()
        }
      case _ => Seq.empty
    }
  }

  protected def generateConnection(column: String): Connection = {
    classOf[org.postgresql.Driver]
    val url = s"jdbc:postgresql://${config.host}/${config.database}"
    val props = new Properties
    props.setProperty("user", column)
    props.setProperty("password", config.password)

    logger.info(s"connecting too ${url}")
    DriverManager.getConnection(url, props)

  }

  /**
    * returns all associated acuqisiton methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = config.columns.asScala.map { column =>
    AcquisitionMethod(
      ChromatographicMethod(
        config.name,
        Option(config.instrument),
        Option(column),
        Option(PositiveMode())
      )
    )
  }
}

@Component
@Validated
@Profile(Array("carrot.gcms.library.binbase"))
@ConfigurationProperties(prefix = "carrot.gcms.library.binbase", ignoreUnknownFields = false, ignoreInvalidFields = false)
class BinBaseConnectionProperties {

  @BeanProperty
  @NotBlank
  var host: String = "venus.fiehnlab.ucdavis.edu"

  @BeanProperty
  @NotBlank
  var database: String = "binbase"

  @BeanProperty
  @NotBlank
  var password: String = ""

  @BeanProperty
  @NotEmpty
  var columns: java.util.List[String] = new util.ArrayList[String]()

  @BeanProperty
  @NotBlank
  var name: String = "remote:"

  @BeanProperty
  @NotEmpty
  var instrument: String = ""
}

