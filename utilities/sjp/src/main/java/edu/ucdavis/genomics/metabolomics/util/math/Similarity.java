/*
 * Created on 20.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.math;

import edu.ucdavis.genomics.metabolomics.exception.SpectraConversionException;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * <h3>Title: Similarity</h3>
 * <p>
 * <p>
 * Author: Gert Wohlgemuth <br>
 * Leader: Dr. Oliver Fiehn <br>
 * Company: Max Plank Institute for molecular plant physologie <br>
 * Contact: wohlgemuth@mpimp-golm.mpg.de <br>
 * Version: <br>
 * Description: Calculates the similarity between 2 spectras <br>
 * the result is between 0 and 1000 for optimal match
 * <p>
 * </p>
 */
public class Similarity implements SpectraArrayKey {
    Vector tempVector = new Vector();

    /**
     * contains spectra data <br>
     * <p>
     * <table border = 1>SpectraConversionException.java
     * <tr>
     * <th>id</th>
     * <th>description</th>
     * <th>typ</th>
     * </tr>
     * <tr>
     * <td>[i][0]</td>
     * <td>ion</td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td>[i][1]</td>
     * <td>abundance</td>
     * <td>Double</td>
     * </tr>
     * <tr>
     * <td>[i][2]</td>
     * <td>unique</td>
     * <td>Boolean</td>
     * </tr>
     * </table>
     */
    double[][] librarySpectra;

    /**
     * contains spectra data with the same ions <br>
     * <p>
     * <table border = 1>
     * <tr>
     * <th>id</th>
     * <th>description</th>
     * <th>typ</th>
     * </tr>
     * <tr>
     * <td>[i][0]</td>
     * <td>ion</td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td>[i][1]</td>
     * <td>abundance</td>
     * <td>Double</td>
     * </tr>
     * <tr>
     * <td>[i][2]</td>
     * <td>unique</td>
     * <td>Boolean</td>
     * </tr>
     * </table>
     */
    double[][] sameSpectra;
    double[] tempData;

    /**
     * contains spectra data <br>
     * <p>
     * <table border = 1>
     * <tr>
     * <th>id</th>
     * <th>description</th>
     * <th>typ</th>
     * </tr>
     * <tr>
     * <td>[i][0]</td>
     * <td>ion</td>
     * <td>Integer</td>
     * </tr>
     * <tr>
     * <td>[i][1]</td>
     * <td>abundance</td>
     * <td>Double</td>
     * </tr>
     * <tr>
     * <td>[i][2]</td>
     * <td>unique</td>
     * <td>Boolean</td>
     * </tr>
     * </table>
     */
    double[][] unknownSpectra;
    double f1 = 0;
    double f2 = 0;
    double lib = 0;
    double sqrt1 = 0;
    double sum = 1;
    double sum4 = 0;
    double sum_2 = 0;
    double sum_3 = 0;
    double unk = 0;

    /**
     * count of same ions
     */
    int sameCount;

    /**
     * @param librarySpectra
     * @uml.property name="librarySpectra"
     */
    public void setLibrarySpectra(double[][] librarySpectra) {
        this.librarySpectra = sizeSpectra(librarySpectra);
    }

    /**
     * resizes the spectra
     *
     * @param spectra
     * @return
     */
    public double[][] sizeSpectra(double[][] spectra) {
        if (spectra.length == MAX_ION) {
            return spectra;
        } else {
            double[][] bigSpectra = new double[MAX_ION][ARRAY_WIDTH];

            for (int i = 0; i < MAX_ION; i++) {
                bigSpectra[i][FRAGMENT_ION_POSITION] = i + 1;
                bigSpectra[i][FRAGMENT_ABS_POSITION] = 0;
                bigSpectra[i][FRAGMENT_REL_POSITION] = 0;

                for (int y = 0; y < spectra.length; y++) {
                    if (Math.abs(bigSpectra[i][FRAGMENT_ION_POSITION] -
                        spectra[y][FRAGMENT_ION_POSITION]) < 0.0001) {
                        bigSpectra[i][FRAGMENT_ION_POSITION] = spectra[y][FRAGMENT_ION_POSITION];
                        bigSpectra[i][FRAGMENT_ABS_POSITION] = spectra[y][FRAGMENT_ABS_POSITION];
                        bigSpectra[i][FRAGMENT_REL_POSITION] = spectra[y][FRAGMENT_REL_POSITION];
                        y = spectra.length + 1;
                    }
                }
            }

            return bigSpectra;
        }
    }

