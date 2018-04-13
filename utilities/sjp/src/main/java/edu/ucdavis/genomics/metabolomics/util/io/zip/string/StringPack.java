/*
 * <p>
 * Created on 16.04.2003 <br>
 * Filename StringPack.java
 * Projekt BinBaseDatabase
 *
 *
 */
package edu.ucdavis.genomics.metabolomics.util.io.zip.string;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * <h3>
 * Title: StringPack
 * </h3>
 * <p>
 * <p>
 * Author:      Gert Wohlgemuth <br>
 * Leader:      Dr. Oliver Fiehn <br>
 * Company:     Max Plank Institute for molecular plant physologie <br>
 * Contact:     wohlgemuth@mpimp-golm.mpg.de <br>
 * Version:     <br>
 * Description:
 * <p>
 * </p>
 */
public class StringPack {
    /**
     * DOCUMENT ME!
     *
     * @param array DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public static final String unzipArray(byte[] array)
        throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(array);
        GZIPInputStream zip = new GZIPInputStream(input);

        byte[] data = new byte[array.length];

        zip.read(data);

        return new String(data);
    }

    /**
     * DOCUMENT ME!
     *
     * @param string DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public static final byte[] zipString(String string)
        throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream zip = new GZIPOutputStream(out, 256);

        zip.write(string.getBytes());

        return out.toByteArray();
    }
}
