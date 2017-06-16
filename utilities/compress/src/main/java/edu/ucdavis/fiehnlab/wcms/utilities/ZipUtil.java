package edu.ucdavis.fiehnlab.wcms.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * compress as given directory as zip file, using only Java core libraries
 */
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
public class ZipUtil {
    private static final int BUFFER_SIZE = 1024 * 4;

    public static void zipDir(String dir, OutputStream out,String prefix) throws IOException {
        ZipOutputStream zipOutputStream = null;
        if (out == null) {
            throw new NullPointerException("No output stream supplied");
        } else {
            try {
                zipOutputStream = new ZipOutputStream(out);
                int relativePos = dir.length() + 1;
                zipDir(new File(dir), zipOutputStream, relativePos,prefix,relativePos);
            } finally {
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
            }
        }
    }

    public static void zipDir(String dir, String zipFilename, String prefix) throws IOException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(zipFilename);
            zipDir(dir, fileOutputStream,prefix);
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    private static void zipDir(File dir, ZipOutputStream zipOutputStream, int relativePos,String prefix,int maxPositions) throws IOException {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    zipDir(file, zipOutputStream, relativePos,prefix,maxPositions);
                } else {
                    InputStream in = null;
                    try {
                        String filename = file.getPath();
                        if(maxPositions == relativePos) {
                            zipOutputStream.putNextEntry(new ZipEntry(prefix + "/" + filename.substring(relativePos)));
                        }
                        else{
                            zipOutputStream.putNextEntry(new ZipEntry(filename.substring(relativePos)));
                        }

                        in = new FileInputStream(file);
                        int len;
                        byte[] buf = new byte[BUFFER_SIZE];
                        while ((len = in.read(buf)) != -1) {
                            zipOutputStream.write(buf, 0, len);
                        }
                        zipOutputStream.closeEntry();
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                }
            }
        }
    }
}