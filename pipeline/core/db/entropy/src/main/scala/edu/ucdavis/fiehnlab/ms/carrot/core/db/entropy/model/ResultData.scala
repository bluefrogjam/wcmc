package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.model

case class ResultData(
                         sample: String,
                         time: Double,
                         correction: Correction,
                         results: List[Result]
                     )

case class Curve(
                    x: Double,
                    y: Double
                )

case class Correction(
                         polynomial: Double,
                         sampleUsed: String,
                         curve: List[Curve]
                     )

case class Target(
                     retentionIndex: Double,
                     name: String,
                     id: String,
                     mass: Double
                 )

case class Annotation(
                         retentionIndex: Double,
                         intensity: Double,
                         replaced: Boolean,
                         mass: Double
                     )

case class Result(
                     target: Target,
                     annotation: Annotation
                 )

case class ResultResponse(
                             statusCode: Int,
                             body: ResultData
                         )
