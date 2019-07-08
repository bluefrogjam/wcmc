package edu.ucdavis.fiehnlab.ms.carrot.core.db.mona

import java.util.Date
import java.util.concurrent.{ExecutorService, Executors}

import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.{GenericRestClient, MonaSpectrumRestClient}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.ms.carrot.core.api.exception.{InvalidIonModeDefinedException, IonModeNotDefinedException, TargetGenerationNotSupportedException}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod, Idable}
import javax.annotation.PostConstruct
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.annotation._
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * provides easy access to the MoNA database to query for targets and add new targets
  * Created by wohlgemuth on 8/14/17.
  */
@Profile(Array("carrot.targets.mona"))
@Component
class MonaLibraryAccess extends LibraryAccess[AnnotationTarget] with Logging {

  private val executionService: ExecutorService = Executors.newFixedThreadPool(1)

  private val noneSpecifiedValue = "unknown"

  @Value("${mona.rest.server.user}")
  private val username: String = null

  @Value("${mona.rest.server.password}")
  private val password: String = null

  @Autowired
  private val monaSpectrumRestClient: MonaSpectrumRestClient = null

  @Autowired
  @Qualifier("monaRestServer")
  private val monaRestServer: String = null

  @Autowired
  private val restTemplate: RestTemplate = null

  @Autowired
  private val loginService: LoginService = null

  @Autowired
  private val userSerivce: GenericRestClient[Submitter, String] = null

  @PostConstruct
  def init = {
    logger.info(s"utilizing mona server for targets at: ${monaRestServer}")

    val token = loginService.login(username, password).token

    logger.info(s"login token is: ${token}")
    monaSpectrumRestClient.login(token)
    userSerivce.login(token)

    val info = loginService.info(token)

    info.roles.asScala.foreach { x =>
      logger.info(s"role: ${x}")
    }

    logger.info(s"username: ${info.username}")
    logger.info(s"from: ${info.validFrom}")
    logger.info(s"to: ${info.validTo}")

  }

  /**
    * based on the given method this will evaluate to a query against the system to provide us with valid targets
    * for annotation and identification
    */

  def query(acquistionMethod: AcquisitionMethod): String =
    s"""(tags.text=="${generateLibraryIdentifier(acquistionMethod)}")"""

  /**
    * loads all the spectra from the library
    *
    * @return
    */
  override def load(acquistionMethod: AcquisitionMethod): Iterable[AnnotationTarget] = {
    monaSpectrumRestClient.list(query = if (query(acquistionMethod) != "") Option(query(acquistionMethod)) else None).map { x => generateTarget(x) }

  }

  private def generateAcquisitonInfo(data: Array[MetaData], acquistionMethod: AcquisitionMethod): Array[MetaData] = {

    val buffer: mutable.Buffer[MetaData] = data.toBuffer

    acquistionMethod.chromatographicMethod.column match {
      case Some(column) =>
        buffer +=
          MetaData(
            category = "none",
            computed = false,
            hidden = false,
            name = "column",
            score = null,
            unit = null,
            url = null,
            value = column
          )
      case None => None

    }
    acquistionMethod.chromatographicMethod.instrument match {
      case Some(instrument) =>
        buffer +=
          MetaData(
            category = "none",
            computed = false,
            hidden = false,
            name = "instrument",
            score = null,
            unit = null,
            url = null,
            value = instrument
          )
      case None => None

    }

    buffer +=
      MetaData(
        category = "none",
        computed = false,
        hidden = false,
        name = "method",
        score = null,
        unit = null,
        url = null,
        value = acquistionMethod.chromatographicMethod.name
      )


    buffer.toArray
  }

