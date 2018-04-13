/*
 * Created on 23.04.2004
 *
 */
package edu.ucdavis.genomics.metabolomics.util.serialize;

import java.io.*;


/**
 * @author wohlgemuth stellt statische methoden zum serializieren und
 * convertieren von objekten zur verf?gung
 */
public class ClassConverter {
    /**
     * deserialisiert ein object aus dem angegebenen stream
     *
     * @param in
     * @return @throws
     * IOException
     */
    public static String convertStreamToString(InputStream in)
        throws IOException {
        BufferedReader stream = new BufferedReader(new InputStreamReader(in));
        StringBuffer buffer = new StringBuffer();
        String line = null;

        while ((line = stream.readLine()) != null) {
            buffer.append(line);
        }

        return buffer.toString();
    }

    /**
     * deserialisiert ein object aus dem angegebenen stream
     *
     * @param in
     * @return @throws
     * IOException
     * @throws ClassNotFoundException
     */
    public static Object desirializeObject(InputStream in)
        throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(in);

        return stream.readObject();
    }

    /**
     * gibt die gr?sse des objektes in bytes zur?ck
     *
     * @param o
     * @return @throws
     * IOException
     */
    public static int objectSize(Object o) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializeObject(o, out);

        return out.size();
    }

    /**
     * convertiert das objekt in einen input stream
     *
     * @param o
     * @return @throws
     * IOException
     */
    public static InputStream objectToInputStream(Object o)
        throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializeObject(o, out);

        ByteArrayInputStream stream = new ByteArrayInputStream(out.toByteArray());

        return stream;
    }

    /**
     * serialisiert ein object in einen outputstream
     *
     * @param o
     * @param out
     * @throws IOException
     */
    public static void serializeObject(Object o, OutputStream out)
        throws IOException {
        ObjectOutputStream oout = new ObjectOutputStream(out);
        oout.writeObject(o);
    }

    /**
     * convertiert den string in einen input stream
     *
     * @param o
     * @return @throws
     * IOException
     */
    public static InputStream stringToInputStream(String o) {
        ByteArrayInputStream stream = new ByteArrayInputStream(o.getBytes());

        return stream;
    }

    /**
     * public static Clob stringToClob(String content,Connection c) throws
     * SQLException{ CLOB cl = new CLOB((OracleConnection) c);
     * cl.setBytes(content.getBytes());
     *
     * return cl; }
     */
}
