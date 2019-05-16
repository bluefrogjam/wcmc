package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.storage

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{GapFilledSpectra, QuantifiedSample, QuantifiedSpectra, QuantifiedTarget, Target => CTarget}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.{Annotation, Correction, Curve, Injection, Result, ResultData, TrackingData, Target => STTarget}
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
@Profile(Array("carrot.output.storage.aws"))
class StasisResultStorage[T] extends ResultStorage with Logging {

  @Autowired
  val stasis_cli: StasisClient = null

  def save(sample: QuantifiedSample[T]): ResultData = {

    //    val results = sample.spectra.map(feature => {
    val results = sample.quantifiedTargets.map(target => {
      Result(CarrotToStasisConverter.asStasisTarget(target),
        Annotation(target.retentionIndex,
          target.quantifiedValue.getOrElse(0.0) match {
            case x: Double => x.toDouble
            case _ => 0.0
          },
          replaced = target match {
            case f: GapFilledSpectra[T] => true
            case _ => false
          },

          target.precursorMass.getOrElse(0.0),
          nonCorrectedRt = target.retentionTimeInSeconds,
          massError = getTargetMassError(target, target.spectra),
          massErrorPPM = getTargetMassError(target, target.spectra, ppm = true)
        )
      )
    })

    val injections = Map(sample.name -> Injection(System.currentTimeMillis().toString,
      Correction(3,
        sample.correctedWith.name,
        sample.regressionCurve.getXCalibrationData.zip(sample.regressionCurve.getYCalibrationData)
            .map(pair => Curve(pair._1, pair._2))
      ),
      results)
    ).asJava

    val data: ResultData = ResultData(sample.name, injections)

    val response = stasis_cli.addResult(data)

    if (response.getStatusCode == HttpStatus.OK) {
      stasis_cli.addTracking(TrackingData(sample.name, "exported", sample.fileName))
      response.getBody
    } else {
      logger.warn(response.getStatusCode.getReasonPhrase)
      data
    }
  }


  def getTargetMassError(target: QuantifiedTarget[T], spectrum: Option[QuantifiedSpectra[T]], ppm: Boolean = false): Double = {
    if (spectrum.isDefined)
      if (ppm)
        spectrum.get.massAccuracyPPM.getOrElse(0.0)
      else
        spectrum.get.massAccuracy.getOrElse(0.0)
    else
      0.0
  }

  /**
    * store the given experiment
    *
    * @param experiment
    */
  override def store(experiment: Experiment, task: Task): Unit = {
    experiment.classes.foreach(x =>
      x.samples.foreach {
        case sample: QuantifiedSample[T] =>
          save(sample)
      }
    )
  }
}

object CarrotToStasisConverter {
  def asStasisTarget(target: CTarget): STTarget = {
    STTarget(target.retentionTimeInSeconds, target.name.get, target.name.get, target.accurateMass.getOrElse(0.0))
  }
}
