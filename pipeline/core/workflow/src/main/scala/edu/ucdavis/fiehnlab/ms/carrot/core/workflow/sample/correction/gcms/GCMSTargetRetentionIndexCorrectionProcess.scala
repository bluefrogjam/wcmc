package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Regression
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra, SimilaritySupport}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSample, Sample, Target, TargetAnnotation}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter.{IncludeBySimilarity, IncludesBasePeakSpectra, IncludesByPeakHeight}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
@Profile(Array("carrot.gcms"))
class GCMSTargetRetentionIndexCorrectionProcess @Autowired()(libraryAccess: LibraryAccess[Target], val config: GCMSCorrectionLibraryProperties) extends CorrectionProcess(libraryAccess) with LazyLogging {
  override val regression: Regression = null

  /**
    * abstract method to acutall find out targets
    * required for the correction
    * of this sample
    *
    * @param input
    * @param targets
    * @return
    */
  override protected def findCorrectionTargets(input: Sample, targets: Iterable[Target], method: AcquisitionMethod): Seq[TargetAnnotation[Target, Feature]] = {

    assert(method.chromatographicMethod.isDefined)
    logger.info(s"defined targets: ${targets.size}")

    val configuration = config.config.asScala.find(_.name == method.chromatographicMethod.get.name)

    configuration match {

      case Some(config) =>

        val includeByBasePeakFilter = new IncludesBasePeakSpectra(config.allowedBasePeaks.asScala, config.massAccuracy)
        val includeByIntensityFilter = new IncludesByPeakHeight(config.allowedBasePeaks.asScala, config.massAccuracy, config.minimumPeakIntensity)

        //filters all our spectra
        val msSpectra = input.spectra.collect {
          case x: MSSpectra => x
        }

        val spectraWithCorrectBasePeak = msSpectra.filter(includeByBasePeakFilter.include)
        val spectraWithCorrectionIntensity = spectraWithCorrectBasePeak.filter(includeByIntensityFilter.include)

        //find potential targets
        val potentialMatches = targets.collect {
          case target: GCMSCorrectionTarget =>
            //check for similarity
            val similarity = new IncludeBySimilarity(target, target.config.minSimilarity)

            val similarityMatches = spectraWithCorrectionIntensity.collect{
              case x:SimilaritySupport => x
            }.filter(similarity.include)

            similarityMatches
          //check for qualifier
        }


        logger.info(s"matches: ${potentialMatches}")

        Seq.empty
      case None =>
        Seq.empty
    }


  }
}
