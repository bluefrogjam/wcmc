package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Centroided;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSMSSpectra;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by diego on 9/13/2016.
 */
public class RawSpectrum {
	private static Logger logger = LoggerFactory.getLogger(RawSpectrum.class);

	public double basePeakInt;
	public double basePeakMz;
	public double minInt;
	public int msLevel;
	public double mzHigh = -1;
	public double mzLow = -1;
	public int numPeaks;
	public char polarity;
	public double precursorMz;
	public double rt;
	public double rtMin;
	public double rtSec;
	public int rawScanNum;
	public int scanNum;
	public boolean centroided;
	public List<Ion> spect;
	public double tic;

	public RawSpectrum(int scanNum, double rt, double rtMin, double rtSec, int msLevel, int numPeaks, char polarity, double precursorMz, double basePeakMz, double basePeakInt, double mzLow, double mzHigh, double minInt, double tic, List<Ion> spec) {
		this.basePeakInt = basePeakInt;
		this.basePeakMz = basePeakMz;
		this.minInt = minInt;
		this.msLevel = msLevel;
		this.mzHigh = mzHigh;
		this.mzLow = mzLow;
		this.numPeaks = numPeaks;
		this.polarity = polarity;
		this.precursorMz = precursorMz;
		this.rt = rt;
		this.rtMin = rtMin;
		this.rtSec = rtSec;
		this.rawScanNum = scanNum;
		this.scanNum = scanNum;
		this.spect = spec;
		this.tic = tic;
	}

	public int msLevel() { return msLevel; }

	public RawSpectrum(MSSpectra spec) {
//		logger.debug(String.format("converting MSSpectra in RawSpectrum (scan: %d - ions: %d - centroided: %s)", spec.scanNumber(), spec.ions().length(), spec.isCentroided()));
		this.spect = new ArrayList<>();
		JavaConversions.seqAsJavaList(spec.ions()).stream().forEach(ion -> {
			edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion sion = (edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion) ion;
			this.spect.add(new Ion(sion.mass(), sion.intensity()));
		});

		this.basePeakInt = spec.basePeak().intensity();
		this.basePeakMz = spec.basePeak().mass();
		this.minInt = spect.stream().min(Comparator.comparing(Ion::mass)).get().intensity;
		this.msLevel = spec.msLevel();
		this.mzHigh = spect.stream().max(Comparator.comparing(Ion::mass)).get().mass;
		this.mzLow = spect.stream().min(Comparator.comparing(Ion::mass)).get().mass;
		this.numPeaks = spec.ions().length();
		this.precursorMz = (spec instanceof MSMSSpectra)? ((MSMSSpectra) spec).precursorIon() : 0;
		this.rt = 0;
		this.rtMin = spec.retentionTimeInMinutes();
		this.rtSec = spec.retentionTimeInSeconds();
		this.rawScanNum = spec.scanNumber();
		this.scanNum = -1;
		this.tic = spec.tic();
		this.centroided = spec instanceof Centroided;

		if(spec.ionMode().isDefined()) {
			this.polarity = (spec.ionMode().get() instanceof PositiveMode) ? '+' : '-';
		}
	}

	@Override
	public String toString() {
		return String.format("Scan:%d; RT(min):%f; MSLevel:%d; Polarity:%c; Precursor:%f; Spectrum:%s; Tic:%f", scanNum, rtMin, msLevel, polarity, precursorMz, String.join(":", spect.toString()), tic);
	}
}
