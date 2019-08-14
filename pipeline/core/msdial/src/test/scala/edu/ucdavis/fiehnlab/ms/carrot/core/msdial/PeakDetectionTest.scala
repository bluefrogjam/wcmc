package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{NegativeMode, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDialLCMSProcessedSample
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.{ConcurrentMapCache, ConcurrentMapCacheManager}
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner

import scala.collection.JavaConverters._

@RunWith(classOf[SpringRunner])
@SpringBootTest()
@ActiveProfiles(Array("carrot.lcms", "file.source.luna", "test", "carrot.processing.peakdetection"))
class PeakDetectionTest extends WordSpec {


  @Autowired
  val peakDetection: PeakDetection = null

  @Autowired
  val properties: MSDialLCMSProcessingProperties = null

  @Autowired
  val sampleLoader: SampleLoader = null


  val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(NegativeMode())))
  @Autowired
  val cacheManager: CacheManager = null

  val key = s"B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml_${method}"
  new TestContextManager(this.getClass).prepareTestInstance(this)
  "PeakDetectionTest" should {

    "clear caches" in {
      for (cache <- cacheManager.getCacheNames.asScala) {
        print(cache)
        cacheManager.getCache(cache).clear()
      }

    }
    "doProcess" in {

      assert(cacheManager.getCache("process-peak-detection").get(key) == null)
      val sample = sampleLoader.loadSample("B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml").get
      val result = peakDetection.process(sample, method, None)
      assert(cacheManager.getCache("process-peak-detection").get(key).get().asInstanceOf[MSDialLCMSProcessedSample].fileName == "B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml")
    }

    "doProcessCached" in {
      assert(cacheManager.getCache("process-peak-detection").get(key) != null)
      val sample = sampleLoader.loadSample("B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml").get
      val result = peakDetection.process(sample, method, None)
      assert(cacheManager.getCache("process-peak-detection").get(key).get().asInstanceOf[MSDialLCMSProcessedSample].fileName == "B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml")
    }

  }
}
