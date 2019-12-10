package edu.ucdavis.fiehnlab.ms.carrot.core.db.dynamo

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.ReadonlyLibrary
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import org.springframework.context.annotation._

class DynamoLibraryAccess extends ReadonlyLibrary[AnnotationTarget] {
  /**
   * loads all the spectra from the library
   * applicable for the given acquistion method
   *
   * @return
   */
  override def load(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean]): Iterable[AnnotationTarget] = ???

  /**
   * this will update the existing target with the provided values in the selected method
   *
   * @param target
   * @param acquisitionMethod
   */
  override def update(target: AnnotationTarget, acquisitionMethod: AcquisitionMethod): Boolean = ???

  /**
    * deletes a specified target from the acquisition method
    *
    * @param target
    * @param acquisitionMethod
    */
  override def delete(target: AnnotationTarget, acquisitionMethod: AcquisitionMethod): Unit = ???

  /**
    * returns all associated acquisition methods for this library
    *
    * @return
    */
  override def libraries: Seq[AcquisitionMethod] = ???

  /**
   * deletes the specified acquisition method from the list
   *
   * @param acquisitionMethod
   */
  override def deleteLibrary(acquisitionMethod: AcquisitionMethod, confirmed: Option[Boolean]): Unit = ???
}

/**
  * loads additional required beans for this to work
  */
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[DynamoLibraryAccess]))
@Profile(Array("carrot.targets.aws"))
class DynamoLibraryAccessAutoConfiguration {
}
