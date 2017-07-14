package edu.ucdavis.fiehnlab.ms.carrot.core.api.io

import java.io.FileNotFoundException

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * used to simply load samples from a local or remote storage depending on
  * implementation
  */
trait SampleLoader {

  /**
    * loads a sample as an option, so that we can evaluate it we have it or not, without an exception
    *
    * @param name
    * @return
    */
  def loadSample(name: String): Option[_ <: Sample]

  /**
    * forcefully loads the sample or throws a FileNotFoundException if it was not found
    *
    * @param name
    * @return
    */
  def getSample(name:String) : Sample = {
    val result = loadSample(name)

    if(result.isDefined){
      result.get
    }
    else{
      throw new FileNotFoundException(s"sorry the specified sample '${name}' was not found!")
    }
  }

  /**
    * gets all the specified samples
    * @param names
    * @return
    */
  def getSamples(names:Seq[String]) : Seq[_ <: Sample] = names.map(getSample)

  /**
    * loads all the specified samples
    * @param names
    * @return
    */
  def loadSamples(names:Seq[String]) : Seq[Option[_ <: Sample]] = names.map(loadSample)

  /**
    * checks if the sample exist
    *
    * @param name
    * @return
    */
  def sampleExists(name: String): Boolean
}
