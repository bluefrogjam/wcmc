package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.listeners

import java.io.{OutputStream, PrintStream}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * Created by wohlg_000 on 4/27/2016.
  */
class SplashFileGenerator(outputStream: OutputStream) extends SampleScannerListener {

  val printStream: PrintStream = new PrintStream(outputStream)

  /**
    * a valid sample, which can be processed by the system
    *
    * @param sample
    */
  override def found(sample: Sample): Unit = {
    sample.spectra collect {
      case spectra: MSSpectra =>
        printStream.println(s"${sample.fileName},${spectra.scanNumber}, ${spectra.splash}, ${spectra.spectraString}")
      case _ =>
    }
    printStream.flush()
  }
}
