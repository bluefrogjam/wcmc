package edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j

import java.io.File

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 6/16/17.
  *
  * connects to the MSDial rest server
  */
@Component
class MSDialRestProcessor {

  @Value("${wcms.api.rest.msdialrest4j.host:128.120.143.101}")
  val host:String

  @Value("${wcms.api.rest.msdialrest4j.port:80}")
  val port:Int

  /**
    * processes the input file and
    * @param input
    * @return
    */
  def process(input:File) : File = {

    //if directory and ends with .d zip file

    //upload

    //convert

    //schedule

    //check status

    //if finished download to temp

    //return handle
  }

  /**
    * uploads a given file to the server
    * @param file
    * @return
    */
  protected def upload(file:File):String = ???

  /**
    * was this process finished
    * @param id
    * @return
    */
  protected def isFinished(id:String):Boolean = ???

  /**
    * convert the given file, if it was a .d file or so to an abf file
    * @param id
    */
  protected def convert(id:String):String = ???

  /**
    * downloads the remote file to the local harddrive
    * @param id
    * @return
    */
  protected def download(id:String) : File = ???
}
