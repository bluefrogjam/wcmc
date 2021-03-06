package edu.ucdavis.fiehnlab.ms.carrot.core.api.io

import java.io.{BufferedOutputStream, BufferedWriter, OutputStream, OutputStreamWriter}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Sample}

/**
  * Created by wohlg_000 on 4/21/2016.
  */
abstract class Writer[T] {

  /**
    * writes the header if supported
    *
    * @param outputStream
    */
  def writeHeader(outputStream: OutputStream) = {}

  /**
    * writes the given sample to the output stream
    *
    * @param outputStream
    * @param data
    */
  def write(outputStream: OutputStream, data: T)

  /**
    * writes the footer, if supported
    *
    * @param outputStream
    */
  def writeFooter(outputStream: OutputStream) = {}

  /**
    * the writers extension
    * @return
    */
  def extension:String
}

