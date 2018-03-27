package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSample, Sample, Target}
import org.springframework.beans.factory.annotation.Autowired

/**
  * defines a correction process
  * @param libraryAccess
  */
abstract class CorrectionProcess @Autowired()(val libraryAccess: LibraryAccess[Target]) extends AnnotationProcess[Target, Sample, CorrectedSample](libraryAccess) with LazyLogging{

}
