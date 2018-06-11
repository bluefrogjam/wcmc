package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model

import java.util.Date

case class ResultData(
                         sample: String,
                         injections: java.util.Map[String, Injection]
                     )

case class Injection(
                        logid: String,
                        correction: Correction,
                        results: Array[Result]
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
                             id: String,
                             sample: String,
                             time: Date,
                             injections: java.util.Map[String, Array[Injection]]
                         )
