package edu.ucdavis.genomics.metabolomics.sjp.tools;

import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.transform.AdamsDB5ToRTX5Transformator;
import edu.ucdavis.genomics.metabolomics.sjp.transform.RTX5RTtoRTX5RITransformator;

import java.io.*;
import java.util.Scanner;

/**
 * a simple class to convert one msp file with adams rt's to fiehn ri's
 *
 * @author wohlgemuth
 */
public class ConvertAdamsRTtoFiehnRi {
    public static void main(String[] args) throws FileNotFoundException, ParserException, IOException {

        Scanner scanner = new Scanner(new File(args[0]));

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(args[1])));

        AdamsDB5ToRTX5Transformator a = new AdamsDB5ToRTX5Transformator();
        RTX5RTtoRTX5RITransformator b = new RTX5RTtoRTX5RITransformator();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.matches("^RI:.*")) {
                Double d = new Double(line.split(":")[1].trim());
                line = "RI:" + b.transform(a.transform(d)).intValue();
            }
            writer.write(line);
            writer.write("\n");
        }

        writer.flush();
        writer.close();
    }
}
