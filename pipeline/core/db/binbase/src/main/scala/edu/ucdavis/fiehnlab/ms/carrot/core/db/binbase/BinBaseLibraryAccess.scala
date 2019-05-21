package edu.ucdavis.fiehnlab.ms.carrot.core.db.binbase

import java.sql.{Connection, DriverManager}
import java.util
import java.util.Properties

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, ReadonlyLibrary}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod, Idable}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.GCMSCorrectionTarget
import javax.validation.constraints.{NotBlank, NotEmpty}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.{ComponentScan, Configuration, Profile}
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

@Configuration
@Profile(Array("carrot.gcms.library.binbase"))
@ComponentScan
class BinBaseGCMSConfiguration

/**
  * provides us with a read only access to the binbase database library
  * located at the specified properties
  */
@Component
@Profile(Array("carrot.gcms.library.binbase"))
class BinBaseLibraryAccess @Autowired()(config: BinBaseConnectionProperties) extends ReadonlyLibrary[AnnotationTarget] with Logging {

  /**
    * internal query to be executed
    */
  var binQuery: String = "select * from bin where bin_id not in (select bin_id from standard)"

  /**
    * loads all the spectra from the library
    * applicable for the given acquistion method
    *
    * @return
    */
  override def load(acquisitionMethod: AcquisitionMethod): Iterable[AnnotationTarget] = {
    acquisitionMethod.chromatographicMethod.column match {
      case Some(column) =>

        val connection = generateConnection(column)
        try {
          val result = connection.createStatement().executeQuery(binQuery)

          val annotations = new Iterator[AnnotationTarget] {
            def hasNext = result.next()

            def next() = BinBaseLibraryTarget(
              binId = result.getInt("bin_id").toString,
              binName = result.getString("name"),
              ri = result.getDouble("retention_index"),
              apexMasses = result.getString("apex").trim.split("\\++").map(_.toDouble),
              masses = result.getString("spectra").trim.split(" ").map { p =>
                val data = p.split(":")

                Ion(data(0).toDouble, data(1).toFloat)
              },
              unique = result.getDouble("uniquemass")
            ).asInstanceOf[AnnotationTarget]
          }.toSeq

          annotations
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
@ConfigurationProperties(prefix = "wcmc.workflow.gcms.library.binbase", ignoreUnknownFields = false, ignoreInvalidFields = false)
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


case class BinBaseLibraryTarget(
                                 binId: String,
                                 binName: String,
                                 ri: Double,
                                 apexMasses: Seq[Double],
                                 masses: Seq[Ion],
                                 unique: Double
                               )
  extends AnnotationTarget with Idable[String] {
  /**
    * a name for this spectra
    */
  override var name: Option[String] = Option(binName)
  /**
    * retention time in seconds of this target
    */
  override val retentionIndex: Double = ri
  /**
    * the unique inchi key for this spectra
    */
  override var inchiKey: Option[String] = None
  /**
    * the mono isotopic mass of this spectra
    */
  override val precursorMass: Option[Double] = None
  /**
    * unique mass for a given target
    */
  override val uniqueMass: Option[Double] = Option(unique)
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
  override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
    /**
      * the msLevel of this spectra
      */
    override val msLevel: Short = 1
    /**
      * a list of model ions used during the deconvolution
      */
    override val modelIons: Option[Seq[Double]] = modelIons
    /**
      * all the defined ions for this spectra
      */
    override val ions: Seq[Ion] = masses
  })

  /**
    * internal id
    *
    * @return
    */
  override def id: String = binId
}
