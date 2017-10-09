package edu.ucdavis.fiehnlab.ms.carrot.core.db.mongo

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, QuantifiedSample, QuantifiedTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.exception.ResultException
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.{ResultStorage, Task}
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 9/14/17.
  *
  * Stores the data processing result in a MongoDB collection
  * for later usage and analysis
  */
@Component
@Profile(Array("carrot.store.result.mongo"))
class MongoResultHandler extends ResultStorage {
  /**
    * store the given experiment in the MongoDB repository
    * for later analysis and data integration
    *
    * @param experiment
    */
  override def store(experiment: Experiment, task: Task): Unit = {
    experiment.classes.foreach { c =>
      c.samples.collect {
        case s: QuantifiedSample[Any] =>
          serialize(s)
        case x: Any =>
          throw new ResultException(s"this type of data is not supported: ${x.toString}", x)
      }
    }
  }

  /**
    * serializes the sample to a mongodb compatible format
    *
    * @param sample
    */
  def serialize(sample: QuantifiedSample[Any]) = {

    sample.quantifiedTargets.foreach { target: QuantifiedTarget[Any] =>
      if (target.quantifiedValue.isDefined) {
        val res = MongoResult(
          sampleName = sample.name,
          fileName = sample.fileName,
          MongoAnnotation(
            targetName = target.name.get,
            targetRetentionIndex = target.retentionIndex,
            annotationRetentionIndex = target.spectra.get.retentionIndex,
            annotationValue = target.quantifiedValue.get,
            scanNumber = target.spectra.get.scanNumber,
            annotationMZ = target.spectra.get.massOfDetectedFeature.getOrElse(Ion(0, 0)).mass,
            targetMZ = target.precursorMass.getOrElse(0.0),
            diagnostics = MongoAnnotationDiagnostics(
              massAccuracy = target.spectra.get.massAccuracy.getOrElse(0.0),
              massAccuracyPPM = target.spectra.get.massAccuracyPPM.getOrElse(0.0),
              retentionIndeexDistance = target.spectra.get.retentionIndexDistance.getOrElse(0)
            )
          )

        )
      }
    }
  }

}

@Document(collection="result")
case class MongoResult(
                        sampleName: String,
                        fileName: String,
                        annotation: MongoAnnotation

                      )

/**
  * contains all the relevant annotation information
  *
  * @param targetName
  * @param targetRetentionIndex
  * @param annotationRetentionIndex
  * @param annotationValue
  */
case class MongoAnnotation(
                            targetName: String,
                            scanNumber: Int,
                            targetMZ: Double,
                            targetRetentionIndex: Double,
                            annotationRetentionIndex: Double,
                            annotationValue: Any,
                            annotationMZ: Double,
                            diagnostics: MongoAnnotationDiagnostics
                          )

/**
  * computed helpful diagnostics information
  */
case class MongoAnnotationDiagnostics(
                                       massAccuracy: Double,
                                       massAccuracyPPM: Double,
                                       retentionIndeexDistance: Double
                                     )
