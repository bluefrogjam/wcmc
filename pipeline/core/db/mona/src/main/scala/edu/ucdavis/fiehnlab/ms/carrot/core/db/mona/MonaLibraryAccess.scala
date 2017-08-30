package edu.ucdavis.fiehnlab.ms.carrot.core.db.mona

import java.util.Date
import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.ms.carrot.core.api.exception.TargetGenerationNotSupportedException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import org.springframework.beans.factory.annotation.{Autowired, Qualifier, Value}
import org.springframework.context.annotation._
import org.springframework.stereotype.Component

import scala.collection.mutable

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
  def query(acquistionMethod: AcquisitionMethod): String = ""

  /**
    * loads all the spectra from the library
    *
    * @return
    */
  override def load(acquistionMethod: AcquisitionMethod): Iterable[Target] = monaSpectrumRestClient.list(query = if (query(acquistionMethod) != "") Option(query(acquistionMethod)) else None).map { x => generateTarget(x) }

  def generateAcquisitonInfo(data: Array[MetaData], acquistionMethod: AcquisitionMethod): Array[MetaData] = {

    val buffer: mutable.Buffer[MetaData] = data.toBuffer

    acquistionMethod.chromatographicMethod match {
      case None =>
      case Some(method) =>
        method.column match {
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
        method.instrument match {
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
            value = method.name
          )
    }

    buffer.toArray
  }

  /**
    * converts the given target to a valid mona spectrum for uploads
    *
    * @param t
    * @return
    */
  def generateSpectrum(t: Target, acquistionMethod: AcquisitionMethod): Option[Spectrum] = {
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

    val metaData: Array[MetaData] = generateAcquisitonInfo(generateDefaultMetaData(t), acquistionMethod)

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
        spectrum = if (t.spectrum.isDefined) {
          t.spectrum.get.spectraString
        }
        else {
          logger.warn(s"has no spectra associated, using pre cursor mass as simulated spectra: ${t}")
          s"${t.precursorMass.get}:100"
        },
        splash = null,
        submitter = associateWithSubmitter(),
        tags = Array(Tags(ruleBased = false, "carrot")),
        authors = Array(),
        library = associateWithLibrary()
      )
    )

  }

  private def generateDefaultMetaData(t: Target) = {
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


    )
    metaData
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

    val retentionTime: Option[MetaData] = x.metaData.find(p => p.name == "retention time" && p.category == "none")
    val retentionIndex: Option[MetaData] = x.metaData.find(p => p.name == "retention index" && p.category == "none")

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
      spectrumProperties,
      name = Option(name),
      retentionIndex = retentionIndex.get.value.asInstanceOf[Double],
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
  override def add(targets: Iterable[Target], acquisitionMethod: AcquisitionMethod): Unit = {
    monaSpectrumRestClient.login(username, password)

    targets.foreach {
      t =>
        val spectrum: Option[Spectrum] = generateSpectrum(t, acquisitionMethod)

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
  * @param retentionIndex
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
                              override val retentionIndex: Double,

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
