package edu.ucdavis.fiehnlab.ms.carrot.core.api.acquisition

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

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
