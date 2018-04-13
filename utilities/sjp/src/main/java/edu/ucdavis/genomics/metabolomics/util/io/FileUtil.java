/*
 * Created on 14.06.2003
 */
package edu.ucdavis.genomics.metabolomics.util.io;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

/**
 * @author wohlgemuth
 */
public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * @version Aug 5, 2003
     * @author wohlgemuth <br>
     * reiniegen des dateinamens
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
     * erstellt ein tempor?res verzeichnis. Der Name steht dabei f?r den letzten
     * zweig
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
            file = new File(filePath);
            file.mkdirs();

            return filePath;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * generates a list of files which were created or modfied on this date
     *
     * @param date
     * @return
     */
    public static File[] getFilesForDate(final Date date, File dir) {
        File[] files = dir.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                if (pathname.isFile()) {
                    Date modi = new Date(pathname.lastModified());
                    if (DateUtils.isSameDay(date, modi)) {
                        logger.debug("accepted file: " + pathname);
                        return true;
                    }
                }
                return false;
            }
        });
        return files;
    }

    /**
     * @param date
     * @param dir
     * @return
     */
    public static void setDateForFilesInDir(final Date date, File dir,
                                            final String pattern) {
        dir.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                if (pathname.isFile()) {
                    if (pattern != null) {
                        if (pathname.getName().matches(pattern)) {
                            pathname.setLastModified(date.getTime());
                        }
                    } else {
                        pathname.setLastModified(date.getTime());
                    }
                }
                return false;
            }
        });
    }

}
