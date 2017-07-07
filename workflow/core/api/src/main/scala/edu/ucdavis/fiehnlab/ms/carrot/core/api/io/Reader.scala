package edu.ucdavis.fiehnlab.ms.carrot.core.api.io

import java.io.InputStream

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * Created by wohlg_000 on 4/21/2016.
  */
abstract class Reader[T] {

  /**
    * reads a new sample
    *
    * @param inputStream
    * @return
    */
  def read(inputStream: InputStream): T
}
