package edu.ucdavis.fiehnlab.ms.carrot.core.db.mona

import java.util.Date
import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.ms.carrot.core.api.exception.TargetGenerationNotSupportedException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.context.annotation._
import org.springframework.stereotype.Component

/**
  * provides easy access to the MoNA database to query for targets and add new targets
  * Created by wohlgemuth on 8/14/17.
  */
@Component
@Profile(Array("backend-mona"))
class MonaLibraryAccess extends LibraryAccess[Target] with LazyLogging {

  @Value("${mona.rest.server.user}")
  val username: String = null

  @Value("${mona.rest.server.password}")
  val password: String = null

  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null

  @Autowired
  @Qualifier("monaRestServer")
  val monaRestServer: String = null

  @PostConstruct
  def init = {
    logger.info(s"utilizing mona server for targets at: ${monaRestServer}")
  }


  /**
    * based on the given method this will evaluate to a query against the system to provide us with valid targets
    * for annotation and identification
    */
  def query(acquistionMethod:AcquisitionMethod): String = ""

  /**
    * loads all the spectra from the library
    *
    * @return
    */
  override def load(acquistionMethod:AcquisitionMethod): Iterable[Target] = monaSpectrumRestClient.list(query = if (query(acquistionMethod) != "") Option(query(acquistionMethod)) else None).map { x => generateTarget(x) }

  /**
    * converts the given target to a valid mona spectrum for uploads
    *
    * @param t
    * @return
    */
  def generateSpectrum(t: Target,acquistionMethod:AcquisitionMethod): Option[Spectrum] = {
    if (t.spectrum.isDefined) {
      val compound = Compound(
        inchi = null,
        inchiKey = t.inchiKey.orNull,
        metaData = Array(),
        molFile = null,
        names = Array(Names(computed = false, t.name.getOrElse(s"unknown_${t.retentionTimeInSeconds}_${t.precursorMass.get}"), 0, "carrot")),
        tags = Array(),
        computed = false,
        score = null
      )

      val metaData = Array(
        MetaData(
          category = "carrot",
          computed = false,
          hidden = false,
          name = "retention time",
          score = null,
          unit = "s",
          url = null,
          value = t.retentionTimeInSeconds
        ),
        MetaData(
          category = "carrot",
          computed = false,
          hidden = false,
          name = "precursor m/z",
          score = null,
          unit = "Da",
          url = null,
          value = t.precursorMass.get
        ),
        MetaData(
          category = "carrot",
          computed = false,
          hidden = false,
          name = "riStandard",
          score = null,
          unit = "s",
          url = null,
          value = t.isRetentionIndexStandard
        ),
        MetaData(
          category = "carrot",
          computed = false,
          hidden = false,
          name = "requiredForCorrection",
          score = null,
          unit = "s",
          url = null,
          value = t.requiredForCorrection
        ),
        MetaData(
          category = "carrot",
          computed = false,
          hidden = false,
          name = "confirmed",
          score = null,
          unit = "s",
          url = null,
          value = false
        )
        ,
        MetaData(
          category = "carrot",
          computed = false,
          hidden = false,
          name = "ionization mode",
          score = null,
          unit = "s",
          url = null,
          value = if (t.ionMode.isInstanceOf[PositiveMode]) "positive" else "negative"
        )

      )

      //attach the acquisition method metadata now
      Some(
        Spectrum(
          compound = Array(compound),
          id = null,
          dateCreated = new Date(),
          lastUpdated = new Date(),
          metaData = metaData,
          annotations = Array(),
          score = null,
          spectrum = t.spectrum.get.spectraString,
          splash = null,
          submitter = associateWithSubmitter(),
          tags = Array(Tags(ruleBased = false, "carrot")),
          authors = Array(),
          library = associateWithLibrary()
        )
      )
    }
    else {
      None
    }
  }

  /**
    * associate this submitter to the uploaded spectra
    *
    * @return
    */
  def associateWithSubmitter(): Submitter = Submitter(
    id = "admin",
    emailAddress = "admin@admin.ad",
    firstName = "admin",
    lastName = "admin",
    institution = "admin"
  )

  /**
    * in which library targets should be defined
    *
    * @return
    */
  def associateWithLibrary(): Library = null