  /**
    * converts the given target to a valid mona spectrum for uploads
    *
    * @param t
    * @return
    */
  private def generateSpectrum(t: Target, acquistionMethod: AcquisitionMethod, sample: Option[Sample]): Option[Spectrum] = {
    val compound = Compound(
      inchi = null,
      inchiKey = t.inchiKey.orNull,
      metaData = Array(),
      molFile = null,
      names = Array(Names(computed = false, t.name.getOrElse(f"unknown_${t.retentionIndex}%1.4f_${t.precursorMass.get}%1.4f"), 0, "carrot")),
      tags = Array(),
      computed = false,
      score = null
    )

    val metaData: Array[MetaData] = generateAcquisitonInfo(generateDefaultMetaData(t, sample), acquistionMethod)

    //attach the acquisition method metadata now
    Some(
      Spectrum(
        compound = Array(compound),
        id = t match {
          case x: Target with Idable[String] =>
            logger.debug(s"associate id: ${x.id}")
            x.id
          case _ =>
            logger.debug(s"no id defined! ${t}")
            null
        },
        dateCreated = new Date(),
        lastUpdated = new Date(),
        metaData = metaData,
        annotations = Array(),
        score = null,
        spectrum = if (t.spectrum.isDefined) {
          t.spectrum.get.spectraString
        }
        else {
          logger.warn(s"has no spectra associated, using pre cursor mass as simulated spectra: ${t}")
          s"${t.precursorMass.get}:100"
        },
        splash = null,
        submitter = associateWithSubmitter(),
        tags = (Tags(ruleBased = false, "carrot") :: associateWithLibrary(acquistionMethod).tag :: List()).toArray,
        authors = Array(),
        library = associateWithLibrary(acquistionMethod)
      )
    )

  }

  private def generateDefaultMetaData(t: Target, sample: Option[Sample]) = {
    val metaData = Array(
      MetaData(
        category = "none",
        computed = false,
        hidden = false,
        name = "retention time",
        score = null,
        unit = "s",
        url = null,
        value = t.retentionTimeInSeconds
      ),
      MetaData(
        category = "none",
        computed = false,
        hidden = false,
        name = "retention index",
        score = null,
        unit = "s",
        url = null,
        value = t.retentionIndex
      ),

      MetaData(
        category = "none",
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
        value = t.confirmed
      )
      ,
      MetaData(
        category = "none",
        computed = false,
        hidden = false,
        name = "ionization mode",
        score = null,
        unit = "s",
        url = null,
        value = if (t.ionMode.isInstanceOf[PositiveMode]) "positive" else "negative"
      )
      ,
      MetaData(
        category = "none",
        computed = false,
        hidden = false,
        name = "ms level",
        score = null,
        unit = null,
        url = null,
        value = if (t.spectrum.isDefined) s"MS${t.spectrum.get.msLevel}" else "MS1"
      )
      ,
      MetaData(
        category = "origin",
        computed = false,
        hidden = false,
        name = "fileName",
        score = null,
        unit = null,
        url = null,
        value = if (sample.isDefined) s"${sample.get.fileName}" else "Unknown"
      )

      ,
      MetaData(
        category = "origin",
        computed = false,
        hidden = false,
        name = "sampleName",
        score = null,
        unit = null,
        url = null,
        value = if (sample.isDefined) s"${sample.get.name}" else "Unknown"
      )


    )
    metaData
  }

  /**
    * associate this submitter to the uploaded spectra
    *
    * @return
    */
  private def associateWithSubmitter(): Submitter = {
    try {
      userSerivce.get(username)
    }
    catch {
      case e: Exception =>
        logger.debug(s"observed an error, using dummy user! ${e.getMessage}", e)
        Submitter(
          username,
          "none@provided.com",
          username,
          username,
          "none"
        )
    }
  }

  /**
    * in which library targets should be defined
    *
    * @return
    */
  private def associateWithLibrary(acquistionMethod: AcquisitionMethod): Library = {
    val identifier = generateLibraryIdentifier(acquistionMethod)

    Library(
      id = identifier,
      library = identifier,
      description = "generated based on carrot data processing",
      link = null,
      tag = Tags(ruleBased = false, text = identifier)
    )
  }

