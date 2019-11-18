package edu.ucdavis.fiehnlab.loader.storage

import java.io.File
import java.nio.file.{Files, StandardCopyOption}

import edu.ucdavis.fiehnlab.loader.ResourceStorage
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.beans.BeanProperty

@ConfigurationProperties(prefix = "carrot.resource.store.local")
@Component
class FileStorageProperties {
  @BeanProperty
  var directory: String = "local_storage"
}

@Component
@Profile(Array("carrot.resource.store.local"))
class FileStorage @Autowired()(properties: FileStorageProperties) extends ResourceStorage with Logging {
  /**
    * store the given file
    *
    * @param file
    */
  override def store(file: File): Unit = {
    Files.createDirectories(new File(properties.directory).toPath)

    val out = new File(new File(properties.directory), file.getName)

    logger.info(s"storing ${file.getAbsolutePath} at ${out.getAbsolutePath} ")
    Files.copy(file.toPath, out.toPath, StandardCopyOption.REPLACE_EXISTING)
  }

  /**
    * deletes the given file from the storage
    *
    * @param name
    */
  override def delete(name: String): Unit = {
    Files.delete(new File(properties.directory, name).toPath)
  }

  /**
   * checks if a file exists
   */
  override def exists(filename: String): Boolean = {
    new File(properties.directory).exists()
  }

  /**
   * returns the storage destination
   */
  override def getDestination: String = properties.directory
}
