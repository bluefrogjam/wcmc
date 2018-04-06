package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.provider;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.io.Copy;
import edu.ucdavis.genomics.metabolomics.util.io.source.FileSource;
import edu.ucdavis.genomics.metabolomics.util.io.source.NamedByteArraySource;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 4/21/15
 * Time: 12:11 PM
 */
public class SmartSampleDataProviderFactory extends SampleDataProviderFactory {
    @Override
    public SampleDataProvider createProvider(Source source) throws ConfigurationException {

        Logger logger = LoggerFactory.getLogger(getClass());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Copy.copy(source.getStream(), out);
            out.flush();
            byte[] bytes = out.toByteArray();
            out.close();

            SampleDataProvider provider = null;

            Source source1 = new NamedByteArraySource(bytes);
            source1.setIdentifier(source.getSourceName());


            Scanner scanner = new Scanner(new ByteArrayInputStream(bytes));

            String line = scanner.nextLine();

            if(line.trim().isEmpty()){
                logger.info("skipped first line, since it was empty");
                line = scanner.nextLine();
            }

            logger.info("first line:");
            logger.info(line);

            if (Pattern.compile("NAME:.+\\|SC[0-9]+.*").matcher(line).find()) {
                logger.info("amdis file");
                provider = new AmdisDataProvider();
                provider.setSource(source1);
            }
            else if (Pattern.compile("([A-Za-z0-9\\.\\(\\)\\-\\/\\%\\# ]+)\\t?").matcher(line).find()) {
                logger.info("pegasus file");
                provider = new PegasusASCIIIProvider();
                provider.setSource(source1);
            } else {
                logger.info("unknown file!");
                logger.warn("line was: " + line.length());

                if(scanner.hasNextLine()){
                    logger.warn("next line is: " + scanner.nextLine());
                }
                throw new ConfigurationException("sorry your provided source file is not supported!");
            }

            return provider;
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }

    public static void main(String args[]) throws Exception{
        SmartSampleDataProviderFactory factory = new SmartSampleDataProviderFactory();

        factory.createProvider(new FileSource(new File("/Volumes/gctof/b/txt/121130bddsa01_1.txt")));
    }
}
