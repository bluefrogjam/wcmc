package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, Sample, Target}
import org.springframework.beans.factory.annotation.Autowired

abstract class AnnotateSampleProcess  @Autowired()(val libraryAccess: LibraryAccess[Target]) extends AnnotationProcess[Target, CorrectedSample, AnnotatedSample](libraryAccess) with LazyLogging{

}
