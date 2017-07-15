package edu.ucdavis.fiehnlab.ms.carrot.apps.runner

import java.io.{File, FileInputStream}
import java.nio.file.StandardCopyOption._
import java.nio.file.{Files, Paths}

import scala.beans.BeanProperty

/**
  * Created by wohlgemuth on 7/12/16.
  */
//@Configuration
class SimpleTargetRunnerConfig {
  /**
    * our targets to be loaded for the retention index correction
    *
    * @return
    */
  @Bean
  def retentionIndexTargets(workflowProperties: WorkflowRunnerProperties): LibraryAccess[RetentionIndexTarget] = new TxtStreamLibraryAccess[RetentionIndexTarget](workflowProperties.correction, workflowProperties.separator)

  /**
    * our targets to be identified
    *
    * @return
    */
  @Bean
  def targets(workflowProperties: WorkflowRunnerProperties): LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](workflowProperties.library, workflowProperties.separator)

  /**
    * how do we want to quantify our result
    *
    * @param properties
    * @param libraryAccess
    * @param quantificationPostProcessing
    * @return
    */
  @Bean(name = Array("quantification"))
  def quantification(properties: WorkflowProperties, libraryAccess: LibraryAccess[Target], quantificationPostProcessing: List[PostProcessing[Double]]): QuantifyByHeightProcess = new QuantifyByHeightProcess(libraryAccess, properties, quantificationPostProcessing)

  /**
    * used for zero replacement
    *
    * @param properties
    * @return
    */
  @Bean
  def zeroReplacement(properties: WorkflowProperties): SimpleZeroReplacement = {
    new SimpleZeroReplacement(properties)
  }


  @Bean
  def quantificationPostProcessing( zeroReplacement: SimpleZeroReplacement, workflowRunnerProperties:WorkflowRunnerProperties): List[PostProcessing[Double]] = {
    if(workflowRunnerProperties.enableReplacement){
      zeroReplacement :: List()
    }
    else{
      List()
    }
  }

  /**
    * defines our actual workflow
    *
    * @param properties
    * @param writer
    * @param reader
    * @return
    */
  @Bean
  def workflowPositiveMode(properties: WorkflowProperties, writer: Writer[Sample], reader: Reader[Experiment]): LCMSPositiveModeTargetWorkflow[Double] = {
    new LCMSPositiveModeTargetWorkflow(properties, writer, reader)
  }

  /**
    * writes our output file as quantified sample txt file
    *
    * @return
    */
  @Bean
  def writer: QuantifiedSampleTxtWriter[Double] = {
    new QuantifiedSampleTxtWriter[Double]
  }

  /**
    * utilized to read our experiment definition
    *
    * @param loader
    * @param experimentTXTReaderProperties
    * @return
    */
  @Bean
  def reader(loader: SampleLoader, experimentTXTReaderProperties: ExperimentReaderTxTProperties): Reader[Experiment] = {
    new ExperimentTXTReader(loader, experimentTXTReaderProperties)
  }

}

//@Component
//@ConfigurationProperties(prefix = "input")
class WorkflowRunnerProperties {

  @Valid
  @NotNull(message = "please provide a file containing your targets for the parameter --input.library=<FILE>")
  @BeanProperty
  var library: File = _
  @Valid
  @NotNull(message = "please provide a file containing your targets for the parameter --input.correction=<FILE>")
  @BeanProperty
  var correction: File = _

  @Valid
  @NotNull(message = "please provide a file defining your output --input.result=<FILE>")
  @BeanProperty
  var result: File = _
  @Valid
  @NotNull(message = "please provide a file containing your experiment definition for the parameter --input.experiment=<FILE>")
  @BeanProperty
  var experiment: File = _

  @Valid
  @NotNull(message = "please provide a separator, which defines how your input files are separated --input.separator=<character>")
  @BeanProperty
  var separator: String = "\t"

  @Valid
  @NotNull(message = "please provide the flag if you like to replace values or not --input.enableReplacement=<character>")
  @BeanProperty
  var enableReplacement:Boolean = false
}


//@Controller
class WorkflowRunner extends CommandLineRunner with LazyLogging {


  @Autowired
  val positiveModeTargetWorkflow: LCMSPositiveModeTargetWorkflow[Double] = null

  @Autowired
  val properties: WorkflowRunnerProperties = null

  override def run(args: String*): Unit = {

    args.foreach { line =>
      logger.info(s"Argument: '${line}'")
    }

    val file = properties.experiment

    logger.info(s"input file is: ${properties.experiment}")
    assert(file.exists())

    logger.info(s"output file is: ${properties.result}")

    val result = positiveModeTargetWorkflow.process(new FileInputStream(properties.experiment))

    Files.copy(result, Paths.get(properties.result.toString), REPLACE_EXISTING)

  }
}
