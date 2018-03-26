/*
 * Created on Aug 18, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.trayname;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


/**
 * creates from one big txt file in the class format many small ones
 * @author wohlgemuth
 *
 */
public class TraynamesFromTxtCollection {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));

        String line;
        Map classes = new HashMap();
        Vector keys = new Vector();

        while ((line = reader.readLine()) != null) {
            String[] content = line.split("\t");

            if (classes.get(content[1]) == null) {
                classes.put(content[1], new Vector());
            }

            ((Vector) classes.get(content[1])).add(content[0]);
            keys.add(content[1]);
        }

        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i).toString();

            FileWriter writer = new FileWriter(k + ".txt");

            Collection t = (Collection) classes.get(k);
            Iterator it = t.iterator();

            while (it.hasNext()) {
                writer.write(it.next().toString());
                writer.write("\t");
                writer.write(k);
                writer.write("\n");
            }

            writer.flush();
            writer.close();
        }
    }
}
