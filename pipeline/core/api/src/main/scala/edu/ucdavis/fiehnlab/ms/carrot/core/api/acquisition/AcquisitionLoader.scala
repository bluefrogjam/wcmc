package edu.ucdavis.fiehnlab.ms.carrot.core.api.acquisition

import java.util

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scala.collection.JavaConverters._
/**
  * Created by wohlgemuth on 8/15/17.
  *
  * provides us with access about metadata for the given sample. This is required for the LibraryAccess handling and filtering of data, while keeping unimportant information out of the actual sample
  * objects to save memory at runtime
  */
trait AcquisitionLoader {

  /**
    * loads the related acquition method for the specified sample
    * whihc should provide you with all relevant metadata
    *
    * @param sample
    * @return
    */
  def load(sample: Sample): Option[AcquisitionMethod]
}

/**
  * delegating default loader, which keeps track of all loaders in the system
  */
@Component
class DelegatingAcquisitionLoader extends AcquisitionLoader {

  @Autowired(required = false)
  val loaders: java.util.List[AcquisitionLoader] = new util.ArrayList[AcquisitionLoader]()

  /**
    * loads the related acquition method for the specified sample
    * whihc should provide you with all relevant metadata
    *
    * @param sample
    * @return
    */
  override def load(sample: Sample): Option[AcquisitionMethod] = {

    for (loader: AcquisitionLoader <- loaders.asScala) {
      val result = loader.load(sample)

      if (result.isDefined) {
        return result
      }
    }

    Some(new AcquisitionMethod(None))
  }
}