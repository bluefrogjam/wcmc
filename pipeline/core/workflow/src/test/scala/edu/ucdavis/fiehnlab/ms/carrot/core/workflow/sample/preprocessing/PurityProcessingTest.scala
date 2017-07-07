package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import edu.ucdavis.fiehnlab.ms.carrot.core.LoadersConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{RetentionIndexTarget, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.{WorkflowConfig, WorkflowProperties}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation._
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, ContextConfiguration, TestContextManager}

/**
  * Created by wohlgemuth on 6/26/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[PurityTestConfiguration]))
@ActiveProfiles(Array("common", "msdial"))
class PurityProcessingTest extends WordSpec {

	@Value("${storage.directory:src/test/resources}")
	var directory: String = ""

  @Autowired
  val process: PurityProcessing = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "PurityProcessingTest" should {

    val samples: List[_ <: Sample] =
      new MSDialSample(getClass.getResourceAsStream("/lipids/B5_P20Lipids_Pos_Blank000.msdial"), "B5_P20Lipids_Pos_Blank000.msdial") ::
        new MSDialSample(getClass.getResourceAsStream("/lipids/B5_P20Lipids_Pos_NIST02.msdial"), "B5_P20Lipids_Pos_NIST02.msdial") ::
        new MSDialSample(getClass.getResourceAsStream("/lipids/B5_P20Lipids_Pos_QC000.msdial"), "B5_P20Lipids_Pos_QC000.msdial") ::
        new MSDialSample(getClass.getResourceAsStream("/lipids/B5_SA0001_P20Lipids_Pos_1FL_1004.msdial"), "B5_SA0001_P20Lipids_Pos_1FL_1004.msdial") ::
        new MSDialSample(getClass.getResourceAsStream("/lipids/B5_SA0002_P20Lipids_Pos_1FL_1006.msdial"), "B5_SA0002_P20Lipids_Pos_1FL_1006.msdial") ::
        List()

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
@EnableAutoConfiguration(exclude=Array(classOf[DataSourceAutoConfiguration], classOf[HibernateJpaAutoConfiguration]))
@Import(Array(classOf[WorkflowConfig], classOf[LoadersConfiguration]))
@Profile(Array("common", "msdial"))
class PurityTestConfiguration {

  @Bean
  def process(properties: WorkflowProperties, puritySettings: PuritySettings): PurityProcessing = {
    new PurityProcessing(properties, puritySettings)
  }

  @Bean
  def correctionStandardList: LibraryAccess[RetentionIndexTarget] = new TxtStreamLibraryAccess[RetentionIndexTarget](getClass.getResourceAsStream("/retentionIndexStandards.txt"), "\t")

  @Bean
  def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](getClass.getResourceAsStream("/targets.txt"), "\t")

  @Bean(name = Array("quantification"))
  def quantification(properties: WorkflowProperties, libraryAccess: LibraryAccess[Target], quantificationPostProcessing: List[PostProcessing[Double]]): QuantifyByHeightProcess = new QuantifyByHeightProcess(libraryAccess, properties, quantificationPostProcessing)

  @Bean
  def quantificationPostProcessing: List[PostProcessing[Double]] = List.empty[PostProcessing[Double]]
}
