package edu.ucdavis.fiehnlab.wcmc.server.rdmonitor.impl

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.server.rdmonitor.FileMessage
import edu.ucdavis.fiehnlab.wcmc.server.rdmonitor.api.FileEventListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * handles NewFileEvents by converting the file to ABF and mzml
  */
@Component
@Profile(Array("!test"))
class ConvertingFileEventListener extends FileEventListener with LazyLogging {

  def recieveMessage(message: FileMessage): Unit = {
    logger.info(s"Found new file: ${message.name}\t-\t${message.timestamp}")
  }
}
