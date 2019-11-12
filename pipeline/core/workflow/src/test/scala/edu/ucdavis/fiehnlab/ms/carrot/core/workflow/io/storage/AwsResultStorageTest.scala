package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.storage

import edu.ucdavis.fiehnlab.loader.{ResourceLoader, ResourceStorage}
import edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket.BucketLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("test",
  "test_bucket",
  "carrot.lcms",
  "file.source.eclipse",
  "carrot.lcms.correction",
  "carrot.report.quantify.height",
  "carrot.processing.peakdetection",
  "carrot.processing.replacement.simple",
  "carrot.targets.yaml.annotation",
  "carrot.targets.yaml.correction",
  "carrot.output.storage.aws",
  "carrot.resource.store.bucket",
  "carrot.resource.loader.bucket",
  "carrot.output.storage.converter.target",
  "carrot.output.storage.converter.sample",
  "carrot.output.writer.json"
))
class AwsResultStorageTest extends WordSpec with Matchers with Logging {

  @Autowired
  val workflow: Workflow[Double] = null

  @Autowired
  val sampleLoader: SampleLoader = null

  @Autowired
  val storage: ResultStorage = null

  @Autowired
  val input: BucketLoader = null

  @Autowired
  val output: ResourceStorage = null

  new TestContextManager(this.getClass).prepareTestInstance(this)
  "LocalResultStorageTest" should {

    val sample: Sample = sampleLoader.getSample("B5_P20Lipids_Pos_QC000.mzml")
    val method = AcquisitionMethod(ChromatographicMethod("lcms_istds", Some("test"), Some("test"), Some(PositiveMode())))
    val results = workflow.process(sample, method, Some(sample))

    "send to aws" in {
      output.delete(sample.name)
      input.exists(sample.name) shouldBe false

      storage.store(Experiment(
        name = Some("test"),
        acquisitionMethod = method,

        classes = Seq(ExperimentClass(Seq(results), None)

        )), task = Task(name = "test", email = None, acquisitionMethod = method, samples = Seq(sample).map(x => SampleToProcess(fileName = x.fileName, className = "test"))))

      input.exists(sample.name) shouldBe true
    }

  }
}
