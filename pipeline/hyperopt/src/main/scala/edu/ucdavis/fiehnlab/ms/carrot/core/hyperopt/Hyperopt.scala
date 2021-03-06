package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt

import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.optimizer.grid.{GridSearchResult, SparkGridSearch}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.ConfigYamlProtocol._
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.callbacks.{CallbackHandler, SQSCallbackHandler}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions.{PeakHeightRSDAnnotationLossFunction, PeakHeightRSDCorrectionLossFunction}
import net.jcazevedo.moultingyaml._
import org.apache.spark.{SparkConf, SparkContext}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean

import scala.io.Source

/**
  * utility class to run a hyperoptimization
  */
class HyperoptRunner(callbacks: Seq[CallbackHandler] = Seq.empty) {

  /**
    * takes the given config and runs all the defines stages for us
    *
    * @param config
    */
  def run(name: String, config: Config): Unit = {

    val sc = new SparkContext(new SparkConf().setAppName(name))
    val optimizer = new SparkGridSearch[Point, Double](sc)

    try {

      //compute the possible correction parameters for us
      val bestCorrectionPoint: Option[Point] = config.hyperopt.stages.correction match {
        case Some(x) =>
          val correctionResult = runCorrection(config, optimizer)
          print(correctionResult)
          val bestCorrectionSettings = correctionResult.bestPoint

          print(bestCorrectionSettings)
          Some(bestCorrectionSettings)
        case None => None
      }

      //compute best annotation properties for us
      val bestAnnotationPoint: Option[Point] = config.hyperopt.stages.annotation match {
        case Some(x) =>
          val annotationResult = runAnnotation(config, optimizer, bestCorrectionPoint)
          print(annotationResult)
          Some(annotationResult.bestPoint)
        case None => None
      }
    }
    finally {
      sc.stop()
    }
  }


  /**
    * evaluates the correction settings for us and reports the final score
    *
    * @param config
    * @param optimizer
    * @return
    */
  protected def runCorrection(config: Config, optimizer: SparkGridSearch[Point, Double]): GridSearchResult[Point, Double] = {

    val correctionObjective = new CorrectionObjective(
      config = classOf[HyperoptConfiguration],
      profiles = config.hyperopt.profiles.toArray,
      lossFunction = new PeakHeightRSDCorrectionLossFunction(),
      samples = config.hyperopt.samples,
      methodName = config.hyperopt.method,
      callbacks
    )

    correctionObjective.warmCaches()

    optimizer.minimize(correctionObjective, correctionObjective.getSpace(config))
  }


  /**
    * evaluates all our annotations parameters and provides us with the best combination of them
    *
    * @param config
    * @param optimizer
    * @param bestCorrectionPoint
    * @return
    */
  def runAnnotation(config: Config, optimizer: SparkGridSearch[Point, Double], bestCorrectionPoint: Option[Point]): GridSearchResult[Point, Double] = {
    val annotationObjective = new AnnotationObjective(
      config = classOf[HyperoptConfiguration],
      profiles = config.hyperopt.profiles.toArray,
      lossFunction = new PeakHeightRSDAnnotationLossFunction(),
      samples = config.hyperopt.samples,
      methodName = config.hyperopt.method,
      correctionBestPoint = bestCorrectionPoint,
      callbacks = callbacks
    )

    annotationObjective.warmCaches()
    optimizer.minimize(annotationObjective, annotationObjective.getSpace(config))
  }


}

object HyperoptRunner {
  def main(args: Array[String]): Unit = {

    val data = Source.fromFile(args(0)).getLines.mkString("\n")
    val yaml = data.parseYaml
    val config = yaml.convertTo[Config]
    val opt = new HyperoptRunner(callbacks = Seq(new SQSCallbackHandler("CarrotHyperoptQueue-prod", config)))
    opt.run(args(0), config)

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration], classOf[MongoDataAutoConfiguration], classOf[MongoAutoConfiguration], classOf[QuartzAutoConfiguration]))
@EnableCaching
class HyperoptConfiguration {
  @Bean
  def annotationLibrary(@Autowired(required = false) targets: java.util.List[LibraryAccess[AnnotationTarget]]): DelegateLibraryAccess[AnnotationTarget] = {
    if (targets == null) {
      new DelegateLibraryAccess[AnnotationTarget](new java.util.ArrayList())
    }
    else {
      new DelegateLibraryAccess[AnnotationTarget](targets)
    }
  }

  @Bean
  def correctionLibrary(targets: java.util.List[LibraryAccess[CorrectionTarget]]): DelegateLibraryAccess[CorrectionTarget] = new DelegateLibraryAccess[CorrectionTarget](targets)

}
