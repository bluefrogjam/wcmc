package edu.ucdavis.genomics.metabolomics.util.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertTrue;

public class FileUtilTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCleanFileName() {
        String name = "test.txt";
        assertTrue(FileUtil.cleanFileName(name).equals("test"));
    }

    @Test
    public void testGenerateTempDirectory() {
        File dir = new File(System.getProperty("java.io.tmpdir")
            + File.separator + "/45354jj");

        logger.info("dir is: " + dir);
        if (dir.exists()) {
            dir.delete();

            logger.info("deleted dir: " + dir);
        }

        String path = FileUtil.generateTempDirectory("45354jj");

        logger.info("calculated path is: " + path);
        dir = new File(path);

        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());

        dir.delete();

    }

}
