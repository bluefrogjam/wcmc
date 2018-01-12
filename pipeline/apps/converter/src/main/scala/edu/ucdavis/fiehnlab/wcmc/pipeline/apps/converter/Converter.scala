package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.converter

import java.io.{BufferedOutputStream, File, FileInputStream, FileOutputStream}
import java.net.URL

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.loader.{DelegatingResourceLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.{ApplicationArguments, ApplicationRunner, SpringApplication}
import org.springframework.context.annotation.{Bean, Primary}

import scala.collection.JavaConverters._

/**
  * converts a given file to mzXML
  */
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class Converter extends ApplicationRunner with LazyLogging {

  @Autowired
  val dataForm: DataFormerClient = null

  /**
    * access to our home directory
    * @return
    */
  @Bean
  def homeDirectoyLoader:ResourceLoader =  new RecursiveDirectoryResourceLoader(new File(System.getProperty("user.home")))

  /**
    * access to our local directoy
    * @return
    */
  @Bean
  def localDirectoyLoader:ResourceLoader =  new RecursiveDirectoryResourceLoader(new File("./"))

  /**
    * combine all loaders together, including what is provided by autoconfiguration
    * @return
    */
  @Bean
  @Primary
  def resourceLoader():ResourceLoader = new DelegatingResourceLoader()

  override def run(applicationArguments: ApplicationArguments) = {


    if (applicationArguments.containsOption("file")) {
      applicationArguments.getOptionValues("file").asScala.foreach { f: String =>
        logger.info(s"converting: ${f}")
        val out: Option[File] = dataForm.convert(f, "mzXML")

        out match {
          case Some(x) =>
            logger.info(s"converted ${x}")

            val out = new BufferedOutputStream(new FileOutputStream(s"${x.getName.substring(0,x.getName.indexOf("."))}.mzXML"))
            try {
              IOUtils.copy(new FileInputStream(x), out)
              out.flush()

            } finally {
              out.close()
            }
          case _ => logger.error(s"something went wrong with: ${f}")
        }
      }
    }

    else {
      System.err.println("\n\nplease provide --file=file arguments for your conversion\n\n")
    }
  }
}

object Converter extends App {
  val app = new SpringApplication(classOf[Converter])
  app.setWebEnvironment(false)
  val context = app.run(args: _*)
}