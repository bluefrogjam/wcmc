package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model

case class ResultData(
                         sample: String,
                         correction: Correction,
                         injections: java.util.Map[String, Seq[Result]]
                     )

case class Curve(
                    x: Double,
                    y: Double
                )

case class Correction(
                         polynomial: Double,
                         sampleUsed: String,
                         curve: Array[Curve]
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
