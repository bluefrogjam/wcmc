package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{GapFilledTarget, Ion, QuantifiedSample, Target => CTarget}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.{Annotation, Correction, Curve, Injection, Result, ResultData, Target => STTarget}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
@Profile(Array("carrot.output.writer.aws"))
class StasisWriter[T] extends LazyLogging {

  @Autowired
  val stasis_cli: StasisClient = null

  def save(sample: QuantifiedSample[T]): ResultData = {
    print(sample)

    val results = sample.spectra.map(feature =>
      Result(CarrotToStasisConverter.asStasisTarget(feature.target),
        Annotation(feature.retentionIndex,
          feature.quantifiedValue.get match {
            case x: Double => x.toDouble
            case _ => 0.0
          },
          replaced = feature.isInstanceOf[GapFilledTarget[T]],
          feature.massOfDetectedFeature.getOrElse(Ion(0.0, 0.0f)).mass
        )
      )
    ).toArray


    val injections = Map(sample.name -> Injection("fakelogid",
      Correction(3, sample.correctedWith.name,
      sample.regressionCurve.getXCalibrationData.zip(sample.regressionCurve.getYCalibrationData)
          .map(pair => Curve(pair._1, pair._2))),
      results)
    ).asJava

    val data: ResultData = ResultData(sample.name, injections)

    stasis_cli.addResult(data)
    data
  }
}

object CarrotToStasisConverter {
  def asStasisTarget(target: CTarget): STTarget = {
    STTarget(target.retentionIndex, target.name.get, target.name.get, target.accurateMass.getOrElse(0.0))
  }
}
