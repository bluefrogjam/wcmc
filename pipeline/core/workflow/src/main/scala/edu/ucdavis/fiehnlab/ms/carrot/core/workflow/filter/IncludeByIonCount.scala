package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile(Array("carrot.filters.ioncount"))
@Component
class IncludeByIonCount @Autowired()(@Value("${carrot.filters.minIonCount}") val minIonCount: Int) extends Filter[Target] {

  protected override def doInclude(target: Target, applicationContext: ApplicationContext): Boolean = {
    target.spectrum match {
      case Some(x) => x.ions.size >= minIonCount
      case None => false
    }
  }

}
