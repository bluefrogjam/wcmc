package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import java.io.{File, FileInputStream}
import javax.management.InvalidAttributeValueException

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.{DeconvolutedSample, MSDialSample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j.MSDialRestProcessor
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Created by diego on 2/2/2017.
 */
@Component
@Profile(Array("msdial"))
class MsdialPreProcessor @Autowired()(workflowProperties: WorkflowProperties) extends PreProcessor(workflowProperties) with LazyLogging {
	var log: Logger = Logger.getLogger(this.getClass)

  @Autowired
  var client: MSDialRestProcessor = null

	@Value("${msdialrest.file.source}")
  val source: String = "G:/Data/LCMS/RAW"

  override def doProcess(sample: Sample): Sample = {
    if(sample.fileName.isEmpty) {
      throw new InvalidAttributeValueException("The name of the sample can't be null or empty.")
    }

	  val file2Process = s"$source/${sample.fileName}"

    logger.info(s"Calling rest client, processing file: $file2Process")
	  client.process(new File(file2Process)) match {
      case result: File =>
        logger.info(s"\t\tresult fileName: ${sample.fileName} (${result.length()})")
        var deconvSample = new MSDialSample(new FileInputStream(result), sample.fileName) with DeconvolutedSample
        deconvSample
      case _ =>
        emptySample()
    }
  }

  def emptySample(fname: String = ""): Sample = new Sample {
    override val spectra: Seq[_ <: MSSpectra] = Seq.empty
    override val fileName: String = fname
  }
}
