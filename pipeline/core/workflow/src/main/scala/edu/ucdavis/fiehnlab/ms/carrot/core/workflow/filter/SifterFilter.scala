package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics.JSONTargetLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Similarity
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSSpectra, SimilaritySupport, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.gcms.GCMSAnnotationLibraryProperties
import org.springframework.context.ApplicationContext

import scala.collection.JavaConverters._

/**
  * evaluates if the given spectra matches one of the properties
  *
  * @param phaseToLog
  * @param properties
  * @param target
  */
class SifterFilter(override val phaseToLog: String, val properties: GCMSAnnotationLibraryProperties, val target: Target) extends Filter[MSSpectra] with JSONTargetLogging {
  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map("properties" -> properties)

  /**
    *
    * @param spectra
    * @param applicationContext
    * @return
    */
  override protected def doIncludeWithDetails(spectra: MSSpectra, applicationContext: ApplicationContext): (Boolean, Any) = {

    logger.info(s"${spectra.retentionTimeInSeconds} evaluating purity: ${spectra.purity} and signal noise: ${spectra.signalNoise}")

    (
      properties.filter.asScala.exists { filterConfig =>
        filterConfig.matching.asScala.exists { config =>
          logger.info(s"current config: ${config}")
          if (spectra.purity.isDefined && spectra.purity.get > config.minPurity && spectra.purity.get < config.maxPurity) {
            if (spectra.signalNoise.isDefined && spectra.signalNoise.get > config.minSignalNoise && spectra.signalNoise.get < config.maxSignalNoise) {
              val sim: SimilaritySupport = spectra match {
                case x: MSSpectra with SimilaritySupport => x
                case y => new SimilaritySupport {
                  /**
                    * associated spectrum propties if applicable
                    */
                  override val spectrum: Option[SpectrumProperties] = y.associatedScan
                }
              }

              val similarity = Similarity.compute(sim, target)

              similarity > config.minSimilarity && similarity < config.maxSimilarity
            }
            else {
              false
            }

          }
          else {
            false
          }
        }
      },
      "no details available"
    )
  }

  /**
    * which target we require to log
    */
  override protected val targetToLog: Target = target
}