  /**
    * generates a library identifier based on the given method
    *
    * @param acquisitionMethod
    * @return
    */
  private def generateLibraryIdentifier(acquisitionMethod: AcquisitionMethod): String = {

    val method = acquisitionMethod.chromatographicMethod
    val instrument = method.instrument match {
      case Some(x) =>
        x
      case _ => noneSpecifiedValue
    }

    val column = method.column match {
      case Some(x) =>
        x
      case _ => noneSpecifiedValue
    }

    val mode = method.ionMode match {
      case Some(x) if x.isInstanceOf[PositiveMode] =>
        "positive"
      case Some(x) if x.isInstanceOf[NegativeMode] =>
        "negative"
      case _ =>
        noneSpecifiedValue
    }

    s"${method.name} - ${instrument} - ${column} - ${mode}"

  }


  /**
    * returns all associated acuqisiton methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = {

    val url = s"${monaRestServer}/rest/tags/library"

    val data = restTemplate.getForObject(url, classOf[Array[Tags]])

    data.filter(!_.ruleBased).map { tag: Tags =>
      val values: Array[String] = tag.text.split(" - ")


      val instrument = if (values(1) == noneSpecifiedValue || values(1) == null) None else Option(values(1))
      val column = if (values(2) == noneSpecifiedValue || values(1) == null) None else Option(values(2))
      val mode = if (values(3).toLowerCase == "positive") {
        Some(PositiveMode())
      }
      else if (values(3).toLowerCase == "negative") {
        Some(NegativeMode())
      }
      else {
        None
      }

      AcquisitionMethod(

        ChromatographicMethod(values(0), instrument, column, mode)
      )
    }.toSeq


  }

  /**
    * converts a spectrum to a target
    *
    * @param x
    * @return
    */
  private def generateTarget(x: Spectrum): AnnotationTarget = {

    val compound = x.compound.head
    val name = compound.names.head.name
    val inchi = compound.inchiKey

    val retentionTime: Option[MetaData] = x.metaData.find(p => p.name == "retention time" && p.category == "none")
    val retentionIndex: Option[MetaData] = x.metaData.find(p => p.name == "retention index" && p.category == "none")

    val uniqueMass: Option[MetaData] = x.metaData.find(p => p.name == "unique mass" && p.category == "none")
    val precursorIon: Option[MetaData] = x.metaData.find(p => p.name == "precursor m/z" && p.category == "none")
    val isRetentionIndexStandard: Option[MetaData] = x.metaData.find(p => p.name == "riStandard" && p.category == "carrot")
    val requiredForCorrection: Option[MetaData] = x.metaData.find(p => p.name == "requiredForCorrection" && p.category == "carrot")
    val confirmed: Option[MetaData] = x.metaData.find(p => p.name == "confirmed" && p.category == "carrot")
    val ionMode = x.metaData.find(p => p.name == "ionization mode" && p.category == "none")
    val msObservedLevel = x.metaData.find(p => p.name == "ms level" && p.category == "none").get.value.toString match {
      case "MS1" => 1
      case "MS2" => 2
      case "MS3" => 3
      case "MS4" => 4
      case "MS5" => 5
      case "MS6" => 6
      case "MS7" => 7
      case "MS8" => 8
      case "MS9" => 9
      case "MS10" => 10

    }

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
      /**
        * the msLevel of this spectra
        */
      override val msLevel: Short = msObservedLevel.toShort
    }

