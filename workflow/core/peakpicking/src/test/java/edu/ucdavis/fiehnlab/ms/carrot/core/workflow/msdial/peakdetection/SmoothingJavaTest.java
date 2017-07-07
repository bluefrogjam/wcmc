package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution.MsDialPeak;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.SmoothingMethod;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 8/31/2016.
 */
public class SmoothingJavaTest {
	@Test
	public void testSmooth() {
		List<MsDialPeak> peaks = new ArrayList<>();
		peaks.add(new MsDialPeak(1, 1, 1, 1));
		peaks.add(new MsDialPeak(2, 2, 2, 2));
		peaks.add(new MsDialPeak(3, 3, 3, 3));
		peaks.add(new MsDialPeak(4, 4, 4, 4));
		peaks.add(new MsDialPeak(5, 4, 4, 4));
		peaks.add(new MsDialPeak(6, 3, 3, 3));
		peaks.add(new MsDialPeak(7, 2, 2, 2));
		peaks.add(new MsDialPeak(8, 1, 1, 1));
		peaks.add(new MsDialPeak(1, 1, 1, 1));
		peaks.add(new MsDialPeak(2, 2, 2, 2));
		peaks.add(new MsDialPeak(3, 3, 3, 3));
		peaks.add(new MsDialPeak(4, 4, 4, 4));
		peaks.add(new MsDialPeak(5, 4, 4, 4));
		peaks.add(new MsDialPeak(6, 3, 3, 3));
		peaks.add(new MsDialPeak(7, 2, 2, 2));
		peaks.add(new MsDialPeak(8, 1, 1, 1));

		List<MsDialPeak> res = SmoothingJava.smoothPeaks(peaks, SmoothingMethod.LINEAR_WEIGHTED_MOVING_AVERAGE, 1, false);

		res.forEach(n -> System.out.println(n.scanNum + ", " + n.intensity));

		assert (res.size() > 0);
	}

}