package edu.ucdavis.fiehnlab.ms.carrot.core.io.mzxml;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.IonMode;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.NegativeMode;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 *
 */
public class MzXMLReader {

    private InputStream stream;
    private int peaksCount = 0;
    private StringBuilder charBuffer;
    private boolean compressFlag = false;
    private DefaultHandler handler = new MzXMLHandler();
    private String precision;

    // Retention time parser
    private DatatypeFactory dataTypeFactory;

    /*
     * This variables are used to set the number of fragments that one single
     * scan can have. The initial size of array is set to 10, but it depends of
     * fragmentation level.
     */
    private int parentTreeValue[] = new int[10];
    private int msLevelTree = 0;

    /*
     * This stack stores the current scan and all his fragments until all the
     * information is recover. The logic is FIFO at the moment of write into the
     * RawDataFile
     */
    private LinkedList<Spectra> parentStack;

    /*
     * This variable hold the present scan or fragment, it is send to the stack
     * when another scan/fragment appears as a parser.startElement
     */
    private Spectra buildingScan;

    private List<Spectra> scans = new ArrayList<>();

    public MzXMLReader(InputStream stream
    ) {
        // 256 kilo-chars buffer
        charBuffer = new StringBuilder(1 << 18);
        parentStack = new LinkedList<>();
        this.stream = stream;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public List<Spectra> run() throws DatatypeConfigurationException, ParserConfigurationException, SAXException, IOException {

        // Use the default (non-validating) parser
        SAXParserFactory factory = SAXParserFactory.newInstance();


        dataTypeFactory = DatatypeFactory.newInstance();

        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(stream, handler);


        return scans;
    }


    private class MzXMLHandler extends DefaultHandler {
        public void startElement(String namespaceURI, String lName, // local
                                 // name
                                 String qName, // qualified name
                                 Attributes attrs) throws SAXException {


            // <msRun>
            if (qName.equals("msRun")) {
                String s = attrs.getValue("scanCount");
            }

            // <scan>
            if (qName.equalsIgnoreCase("scan")) {

                if (buildingScan != null) {
                    parentStack.addFirst(buildingScan);
                    buildingScan = null;
                }

                /*
                 * Only num, msLevel & peaksCount values are required according
                 * with mzxml standard, the others are optional
                 */
                int scanNumber = Integer.parseInt(attrs.getValue("num"));
                short msLevel = Short.parseShort(attrs.getValue("msLevel"));

	            Boolean centroided = false;
	            if(attrs.getValue("centroided") != null && attrs.getValue("centroided").equals("1")) {
		            centroided = true;
	            }

                IonMode ionMode;
                String polarityAttr = attrs.getValue("polarity");
                if ((polarityAttr != null) && (polarityAttr.length() == 1))
                    if (polarityAttr.equals("+")) {
                        ionMode = new PositiveMode();
                    } else if (polarityAttr.equals("-")) {
                        ionMode = new NegativeMode();
                    } else {
                        throw new SAXException("unknown ion mode: " + polarityAttr);
                    }
                else
                    throw new SAXException("unknown ion mode: " + polarityAttr);

                peaksCount = Integer.parseInt(attrs.getValue("peaksCount"));

                // Parse retention time
                double retentionTime = 0;
                String retentionTimeStr = attrs.getValue("retentionTime");
                if (retentionTimeStr != null) {
                    Date currentDate = new Date();
                    Duration dur = dataTypeFactory
                            .newDuration(retentionTimeStr);
                    retentionTime = dur.getTimeInMillis(currentDate) / 1000d ;
                } else {
                    throw new SAXException("Could not read retention time");
                }

                int parentScan = -1;

                if (msLevel > 9) {
                    throw new SAXException(
                            "The value of msLevel is bigger than 10");
                }

                if (msLevel > 1) {
                    parentScan = parentTreeValue[msLevel - 1];
                }

                // Setting the level of fragment of scan and parent scan number
                msLevelTree++;
                parentTreeValue[msLevel] = scanNumber;

                buildingScan = new Spectra();
                buildingScan.retentionTime = retentionTime;
                buildingScan.msLevel = msLevel;
                buildingScan.scanNumber = scanNumber;
                buildingScan.parentScan = parentScan;
                buildingScan.ionMode = ionMode;
	            buildingScan.centroided = centroided;
            }

            // <peaks>
            if (qName.equalsIgnoreCase("peaks")) {
                // clean the current char buffer for the new element
                charBuffer.setLength(0);
                compressFlag = false;
                String compressionType = attrs.getValue("compressionType");
                if ((compressionType == null)
                        || (compressionType.equals("none"))) {
                    compressFlag = false;
                }
                else {
                    compressFlag = true;
                }
                precision = attrs.getValue("precision");

            }

            // <precursorMz>
            if (qName.equalsIgnoreCase("precursorMz")) {
                // clean the current char buffer for the new element
                charBuffer.setLength(0);
                String precursorCharge = attrs.getValue("precursorCharge");
                if (precursorCharge != null)
                    buildingScan.precursorCharge = Integer
                            .parseInt(precursorCharge);
            }

        }

        /**
         * endElement()
         */
        public void endElement(String namespaceURI, String sName, // simple name
                               String qName // qualified name
        ) throws SAXException {

            // </scan>
            if (qName.equalsIgnoreCase("scan")) {

                msLevelTree--;

                /*
                 * At this point we verify if the scan and his fragments are
                 * closed, so we include the present scan/fragment into the
                 * stack and start to take elements from them (FIFO) for the
                 * RawDataFile.
                 */

                if (msLevelTree == 0) {
                    parentStack.addFirst(buildingScan);
                    buildingScan = null;
                    while (!parentStack.isEmpty()) {
                        Spectra currentScan = parentStack.removeLast();
                        scans.add(currentScan);
                    }

                    /*
                     * The scan with all his fragments is in the RawDataFile,
                     * now we clean the stack for the next scan and fragments.
                     */
                    parentStack.clear();

                }

                return;
            }

            // <precursorMz>
            if (qName.equalsIgnoreCase("precursorMz")) {
                final String textContent = charBuffer.toString();
                double precursorMz = 0d;
                if (!textContent.isEmpty())
                    precursorMz = Double.parseDouble(textContent);
                buildingScan.precursor = precursorMz;

                return;
            }

            // <peaks>
            if (qName.equalsIgnoreCase("peaks")) {

                byte[] peakBytes =  java.util.Base64.getDecoder().decode(charBuffer.toString());

                if (compressFlag) {
                    try {
                        peakBytes = decompress(peakBytes);
                    } catch (DataFormatException e) {
                        throw new SAXException("Corrupt compressed peak: "
                                + e.toString());
                    } catch (IOException e) {
                        throw new SAXException("Corrupt compressed peak: "
                                + e.toString());
                    }
                }

                // make a data input stream
                DataInputStream peakStream = new DataInputStream(
                        new ByteArrayInputStream(peakBytes));

                Ion dataPoints[] = new Ion[peaksCount];

                try {
                    for (int i = 0; i < dataPoints.length; i++) {

                        // Always respect this order pairOrder="m/z-int"
                        double massOverCharge;
                        float intensity;
                        if ("64".equals(precision)) {
                            massOverCharge = peakStream.readDouble();
                            intensity = (float)peakStream.readDouble();
                        } else {
                            massOverCharge = (double) peakStream.readFloat();
                            intensity = peakStream.readFloat();
                        }

                        // Copy m/z and intensity data
                        dataPoints[i] = new Ion(massOverCharge, intensity);
//	                    if(massOverCharge > 524.35 && massOverCharge < 524.38) {
//		                    System.out.println("DEBUG MzXMLReader --- ion: " + massOverCharge + ", " + intensity);
//	                    }
                    }
                } catch (IOException eof) {
                    throw new SAXException("Corrupt mzXML file: " + eof.getMessage(),eof);
                }

                // Set the final data points to the scan
                buildingScan.ions = dataPoints;

                return;
            }
        }

        /**
         * characters()
         *
         * @see org.xml.sax.ContentHandler#characters(char[], int, int)
         */
        public void characters(char buf[], int offset, int len)
                throws SAXException {
            charBuffer.append(buf, offset, len);
        }

        byte[] decompress(byte compressedBytes[])
                throws DataFormatException, IOException {

            Inflater decompresser = new Inflater();

            decompresser.setInput(compressedBytes);

            byte[] resultBuffer = new byte[compressedBytes.length * 2];
            byte[] resultTotal = new byte[0];

            int resultLength = decompresser.inflate(resultBuffer);

            while (resultLength > 0) {
                byte previousResult[] = resultTotal;
                resultTotal = new byte[resultTotal.length + resultLength];
                System.arraycopy(previousResult, 0, resultTotal, 0,
                        previousResult.length);
                System.arraycopy(resultBuffer, 0, resultTotal,
                        previousResult.length, resultLength);
                resultLength = decompresser.inflate(resultBuffer);
            }

            decompresser.end();

            return resultTotal;
/*

            ByteArrayInputStream in = new ByteArrayInputStream(compressedBytes);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DeflaterOutputStream  decompress = new DeflaterOutputStream(out);

            IOUtils.copy(in,decompress);
            decompress.flush();
            out.flush();
            in.close();
            decompress.close();
            out.close();

            return out.toByteArray();

            */
        }
    }

}