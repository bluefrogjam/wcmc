package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model

import java.util.Date

/**
  * Object holding all the ionformation about a sample, from acquisition to all the differen runs it went through
  *
  * @param sample     name of the sample without extension
  * @param injections list of runs for this sample
  */
case class ResultData(
                         sample: String,
                         injections: java.util.Map[String, Injection]
                     )

/**
  * Information about a sample run
  *
  * @param logid      TBD
  * @param correction Retention Index correction information
  * @param results    list of annotations found
  */
case class Injection(
                        logid: String,
                        correction: Correction,
                        results: Seq[Result]
                    )

/**
  * Corrected data point
  *
  * @param x original ri value
  * @param y corrected ri value
  */
case class Curve(
                    x: Double,
                    y: Double
                )

/**
  * Retention Index Correction indformation
  *
  * @param polynomial order of polinomial used (default is 3)
  * @param sampleUsed sample name used for correction
  * @param curve      list of corrected points
  */
case class Correction(
                         polynomial: Double,
                         sampleUsed: String,
                         curve: Seq[Curve]
                     )

/**
  * Target used in this sample's annotation
  *
  * @param retentionTimeInSeconds
  * @param name
  * @param id
  * @param mass
  */
case class Target(
                     retentionTimeInSeconds: Double,
                     name: String,
                     id: String,
                     mass: Double,
                     index: Int
                 ) {
  override def toString: String = {
    val sb = new StringBuilder()
    sb.append("Target(")
        .append(index).append(",")
        .append(id).append(",")
        .append(name).append(",")
        .append(retentionTimeInSeconds).append(",")
        .append(mass).append(",")
        .append(")").toString()
  }
}

/**
  * Feature from this sample that has been anotated with a target
  *
  * @param retentionIndex
  * @param intensity
  * @param replaced
  * @param mass
  */
case class Annotation(
                         retentionIndex: Double,
                         intensity: Double,
                         replaced: Boolean,
                         mass: Double,
                         nonCorrectedRt: Double = 0,
                         massError: Double = 0,
                         massErrorPPM: Double = 0,
                         other: Option[Map[String, Any]] = None
                     ) {
  def rtDistance: Double = Math.abs(nonCorrectedRt - retentionIndex)
}

/**
  *
  * @param target
  * @param annotation
  */
case class Result(
                     target: Target,
                     annotation: Annotation
                 )

case class ResultResponse(
                             id: String,
                             sample: String,
                             time: Date,
                             injections: java.util.Map[String, Seq[Injection]]
                         )
