package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.io;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.RawSpectrum;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by diego on 9/14/2016.
 */
@Component
public class MSDialSampleTxtReaderJava {
	public List<RawSpectrum> read(InputStream inputStream) {
		try {
			if (inputStream == null || inputStream.available() == 0)
				return new ArrayList<>();
		} catch (IOException e) {
			return new ArrayList<>();
		}

		List<RawSpectrum> spectra = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		/**
		 * converts the data into a RawSpectrum object
		 */
		spectra = reader.lines().map(line -> {
			if (!line.startsWith("Scan#")) {
				List<String> data = new ArrayList<>();
				data = Arrays.asList(line.split("\t"));

				String spec = "";
				if (data.size() == 15) {
					spec = data.get(14);
				}

				List<Ion> ions = new ArrayList<>();
				ions = Arrays.stream(spec.split("; ")).map(x -> x.replaceAll("\\(|\\)", "").split(":")).map(i -> new Ion(Double.parseDouble(i[0]), Double.parseDouble(i[1]))).collect(Collectors.toList());

				return new RawSpectrum(Integer.parseInt(data.get(0)), 0, Double.parseDouble(data.get(2)), Double.parseDouble(data.get(3)),
						Integer.parseInt(data.get(4)),
						Integer.parseInt(data.get(5)),
						data.get(6).charAt(0),
						data.get(7).isEmpty() ? 0.0 : Double.parseDouble(data.get(7)),
						Double.parseDouble(data.get(8)),
						Double.parseDouble(data.get(9)), Double.parseDouble(data.get(10)), Double.parseDouble(data.get(11)), Double.parseDouble(data.get(12)), Double.parseDouble(data.get(13)), ions);
			} else {
				return null;
			}
		}).filter(x -> x != null).collect(Collectors.toList());

		return spectra;
	}
}