    /**
      * our defined method
      */
    MonaLibraryTarget(
      id = x.id,
      msmsSpectrum = spectrumProperties,
      name = Option(name),
      retentionIndex = retentionIndex.get.value.toString.toDouble,
      retentionTimeInSeconds = retentionTime.get.value.toString.toDouble,
      inchiKey = Option(inchi),
      precursorMass = Option(precursorIon.get.value.toString.toDouble),
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
          throw new InvalidIonModeDefinedException("the provided ion mode is not valid!")
        }

      } else {
        throw new IonModeNotDefinedException("we require you to define an ion mode!")
      },
      uniqueMass = if (uniqueMass.isDefined) Option(precursorIon.get.value.toString.toDouble) else None
    )
  }

  /**
    * adds a list of targets
    *
    * @param targets
    */
  override def add(targets: Iterable[AnnotationTarget], acquisitionMethod: AcquisitionMethod, sample: Option[Sample]): Unit = {

    logger.debug(s"adding ${targets.size} targets for method ${acquisitionMethod}")
    targets.foreach {
      t =>
        val spectrum: Option[Spectrum] = generateSpectrum(t, acquisitionMethod, sample)

        if (spectrum.isDefined) {
          val spec = spectrum.get
          monaSpectrumRestClient.add(spec)
        }
        else {
          throw new TargetGenerationNotSupportedException(s"not possible to generate a target here, due to lack of metadata. Please ensure that you provide all required information", t)
        }
    }

    updateLibraries

  }

  private def updateLibraries = {
    //let a process in the background update all the statistics
    executionService.submit(new Runnable {
      override def run(): Unit = {

        logger.debug("updating all statistics and downloads")
        monaSpectrumRestClient.regenerateStatistics
        monaSpectrumRestClient.regenerateDownloads

      }
    })
  }

  /**
    * deletes a specified target from the library
    *
    * @param t
    * @param acquisitionMethod
    */
  @CacheEvict(value = Array("monacache"), allEntries = true)
  override def delete(t: AnnotationTarget, acquisitionMethod: AcquisitionMethod): Unit = {
    t match {
      case target: Target with Idable[String] =>
        val spectrum: Option[Spectrum] = generateSpectrum(target, acquisitionMethod, None)
        this.monaSpectrumRestClient.delete(spectrum.get.id)

        updateLibraries
      case _ =>
        logger.warn(s"${t} is not of the right type! Type is ${t.getClass}")
    }
  }

  /**
    * this will update the existing target with the provided values
    *
    * @param target
    * @param acquisitionMethod
    */
  @CacheEvict(value = Array("monacache"), allEntries = true)
  override def update(target: AnnotationTarget, acquisitionMethod: AcquisitionMethod): Boolean = {
    val spectrum = generateSpectrum(target, acquisitionMethod, None).get
    this.monaSpectrumRestClient.update(spectrum, spectrum.id)
    true
  }

  @CacheEvict(value = Array("monacache"), allEntries = true)
  override def deleteLibrary(acquisitionMethod: AcquisitionMethod): Unit = {
    logger.info(s"about to delete library ${acquisitionMethod}")

    load(acquisitionMethod).foreach(t => delete(t, acquisitionMethod))
  }
}

/**
  * loads additional required beans for this to work
  */
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[MonaSpectrumRestClient], classOf[MonaLibraryAccess]))
@Profile(Array("carrot.targets.mona"))
@Import(Array(classOf[RestClientConfig]))
class MonaLibraryAccessAutoConfiguration {

  @Bean
  def submitterService: GenericRestClient[Submitter, String] = new GenericRestClient[Submitter, String]("rest/submitters")
}

/**
  * this defines a valid mona based target in the system
  *
  * @param msmsSpectrum
  * @param name
  * @param retentionIndex
  * @param inchiKey
  * @param precursorMass
  * @param confirmed
  * @param requiredForCorrection
  * @param isRetentionIndexStandard
  */
case class MonaLibraryTarget(
                              id: String,

                              /**
                                * associate msms spectrum
                                */
                              msmsSpectrum: SpectrumProperties,

                              /**
                                * a name for this spectra
                                */
                              override var name: Option[String],

                              /**
                                * retention time in seconds of this target
                                */
                              override val retentionIndex: Double,

                              override val retentionTimeInSeconds: Double,

                              /**
                                * the unique inchi key for this spectra
                                */
                              override var inchiKey: Option[String],

                              /**
                                * the mono isotopic mass of this spectra
                                */
                              override val precursorMass: Option[Double],

                              /**
                                * is this a confirmed target
                                */
                              override var confirmed: Boolean,

                              /**
                                * is this target required for a successful retention index correction
                                */
                              override var requiredForCorrection: Boolean,

                              /**
                                * is this a retention index correction standard
                                */
                              override var isRetentionIndexStandard: Boolean,

                              /**
                                * the specified ionmode for this target. By default we should always assume that it's positive
                                */
                              override val ionMode: IonMode,

                              override val uniqueMass: Option[Double]

                            )
    extends AnnotationTarget with Idable[String] {

  /**
    * required since targets expects a spectrum, while its being optional on the carrot level
    */
  override val spectrum = Option(msmsSpectrum)


}
