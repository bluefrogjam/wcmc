/*
 * Created on 24.04.2004
 *
 */
package edu.ucdavis.genomics.metabolomics.util.serialize;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author wohlgemuth
 */
public class ClassConverterTest extends TestCase {
    /**
     * Constructor for ClassConverterTest.
     *
     * @param arg0
     */
    public ClassConverterTest(String arg0) {
        super(arg0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        TestRunner.run(ClassConverterTest.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testConvertStreamToString()
        throws IOException {
        InputStream stream = ClassConverter.stringToInputStream("test");
        String b = ClassConverter.convertStreamToString(stream);

        assertTrue("test".equals(b));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException            DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     */
    public void testDesirializeObject()
        throws IOException, ClassNotFoundException {
        Integer integer = new Integer(1);
        assertTrue(ClassConverter.desirializeObject(
            ClassConverter.objectToInputStream(integer)).equals(integer));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException            DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     */
    public void testObjectToInputStream()
        throws IOException, ClassNotFoundException {
        Integer integer = new Integer(1);
        assertTrue(ClassConverter.desirializeObject(
            ClassConverter.objectToInputStream(integer)).equals(integer));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException            DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     */
    public void testSerializeObject()
        throws IOException, ClassNotFoundException {
        Integer integer = new Integer(1);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ClassConverter.serializeObject(integer, stream);
        assertTrue(ClassConverter.desirializeObject(
            new ByteArrayInputStream(stream.toByteArray())).equals(integer));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testStringToInputStream()
        throws IOException {
        InputStream stream = ClassConverter.stringToInputStream("test");
        String b = ClassConverter.convertStreamToString(stream);

        assertTrue("test".equals(b));
    }
}
