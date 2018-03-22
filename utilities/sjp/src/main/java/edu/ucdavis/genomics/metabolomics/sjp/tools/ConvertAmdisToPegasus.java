package edu.ucdavis.genomics.metabolomics.sjp.tools;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.parser.amdis.AmdisELUParser;
import edu.ucdavis.genomics.metabolomics.util.io.source.ByteArraySource;
import edu.ucdavis.genomics.metabolomics.util.io.source.NamedByteArraySource;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;

import java.io.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convers an AMDIS dataset to a Pegasus Dataset
 */
public class ConvertAmdisToPegasus {

    /**
     * convertrs from the input source to the output source and allows easy chaining
     * @param input
     * @return
     */
    public static Source convert(Source input) throws Exception{

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        convert(input.getStream(),out);

        out.flush();

        ByteArraySource result = new NamedByteArraySource(out.toByteArray());

        result.setIdentifier(input.getSourceName());
        return result;
    }
    /**
     * converts the given input stream to a outputstrea,
     */
    public static void convert(InputStream amdisData, final OutputStream pegausData) throws ParserException {


        Parser parser = new AmdisELUParser();

        final BufferedWriter buffered =  new BufferedWriter(new OutputStreamWriter(pegausData));

        parser.parse(amdisData, new ParserHandler() {

            StringBuffer buffer = new StringBuffer();
            StringBuffer header = new StringBuffer();

            /**
             * last observed scan
             */
            String lastScan = "";

            /**
             * current scan
             */
            String currentScan;

            boolean firstLine = false;
            String seperator = "\t";

            @Override
            public void setProperties(Properties p) throws ParserException {

            }

            @Override
            public void endAttribute(String element, String name) throws ParserException {

            }

            @Override
            public void endDataSet() throws ParserException {
                try {

                    if (firstLine) {
                        buffered.write(header.toString());
                        buffered.write("\n");

                        firstLine = false;
                    }

                    //if the last scan equals the current scan
                        //skip
                    if(lastScan.equals(currentScan)){
                        //skip duplicatedscan
                        buffer = new StringBuffer();
                    }
                    else {
                        buffered.write(buffer.toString().trim());
                        buffered.write("\n");
                        buffer = new StringBuffer();

                        lastScan = currentScan;
                    }

                } catch (IOException e) {
                    throw new ParserException(e);
                }
            }

            @Override
            public void endDocument() throws ParserException {

                try {
                    buffered.flush();
                } catch (IOException e) {
                    throw new ParserException(e);
                }
            }

            @Override
            public void endElement(String name) throws ParserException {
                try {
                    buffered.flush();
                } catch (IOException e) {
                    throw new ParserException(e);
                }
            }

            @Override
            public void startAttribute(String element, String name, String value) throws ParserException {

                if(firstLine){
                    header.append(name);
                    header.append(seperator);
                }

                //drop the amdis metadata
                if(name.equals("UniqueMass")){
                    Pattern pattern = Pattern.compile("([0-9]+)");
                    Matcher matcher = pattern.matcher(value);

                    if(matcher.find()){
                        value = matcher.group(1);
                    }
                }
                //scale the purity to the pegasus 0-1 range
                if(name.equals("Purity")){
                    value = String.valueOf(Double.parseDouble(value)/100);
                }
                //needed to avoid printing duplicated scans
                if(name.equals("Amdis Scan")){
                    currentScan = value;
                }

                buffer.append(value);
                buffer.append(seperator);
            }

            @Override
            public void startDataSet() throws ParserException {

            }

            @Override
            public void startDocument() throws ParserException {
                firstLine = true;
            }

            @Override
            public void startElement(String name, String value) throws ParserException {

            }
        });
    }
}
