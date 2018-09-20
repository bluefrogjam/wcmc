package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.storage

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Target => CTarget}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.{Annotation, Correction, Curve, Injection, Result, ResultData, TrackingData, Target => STTarget}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
@Profile(Array("carrot.output.storage.aws"))
class StasisResultStorage[T] extends ResultStorage with LazyLogging {

  @Autowired
  val stasis_cli: StasisClient = null

  def save(sample: QuantifiedSample[T]): ResultData = {
    logger.info(s"Storing '${sample.name}' on AWS")

    val results = sample.spectra.map(feature => { // of type $anon$5 or anon$7
      //      logger.info(s"type: ${feature.getClass}")
      //      logger.info(s"\treplaced? ${feature.getClass.getGenericInterfaces.contains(classOf[GapFilledTarget[T]]) ||
      //          feature.getClass.getGenericInterfaces.contains(classOf[ZeroreplacedTarget])}")
      Result(CarrotToStasisConverter.asStasisTarget(feature.target),
        Annotation(feature.retentionIndex,
          feature.quantifiedValue.get match {
            case x: Double => x.toDouble
            case _ => 0.0
          },
          replaced = feature.getClass.getTypeName.contains("ZeroreplacedTarget") || feature.getClass.getTypeName.contains("GapFilledTarget"),
          feature.accurateMass.getOrElse(0.0),
          nonCorrectedRt = Some(feature.retentionTimeInSeconds),
          feature.massAccuracy,
          feature.massAccuracyPPM,
          feature.retentionIndexDistance
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
    logger.trace(s"stasis response: ${response}")

    if (response.getStatusCode == HttpStatus.OK) {
      stasis_cli.addTracking(TrackingData(sample.name, "exported", sample.fileName))
      response.getBody
    } else {
      logger.info(response.getStatusCode.getReasonPhrase)
      data
    }
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
    STTarget(target.retentionIndex, target.name.get, target.name.get, target.accurateMass.getOrElse(0.0))
  }
}
