package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution.util;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSample;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

//import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils.LCMSDataAccessUtility;

/**
 * Created by diego on 7/27/2016.
 */
public class LCMSDataAccessUtilityTest {
	private Sample sample = null;

	@Before
	public void setUp() throws Exception {
		sample = new MSDialSample(new FileInputStream(new File("src/test/resources/raw/spectrumCollection_LC_DDA-reduced.txt")),"spectrumCollection_LC_DDA-reduced.txt");
	}

	@After
	public void tearDown() throws Exception {
		sample = null;
	}

	@Ignore("Not implemented yet")
	@Test
	public void testGetCentroidedMassSpectra() throws Exception {
	}

//	@Ignore("need to setup access to big files")
//	@Test
//	public void testGetMs2Peaklist() throws Exception {
//		List<Peak> ms2PeakList = LCMSDataAccessUtility.getMs2Peaklist(sample, 249.1045512, 114.0670415, 4.0, 5.0, new PositiveMode(), 0.1, 0.1);
//		assert(ms2PeakList.size() > 0);
//	}

	@Ignore("Not implemented yet")
	@Test
	public void testGetRtStartIndex() throws Exception {
	}

	@Ignore("Not implemented yet")
	@Test
	public void testGetMs2StartIndex() throws Exception {
	}
}