package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile(Array("carrot.filters.ioncount"))
@Component
class IonCountFilter @Autowired()(@Value("${carrot.filters.minIonCount:3}") val minIonCount: Int = 0) extends Filter[Target] with Logging {
  logger.info(s"Creating filter ${this.getClass.getSimpleName} with minimum ion count: ${minIonCount}")

  protected override def doInclude(target: Target, applicationContext: ApplicationContext): Boolean = {
    target.spectrum match {
      case Some(x) => x.ions.size >= minIonCount
      case None => false
    }
  }

}
