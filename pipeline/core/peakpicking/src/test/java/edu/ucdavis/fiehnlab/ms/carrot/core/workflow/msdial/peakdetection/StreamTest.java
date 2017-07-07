package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.Ion;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by diego on 9/14/2016.
 */

public class StreamTest {

	@Test
	public void testStreams() {
		String data = "(102.127372741699:2928.71509339084); (103.040687561035:76.3421225402862); (104.106727600098:10648.6778259277); (105.106880187988:521.718566894531)";
		BufferedReader reader = new BufferedReader(new StringReader(data));
		List<Ion> ions = new ArrayList<>();

		ions = Arrays.stream(data.split("; ")).map(x -> x.replaceAll("\\(|\\)", "").split(":")).map(i -> new Ion(Float.parseFloat(i[0]), Double.parseDouble(i[1]))).collect(Collectors.toList());

		ions.forEach(x-> System.out.println(x.mass));
	}
}
