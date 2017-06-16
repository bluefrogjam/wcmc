package edu.ucdavis.fiehnlab.wcms.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * compress as given directory as zip file, using only Java core libraries
 */
public class ZipUtil {

    /**
     * compress the given file as a zip.
     * @param dirObj the directory or file to compress
     * @param out the zip output stream as destination
     * @throws IOException
     */
    public static void zip(File dirObj, ZipOutputStream out) throws IOException {

        if (dirObj.isDirectory()) {

            byte[] tmpBuf = new byte[1024];

            for (File file : dirObj.listFiles()) {
                if (file.isDirectory()) {
                    zip(file, out);
                    continue;
                }
                FileInputStream in = new FileInputStream(file.getAbsolutePath());
                out.putNextEntry(new ZipEntry(file.getAbsolutePath()));
                int len;
                while ((len = in.read(tmpBuf)) > 0) {
                    out.write(tmpBuf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
        } else {
            FileInputStream in = new FileInputStream(dirObj.getAbsolutePath());
            out.putNextEntry(new ZipEntry(dirObj.getAbsolutePath()));
            byte[] tmpBuf = new byte[1024];
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            in.close();

        }
    }
}