  /**
    * converts a spectrum to a target
    *
    * @param x
    * @return
    */
  def generateTarget(x: Spectrum): Target = {

    val compound = x.compound.head
    val name = compound.names.head.name
    val inchi = compound.inchiKey

    val retentionTime: Option[MetaData] = x.metaData.find(p => p.name == "retention time" && p.category == "carrot")
    val precursorIon: Option[MetaData] = x.metaData.find(p => p.name == "precursor m/z" && p.category == "carrot")
    val isRetentionIndexStandard: Option[MetaData] = x.metaData.find(p => p.name == "riStandard" && p.category == "carrot")
    val requiredForCorrection: Option[MetaData] = x.metaData.find(p => p.name == "requiredForCorrection" && p.category == "carrot")
    val confirmed: Option[MetaData] = x.metaData.find(p => p.name == "confirmed" && p.category == "carrot")
    val ionMode = x.metaData.find(p => p.name == "ionization mode" && p.category == "carrot")
    val spectrum = x.spectrum

    val spectrumProperties = new SpectrumProperties {
      /**
        * a list of model ions used during the deconvolution
        */
      override val modelIons: Option[Seq[Double]] = None
      /**
        * all the defined ions for this spectra
        */
      override val ions: Seq[Ion] = spectrum.split(" ").collect {
        case x: String =>
          val values = x.split(":")

          Ion(values(0).toDouble, values(1).toFloat)

      }.filter(_.intensity > 0).toList
    }

    /**
      * our defined method
      */
    MonaLibraryTarget(
      spectrumProperties,
      name = Option(name),
      retentionTimeInSeconds = retentionTime.get.value.asInstanceOf[Double],
      inchiKey = Option(inchi),
      precursorMass = Option(precursorIon.get.value.asInstanceOf[Double]),
      confirmed = if (confirmed.isDefined) confirmed.get.value.asInstanceOf[Boolean] else false,
      requiredForCorrection = if (requiredForCorrection.isDefined) requiredForCorrection.get.value.asInstanceOf[Boolean] else false,
      isRetentionIndexStandard = if (isRetentionIndexStandard.isDefined) isRetentionIndexStandard.get.value.asInstanceOf[Boolean] else false,
      ionMode = if (ionMode.isDefined) {
        if (ionMode.get.value.asInstanceOf[String] == "negative") {
          NegativeMode()
        }
        else if (ionMode.get.value.asInstanceOf[String] == "positive") {
          PositiveMode()
        }
        else {
          Unknown()
        }
      }
      else {
        Unknown()
      }
    )
  }

  /**
    * adds a list of targets
    *
    * @param targets
    */
  override def add(targets: Iterable[Target],acquisitionMethod: AcquisitionMethod): Unit = {
    monaSpectrumRestClient.login(username, password)

    targets.foreach {
      t =>
        val spectrum: Option[Spectrum] = generateSpectrum(t,acquisitionMethod)

        if (spectrum.isDefined) {
          val spec = spectrum.get
          monaSpectrumRestClient.add(spec)
        }
        else {
          throw new TargetGenerationNotSupportedException(s"not possible to generate a target here, due to lack of metadata. Please ensure that you provide all required information", t)
        }
    }
  }
}

/**
  * loads additional required beans for this to work
  */
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[MonaSpectrumRestClient]))
@Import(Array(classOf[RestClientConfig]))
class MonaLibraryAccessConfiguration

/**
  * this defines a valid mona based target in the system
  *
  * @param msmsSpectrum
  * @param name
  * @param retentionTimeInSeconds
  * @param inchiKey
  * @param precursorMass
  * @param confirmed
  * @param requiredForCorrection
  * @param isRetentionIndexStandard
  */
case class MonaLibraryTarget(

                              /**
                                * associate msms spectrum
                                */
                              msmsSpectrum: SpectrumProperties,

                              /**
                                * a name for this spectra
                                */
                              override val name: Option[String],

                              /**
                                * retention time in seconds of this target
                                */
                              override val retentionTimeInSeconds: Double,

                              /**
                                * the unique inchi key for this spectra
                                */
                              override val inchiKey: Option[String],

                              /**
                                * the mono isotopic mass of this spectra
                                */
                              override val precursorMass: Option[Double],

                              /**
                                * is this a confirmed target
                                */
                              override val confirmed: Boolean,

                              /**
                                * is this target required for a successful retention index correction
                                */
                              override val requiredForCorrection: Boolean,

                              /**
                                * is this a retention index correction standard
                                */
                              override val isRetentionIndexStandard: Boolean,

                              /**
                                * the specified ionmode for this target. By default we should always assume that it's positive
                                */
                              override val ionMode: IonMode
                            )
  extends Target {

  /**
    * required since targets expects a spectrum, while its being optional on the carrot level
    */
  override val spectrum = Option(msmsSpectrum)

}