    /**
     * returns the max ion count
     *
     * @return
     */
    public int getMaxIons() {
        return SpectraArrayKey.MAX_ION;
    }

    /**
     * @param librarySpectra
     */
    public void setLibrarySpectra(String librarySpectra) {
        this.setLibrarySpectra(convert(librarySpectra));
    }

    /**
     * @param unknownSpectra
     * @uml.property name="unknownSpectra"
     */
    public void setUnknownSpectra(double[][] unknownSpectra) {
        this.unknownSpectra = sizeSpectra(unknownSpectra);
    }

    /**
     * @param unknownSpectra
     */
    public void setUnknownSpectra(String unknownSpectra) {
        this.setUnknownSpectra(convert(unknownSpectra));
    }

    /**
     * calculates the similarity between this two spectras
     *
     * @return
     */
    public double calculateSimimlarity() {
        /* check spectra with higher count and than compare the lowler spectra */
        double[][] tempSpectra = new double[getMaxIons()][5];

        sameCount = 0;

        //getting the same count of ions
        for (int i = 0; i < getMaxIons(); i++) {
            if (Math.abs(librarySpectra[i][0] - unknownSpectra[i][0]) < 0.0001) {
                if ((librarySpectra[i][FRAGMENT_ABS_POSITION] > 0) &&
                    (unknownSpectra[i][FRAGMENT_ABS_POSITION] > 0)) {
                    tempSpectra[sameCount][0] = librarySpectra[i][FRAGMENT_ION_POSITION];
                    tempSpectra[sameCount][1] = unknownSpectra[i][FRAGMENT_REL_POSITION];
                    tempSpectra[sameCount][2] = librarySpectra[i][FRAGMENT_REL_POSITION];
                    tempSpectra[sameCount][3] = unknownSpectra[i][FRAGMENT_ABS_POSITION];
                    tempSpectra[sameCount][4] = librarySpectra[i][FRAGMENT_ABS_POSITION];
                    sameCount++;
                }
            }
        }

        this.sameSpectra = new double[sameCount][5];

        for (int i = 0; i < this.sameCount; i++) {
            for (int x = 0; x < 5; x++) {
                this.sameSpectra[i][x] = tempSpectra[i][x];
            }
        }

        f1 = 0;
        f2 = 0;
        sqrt1 = 0;
        sum = 1;

        sum_2 = 0;
        sum_3 = 0;
        unk = 0;
        lib = 0;
        sum4 = 0;

        //calculate f1
        for (int i = 0; i < sameCount; i++) {
            sqrt1 = Math.sqrt(sameSpectra[i][2] * sameSpectra[i][1]);
            sum4 = sum4 + (sqrt1 * sameSpectra[i][0]);

            if (i > 0) {
                unk = sameSpectra[i][1] / sameSpectra[i - 1][1];
                lib = sameSpectra[i][2] / sameSpectra[i - 1][2];

                if (unk <= lib) {
                    sum = sum + (unk / lib);
                } else {
                    sum = sum + (lib / unk);
                }
            }
        }

        for (int i = 0; i < librarySpectra.length; i++) {
            if (librarySpectra[i][FRAGMENT_ABS_POSITION] > 0) {
                sum_2 = sum_2 +
                    (librarySpectra[i][FRAGMENT_REL_POSITION] * librarySpectra[i][FRAGMENT_ION_POSITION]);
            }
        }

        for (int i = 0; i < unknownSpectra.length; i++) {
            if (unknownSpectra[i][FRAGMENT_ABS_POSITION] > 0) {
                sum_3 = sum_3 +
                    (unknownSpectra[i][FRAGMENT_REL_POSITION] * unknownSpectra[i][FRAGMENT_ION_POSITION]);
            }
        }

        f1 = sum4 / Math.sqrt(sum_2 * sum_3);
        f2 = 1.0 / sameCount * sum;

        return (1000.0 / (unknownSpectra.length + sameCount)) * ((unknownSpectra.length * f1) +
            (sameCount * f2));
    }


