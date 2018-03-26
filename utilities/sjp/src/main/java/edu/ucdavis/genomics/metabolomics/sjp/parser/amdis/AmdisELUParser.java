package edu.ucdavis.genomics.metabolomics.sjp.parser.amdis;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.parser.msp.SpectraParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General Parser to read AMDIS ELU files
 */
public class AmdisELUParser extends Parser implements SpectraParser {
    boolean specStart = false;

    boolean dataSetStart = false;

    StringBuffer spectraBuffer;

    @Override
    protected void parseLine(String line) throws ParserException {

        if (line.startsWith("NAME:")) {

            //start a new data set
            this.dataSetStart = true;
            this.specStart = false;
            this.getHandler().startDataSet();

            //actual parsing of name attributes
            if (line.contains("|")) {
                String values[] = line.split("\\|");

                for (int i = 0; i < values.length; i++) {

                    String value = values[i];
//                    System.out.println(i + " - " + values[i]);

                    if (value.startsWith("SC")) {
                        writeAttribute(line, "Amdis Scan", value.replace("SC", ""));
                    }
                    if (value.startsWith("MP") && value.contains("MODN"))
                        writeAttribute(line, "UniqueMass", value.split(":")[1]);
                    if (value.startsWith("AM"))
                        writeAttribute(line, "Base Peak Intensity", value.replace("AM", ""));
                    if (value.startsWith("PC"))
                        writeAttribute(line, "Purity", value.replace("PC", ""));
                    if (value.startsWith("SN"))
                        writeAttribute(line, "S/N", value.replace("SN", ""));
                    if (value.startsWith("WD"))
                        writeAttribute(line, "Width", value.replace("WD", ""));
                    if (value.startsWith("TA"))
                        writeAttribute(line, "Tailing", value.replace("TA", ""));
                    if (value.startsWith("RT")) {
                        writeAttribute(line, "R.T. (minutes)", value.replace("RT", ""));
                        writeAttribute(line, "R.T. (seconds)", String.valueOf(Double.parseDouble(value.replace("RT", "")) * 60));
                    }
                    if (value.startsWith("MO"))
                        writeAttribute(line, "Quant Masses", value.split(":")[1].trim().replaceAll(" ", "+"));
                }
            }
        }
        //next line till end is a spectra
        else if (line.startsWith("NUM PEAKS")) {
            //next line starts the spectra
            this.specStart = true;
            this.spectraBuffer = new StringBuffer();

            //leave method
            return;
        }
        //read spectra in
        else if (specStart) {
            spectraBuffer.append(line);
        }
        if (line.trim().length() == 0) {
            this.dataSetStart = false;

            StringBuffer result = new StringBuffer();

            //regular expression to extract ion/value pairs. Ion is in group 2, value is in group 3
            String regex = "(([0-9]+),([0-9]+))+";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(spectraBuffer.toString());

            while (matcher.find()) {
                String ion = matcher.group(2);
                String intensity = matcher.group(3);

                result.append(ion);
                result.append(":");
                result.append(intensity);
                result.append(" ");
            }

            writeAttribute(line, SPECTRA, result.toString().trim());
            this.getHandler().endDataSet();

        }
    }


    /**
     * just a little helper to keep code cleaner
     *
     * @param element
     * @param name
     * @param value
     * @throws ParserException
     */
    private void writeAttribute(String element, String name, String value) throws ParserException {
        getHandler().startAttribute("", name, value);
        getHandler().endAttribute("", name);
    }
}
