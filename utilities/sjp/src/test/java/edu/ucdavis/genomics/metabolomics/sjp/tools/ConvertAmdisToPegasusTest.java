package edu.ucdavis.genomics.metabolomics.sjp.tools;

import junit.framework.TestCase;

import java.io.InputStream;
import java.io.OutputStream;

public class ConvertAmdisToPegasusTest extends TestCase {

    public void testConvert() throws Exception {

        InputStream stream = getClass().getResourceAsStream("/amdis.ELU");

        OutputStream out = System.out;

        ConvertAmdisToPegasus.convert(stream,out);

    }

    public void testConvert2() throws Exception {

        InputStream stream = getClass().getResourceAsStream("/amdis2.ELU");

        OutputStream out = System.out;

        ConvertAmdisToPegasus.convert(stream,out);

    }


}