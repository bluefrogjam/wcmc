package edu.ucdavis.fiehnlab.ms.carrot.core.db.binbase

import java.sql.{Connection, DriverManager, ResultSet}
import java.util
import java.util.Properties
import javax.validation.constraints.{NotBlank, NotEmpty}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.ReadonlyLibrary
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
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
class BinBaseLibraryAccess @Autowired()(config: BinBaseConnectionProperties) extends ReadonlyLibrary[BinBaseTarget] with LazyLogging{
  /**
    * loads all the spectra from the library
    * applicable for the given acquistion method
    *
    * @return
    */
  override def load(acquisitionMethod: AcquisitionMethod): Iterable[BinBaseTarget] = {
    acquisitionMethod.chromatographicMethod match {
      case Some(method) =>
        method.column match {
          case Some(column) =>

            val connection = generateConnection(column)
            try {
              val res = connection.createStatement().executeQuery("select * from bin where bin_id not in (select bin_id from standard)")

              new Iterator[BinBaseTarget] {
                def hasNext = res.next()

                def next() = BinBaseTarget(res)
              }.toSeq
            }
            finally {
              connection.close()
            }
          case _ => Seq.empty
        }
      case _ => Seq.empty
    }
  }

  def generateConnection(column: String): Connection = {
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
    AcquisitionMethod(Option(
      ChromatographicMethod(
        config.name,
        Option("Leco GC-TOF"),
        Option(column),
        Option(PositiveMode())
      )
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
}


case class BinBaseTarget(result: ResultSet) extends Target with PuritySupport with UniqueMassSupport with LazyLogging {
  /**
    * a name for this spectra
    */
  override var name: Option[String] = Option(result.getString("name"))
  /**
    * retention time in seconds of this target
    */
  override val retentionIndex: Double = result.getDouble("retention_index")
  /**
    * the unique inchi key for this spectra
    */
  override var inchiKey: Option[String] = None
  /**
    * the mono isotopic mass of this spectra
    */
  override val precursorMass: Option[Double] = None
  /**
    * is this a confirmed target
    */
  override var confirmed: Boolean = true
  /**
    * is this target required for a successful retention index correction
    */
  override var requiredForCorrection: Boolean = false
  /**
    * is this a retention index correction standard
    */
  override var isRetentionIndexStandard: Boolean = false

  /**
    * associated spectrum propties if applicable
    */
  override val spectrum: Option[SpectrumProperties] = Option(new SpectrumProperties {
    /**
      * the msLevel of this spectra
      */
    override val msLevel: Short = 1
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[Seq[Double]] = {

      Option({
        result.getString("apex").trim.split("\\++").map(_.toDouble)
      })
    }
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = result.getString("spectra").trim.split(" ").map { p =>
      val data = p.split(":")

      Ion(data(0).toDouble, data(1).toDouble)
    }
  })
  override val purity: Double = result.getDouble("purity")

  override val uniqueMass: Double = result.getDouble("uniquemass")
}
