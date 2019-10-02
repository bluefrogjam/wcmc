package edu.ucdavis.fiehnlab.wcmc.utilities.translate.api

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target

trait LibraryTranslator {
  def convert(from: LibraryAccess[_ <: Target], to: LibraryAccess[_ <: Target], method: AcquisitionMethod)

  def convertToFile(from: LibraryAccess[_ <: Target], method: AcquisitionMethod)
}
