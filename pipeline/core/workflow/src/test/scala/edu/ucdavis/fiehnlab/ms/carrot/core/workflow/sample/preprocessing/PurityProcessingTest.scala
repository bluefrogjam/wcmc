package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import java.io.File

import edu.ucdavis.fiehnlab.ms.carrot.core.LoadersConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{RetentionIndexTarget, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ResourceLoaderSampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{WorkflowConfig, WorkflowProperties}
import edu.ucdavis.fiehnlab.wcms.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation._
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, ContextConfiguration, TestContextManager}

/**
  * Created by wohlgemuth on 6/26/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[PurityTestConfiguration],classOf[CaseClassToJSONSerializationConfiguration]))
class PurityProcessingTest extends WordSpec {

  @Autowired
  val sampleLoader:ResourceLoaderSampleLoader = null

  @Autowired
  val process: PurityProcessing = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "PurityProcessingTest" should {

    val samples: Seq[_ <: Sample] = sampleLoader.getSamples(Seq("B5_P20Lipids_Pos_Blank000.abf","B5_P20Lipids_Pos_NIST02.abf","B5_P20Lipids_Pos_QC000.abf","B5_SA0001_P20Lipids_Pos_1FL_1004.abf","B5_SA0002_P20Lipids_Pos_1FL_1006.abf"))

    samples.foreach { sample =>
      s"process $sample" in {

        val out = process.process(sample)

        assert(out.spectra != null)

        assert(out.spectra.nonEmpty)

        out.spectra.collect {

          case spec: MSSpectra =>
            assert(spec.purity.isDefined)
            assert(spec.purity.get >= 0)
            assert(spec.purity.get <= 1)
        }

      }
    }
  }
}

@Configuration
@Import(Array(classOf[WorkflowConfig], classOf[LoadersConfiguration]))
@EnableAutoConfiguration(exclude=Array(classOf[DataSourceAutoConfiguration]))
class PurityTestConfiguration {

  @Bean
  def correctionStandardList: LibraryAccess[RetentionIndexTarget] = new TxtStreamLibraryAccess[RetentionIndexTarget](new File("src/test/resources/retentionIndexStandards.txt"), "\t")

  @Bean
  def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](new File("src/test/resources/targets.txt"), "\t")

  @Bean(name = Array("quantification"))
  def quantification(properties: WorkflowProperties, libraryAccess: LibraryAccess[Target], quantificationPostProcessing: List[PostProcessing[Double]]): QuantifyByHeightProcess = new QuantifyByHeightProcess(libraryAccess, properties, quantificationPostProcessing)

  @Bean
  def quantificationPostProcessing: List[PostProcessing[Double]] = List.empty[PostProcessing[Double]]
}