    /**
     * <p>
     * Dies Methode generiert aus einem Pegasus Spectren String ein Array
     * welches die daten enth???hlt Der String muss folgendes Format haben
     * </p>
     * <p>
     * 1:23 2:87 3:43 4:43 5:32
     * <p>
     * <p>
     * wenn das nicht der Fall ist wird eine NullPointer Exception ausgel???st
     * </p>
     * <p>
     * <p>
     * Die R???ckgabe erfolgt als ein mehr dimensionales Array. <br>
     * Auf die Positionen der einzelnen Arrays kann mittels folgender Variablen
     * zugegriffen werden
     * </p>
     * <p>
     * public static final int FRAGMENT_ION_POSITION
     * </p>
     * <p>
     * public static final int FRAGMENT_ABS_POSITION
     * </p>
     * <p>
     * public static final int FRAGMENT_REL_POSITION
     * </p>
     * <p>
     * public static final int MAX_ION = 500
     * </p>
     * <p>
     * <p>
     * Beispiel f???r einen Zugriff:
     * <p>
     * <p>
     * <h4>for(int i = 0; i < ValidateSpectra.MAX_ION; i++){ <br>
     * System.out.println(data[i][FRAGMENT_ION_POSITION]); <br>}<br>
     * </h4>
     * </p>
     *
     * @param spectra der spektren string
     * @return double Das Array
     * @author gert wohlgemuth
     */
    protected double[][] convert(String spectra) {
        StringTokenizer tokenizer = new StringTokenizer(spectra, " ");
        double maxAbundance = 0;
        HashMap map = new HashMap();

        // f???llt die hashmap mit den gew???nschten daten
        try {
            while (tokenizer.hasMoreTokens()) {
                StringTokenizer token = new StringTokenizer(tokenizer.nextToken(),
                    ":");
                Integer ion = new Integer(token.nextToken());
                Double abundance = new Double(token.nextToken());

                map.put(ion, abundance);

                double abs = abundance.doubleValue();

                if (abs >= maxAbundance) {
                    maxAbundance = abs;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new SpectraConversionException(e);
        }

        // generiert das spektrum
        double[][] array = new double[MAX_ION][ARRAY_WIDTH];

        for (int i = 0; i < MAX_ION; i++) {
            array[i][FRAGMENT_ION_POSITION] = i + 1;

            Integer index = new Integer(i + 1);

            if (map.containsKey(index)) {
                array[i][FRAGMENT_ABS_POSITION] = ((Double) map.get(index)).doubleValue();
                array[i][FRAGMENT_REL_POSITION] = array[i][FRAGMENT_ABS_POSITION] / maxAbundance * 100;
            } else {
                array[i][FRAGMENT_ABS_POSITION] = 0;
                array[i][FRAGMENT_REL_POSITION] = 0;
            }
        }
        if (array.length == MAX_ION) {
            return array;
        } else {
            double[][] bigSpectra = new double[MAX_ION][ARRAY_WIDTH];

            for (int i = 0; i < MAX_ION; i++) {
                bigSpectra[i][FRAGMENT_ION_POSITION] = i + 1;
                bigSpectra[i][FRAGMENT_ABS_POSITION] = 0;
                bigSpectra[i][FRAGMENT_REL_POSITION] = 0;

                for (int y = 0; y < array.length; y++) {
                    if (Math.abs(bigSpectra[i][FRAGMENT_ION_POSITION] -
                        array[y][FRAGMENT_ION_POSITION]) < 0.0001) {
                        bigSpectra[i][FRAGMENT_ION_POSITION] = array[y][FRAGMENT_ION_POSITION];
                        bigSpectra[i][FRAGMENT_ABS_POSITION] = array[y][FRAGMENT_ABS_POSITION];
                        bigSpectra[i][FRAGMENT_REL_POSITION] = array[y][FRAGMENT_REL_POSITION];
                        y = array.length + 1;
                    }
                }
            }

            return bigSpectra;
        }
    }

    public double[][] getLibrarySpectra() {
        return librarySpectra;
    }

    public double[][] getUnknownSpectra() {
        return unknownSpectra;
    }
}
