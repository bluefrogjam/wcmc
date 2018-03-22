/*
 * Created on 14.06.2003
 */
package edu.ucdavis.genomics.metabolomics.util.io;

import java.io.File;


/**
 * @author wohlgemuth
 *
 */
public class FileUtil {
    /**
     * @version Aug 5, 2003
     * @author wohlgemuth <br>reiniegen des dateinamens
     */
    public static String cleanFileName(String file) {
        File f = new File(file);
        file = f.getName();

        if (file.indexOf(".") == -1) {
            return file;
        } else {
            file = file.substring(0, file.indexOf("."));

            return file;
        }
    }

    /**
     * erstellt ein tempor?res verzeichnis. Der Name steht dabei f?r den letzten zweig
     *
     * @param name
     * @return
     */
    public static String generateTempDirectory(String name) {
        try {
            File file = File.createTempFile("test", ".tmp");
            String filePath = file.getParent();
            filePath = filePath + "/" + name;
            file.deleteOnExit();

            return filePath;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
