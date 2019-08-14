package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.iupac;

import java.io.InputStream;
import java.util.*;

public class IUPACReference {

    public static Map<Integer, List<IUPACElement>> iupacElementsByID = new HashMap<>();
    public static Map<String, List<IUPACElement>> iupacElementsByName = new HashMap<>();

    /**
     * Load IUPAC data
     */
    static {
        final InputStream iupacStream = IUPACReference.class.getClassLoader().getResourceAsStream("IUPAC.txt");

        try (Scanner scanner = new Scanner(iupacStream)) {
            int iupacID = 0;
            String iupacElementName = "";
            List<IUPACElement> iupacElements = new ArrayList<>();

            while(scanner.hasNext()) {
                String line = scanner.nextLine().trim();
                String[] fields = line.split("\t");

                if (line.isEmpty())
                    break;

                if (iupacID != Integer.parseInt(fields[0])) {
                    if (iupacID != 0) {
                        iupacElementsByID.put(iupacID, iupacElements);
                        iupacElementsByName.put(iupacElementName, iupacElements);
                    }

                    iupacElements = new ArrayList<>();
                    iupacID = Integer.parseInt(fields[0]);
                    iupacElementName = fields[1];

                    iupacElements.add(new IUPACElement(fields));
                } else {
                    iupacElements.add(new IUPACElement(iupacID, fields));
                }

                iupacElementsByID.put(iupacID, iupacElements);
                iupacElementsByName.put(iupacElementName, iupacElements);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
