package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Converter {

	/**
	 * 
	 * @return
	 */
	public Map<Scan, Double> toMZmineV1(List<Spectrum> spectra, boolean upScale) {
		Map<Scan, Double> export = new HashMap<Scan, Double>();

		for (Spectrum s : spectra) {
			// Convert m/z and intensity values to arrays
			int i = 0;
			double[] mz = new double[s.getNumberOfDataPoints()];
			double[] intensities = new double[s.getNumberOfDataPoints()];

			for (DataPoint p : s.getDataPoints()) {
				mz[i] = p.getMZ();
				intensities[i++] = p.getIntensity();
			}

			// Create an MZmine v1 Scan object of the current spectrum
			Scan scan = new Scan(mz, intensities, s.getSpectrumNumber(), s
					.getMzRange().getMin(), s.getMzRange().getMax());

			if (upScale) {
				export.put(scan, s.getRetentionTime() * 60 * 1000);

			} else {
				export.put(scan, s.getRetentionTime());
			}
		}

		return export;
	}

	/**
	 * 
	 * @return
	 */
	public String[] toAsCsv(List<Spectrum> spectra) {
		String[] export = new String[spectra.size() + 1];
		export[0] = "id,retention_time,tic,spectrum";
		int i = 1;

		for (Spectrum s : spectra) {
			// Build mass spectrum string
			StringBuilder sb = new StringBuilder();

			for (DataPoint p : s.getDataPoints()) {
				sb.append(' ');
				sb.append(p.getMZ());
				sb.append(':');
				sb.append(p.getIntensity());
			}

			sb.deleteCharAt(0);

			// Build CSV version of the current spectrum
			export[i++] = s.getSpectrumNumber() + "," + s.getRetentionTime()
					+ "," + s.getTotalIonCurrent() + "," + sb.toString();
		}

		return export;
	}
}
