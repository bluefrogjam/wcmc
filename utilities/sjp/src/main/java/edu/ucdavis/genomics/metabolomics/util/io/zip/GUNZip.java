/*
 * <p>
 * Created on 04.04.2003 <br>
 * Filename GUNZip.java
 * Projekt BinBaseDatabase
 *
 *
 */
package edu.ucdavis.genomics.metabolomics.util.io.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.zip.GZIPInputStream;


/**
 * <h3>
 * Title: GUNZip
 * </h3>
 *
 * <p>
 * Author:      Gert Wohlgemuth <br>
 * Leader:      Dr. Oliver Fiehn <br>
 * Company:     Max Plank Institute for molecular plant physologie <br>
 * Contact:     wohlgemuth@mpimp-golm.mpg.de <br>
 * Version:     <br>
 * Description:

 * </p>
 */
public class GUNZip {
    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(String[] args) throws Exception {
        FileOutputStream out = new FileOutputStream(new File(args[1]));

        GZIPInputStream input = new GZIPInputStream(new FileInputStream(args[0]));

        byte[] data = new byte[1];
        int count = 0;

        System.out.println("start");

        while (true) {
            count = input.read(data);
            out.write(data);
            System.out.print(".");

            if (count == -1) {
                break;
            }
        }

        out.flush();
        out.close();
        input.close();

        System.out.println("finish");
    }
}
