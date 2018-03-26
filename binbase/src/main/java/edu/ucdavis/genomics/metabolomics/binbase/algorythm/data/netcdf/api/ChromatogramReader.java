package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * reads a netcdf file
 * 
 * @author wohlgemuth
 * 
 */
public class ChromatogramReader {

	/**
	 * reads a chromatogram into a list of spectra
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public List<Spectrum> readChromatogram(String fileName) throws IOException {
		// NetCDF file
		NetcdfFile inputFile = null;

		// Stored representation of the read chromatogram
		List<Spectrum> chromatogram = new ArrayList<Spectrum>();

		try {
			// Open NetCDF file
			inputFile = NetcdfFile.open(fileName);

			// Read variables and confirm their existence in the file
			Variable massValueVariable = inputFile.findVariable("mass_values");
			Variable intensityValueVariable = inputFile
					.findVariable("intensity_values");
			Variable scanIndexVariable = inputFile.findVariable("scan_index");
			Variable scanTimeVariable = inputFile
					.findVariable("scan_acquisition_time");

			if (massValueVariable == null)
				throw new IOException("Could not find variable mass_values");
			if (intensityValueVariable == null)
				throw new IOException(
						"Could not find variable intensity_values");
			if (scanIndexVariable == null)
				throw new IOException(
						"Could not find variable scan_index from file "
								+ fileName);
			if (scanTimeVariable == null)
				throw new IOException(
						"Could not find variable scan_acquisition_time from file "
								+ fileName);

			// Confirm that the file has only one MS level
			assert (massValueVariable.getRank() == 1);

			// Read m/z and intensity scale factors
			Attribute massScaleFacAttr = massValueVariable
					.findAttribute("scale_factor");
			Attribute intScaleFacAttr = intensityValueVariable
					.findAttribute("scale_factor");
			double massValueScaleFactor = (massScaleFacAttr != null) ? massScaleFacAttr
					.getNumericValue().doubleValue() : 1;
			double intensityValueScaleFactor = (intScaleFacAttr != null) ? intScaleFacAttr
					.getNumericValue().doubleValue() : 1;

			// Read the index and retention time arrays
			Array indexArray, timeArray;

			try {
				indexArray = scanIndexVariable.read();
			} catch (Exception e) {
				throw new IOException(
						"Could not read from variable scan_index from file "
								+ fileName + ": " + e.toString());
			}

			try {
				timeArray = scanTimeVariable.read();
			} catch (Exception e) {
				throw new IOException(
						"Could not read from variable scan_acquisition_time from file "
								+ fileName);
			}

			// Determine the index boundaries for each spectrum
			int totalScans = scanIndexVariable.getShape()[0];
			int[] scanStartPositions = new int[totalScans + 1];

			IndexIterator indexIterator = indexArray.getIndexIterator();
			int idx = 0;

			while (indexIterator.hasNext())
				scanStartPositions[idx++] = (Integer) indexIterator.next();

			// Calculate the stop position for the last spectrum
			scanStartPositions[totalScans] = (int) massValueVariable.getSize();

			// Read retention times
			double[] retentionTimes = new double[totalScans];

			IndexIterator timeIterator = timeArray.getIndexIterator();
			idx = 0;

			while (timeIterator.hasNext()) {
				if (scanTimeVariable.getDataType().getPrimitiveClassType() == float.class
						|| scanTimeVariable.getDataType()
								.getPrimitiveClassType() == double.class)
					retentionTimes[idx++] = ((Double) timeIterator.next()) / 60d;
			}

			// Create a collection of all contained mass spectra
			for (int i = 0; i < totalScans; i++) {
				// Get scan starting position and length
				int[] spectrumStartPosition = new int[] { scanStartPositions[i] };
				int[] spectrumLength = new int[] { scanStartPositions[i + 1]
						- scanStartPositions[i] };

				// Get retention time of the scan
				double retentionTime = retentionTimes[i];

				// Read the mass spectrum data
				if (spectrumLength[0] > 0) {
					// Read mass and intensity values
					Array massValueArray;
					Array intensityValueArray;

					try {
						massValueArray = massValueVariable.read(
								spectrumStartPosition, spectrumLength);
						intensityValueArray = intensityValueVariable.read(
								spectrumStartPosition, spectrumLength);
					} catch (Exception e) {
						throw new IOException(
								"Could not read from variables mass_values and/or intensity_values.");
					}

					Index massValuesIndex = massValueArray.getIndex();
					Index intensityValuesIndex = intensityValueArray.getIndex();

					int spectrumSize = massValueArray.getShape()[0];
					List<DataPoint> dataPoints = new ArrayList<DataPoint>();

					for (int j = 0; j < spectrumSize; j++) {
						Index massIndex0 = massValuesIndex.set0(j);
						Index intensityIndex0 = intensityValuesIndex.set0(j);

						dataPoints.add(new DataPoint(massValueArray
								.getDouble(massIndex0) * massValueScaleFactor,
								intensityValueArray.getDouble(intensityIndex0)
										* intensityValueScaleFactor));
					}

					chromatogram.add(new Spectrum(i, retentionTime, dataPoints
							.toArray(new DataPoint[spectrumSize])));
				}

				// If the spectrum has no data, create an empty spectrum
				else
					chromatogram.add(new Spectrum(i, retentionTime,
							new DataPoint[0]));
			}
		} catch (Exception e) {
			throw new IOException("Couldn't open/read data file " + fileName
					+ ": " + e.toString());
		} finally {
			if (inputFile != null)
				inputFile.close();
		}

		return chromatogram;
	}
}
