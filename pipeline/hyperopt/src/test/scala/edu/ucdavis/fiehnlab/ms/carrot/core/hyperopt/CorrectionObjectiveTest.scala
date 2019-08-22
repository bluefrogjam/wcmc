package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.optimizer.grid.SparkGridSearch
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.io.{ConversionAwareSampleLoader, ResourceLoaderSampleLoader}
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions.PeakHeightRSDCorrectionLossFunction
import org.apache.logging.log4j.scala.Logging
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.{Bean, Primary}
import org.springframework.test.context.TestContextManager

@RunWith(classOf[JUnitRunner])
class CorrectionObjectiveTest extends WordSpec {

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "CorrectionObjectiveTest" should {

    "apply-qtof-lipids" in {


      val samples: List[String] = List(
        "B2A_TEDDYLipids_Pos_QC006.mzml",
        "B2A_TEDDYLipids_Pos_QC007.mzml",
        "B2A_TEDDYLipids_Pos_QC008.mzml"
      )

      val method = AcquisitionMethod.serialize(AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode()))))

      evaluate(samples, method)
    }


    "apply-qtof-csh" in {

      val samples: List[String] = List(
        "Biorec003_posCSH_postFlenniken020.mzml",
        "Biorec001_posCSH_preFlenniken001.mzml",
        "Biorec004_posCSH_postFlenniken030.mzml"
      )

      val method = AcquisitionMethod.serialize(AcquisitionMethod(ChromatographicMethod("csh", Some("6530"), Some("test"), Some(PositiveMode()))))

      evaluate(samples, method)
    }


  }

  private def evaluate(samples: List[String], method: String) = {
    val sc = new SparkContext(new SparkConf().setAppName("Correction Objective Test").setMaster("local[8]"))
    val optimizer = new SparkGridSearch[Point, Double](sc)

    val correctionObjective = new CorrectionObjective(
      classOf[HyperoptTestConfiguration],
      Array("file.source.eclipse", "carrot.report.quantify.height", "carrot.processing.peakdetection", "carrot.lcms", "test", "carrot.targets.yaml.annotation", "carrot.targets.yaml.correction"),
      new PeakHeightRSDCorrectionLossFunction(),
      samples,
      method
    )

    correctionObjective.warmCaches()

    val result = optimizer.minimize(correctionObjective, correctionObjective.getSpace(
      Config(
        Hyperopt(
          spark = "localhost[*]",
          samples = List.empty,
          profiles = List.empty,
          method = "",
          stages = Stages(
            correction = Some(Correction(
              CorrectionSettings(
                massAccuracyPPM = List(5, 10, 20),
                massAccuracy = List(0.05, 0.06),
                rtAccuracy = List(1),
                minPeakIntensity = List(1000, 2000),
                intensityPenalty = List(10000)
              )
            ))
          )
        )
      )

    ))
    print(result)
    sc.stop()
  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration], classOf[MongoDataAutoConfiguration]))
@EnableCaching
class HyperoptTestConfiguration extends Logging {
  @Bean
  def annotationLibrary(@Autowired(required = false) targets: java.util.List[LibraryAccess[AnnotationTarget]]): DelegateLibraryAccess[AnnotationTarget] = {
    if (targets == null) {
      logger.warn("no library provided, annotations will be empty!")
      new DelegateLibraryAccess[AnnotationTarget](new java.util.ArrayList())
    }
    else {
      new DelegateLibraryAccess[AnnotationTarget](targets)
    }
  }

  @Bean
  def correctionLibrary(targets: java.util.List[LibraryAccess[CorrectionTarget]]): DelegateLibraryAccess[CorrectionTarget] = new DelegateLibraryAccess[CorrectionTarget](targets)

  @Bean
  @Primary
  def loader(resourceLoader: DelegatingResourceLoader): ResourceLoaderSampleLoader = new ResourceLoaderSampleLoader(resourceLoader)
}