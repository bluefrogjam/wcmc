/*
 * <p> Copyright: Copyright (c) 2003 </p><p> Company: Max Plank Institute </p><p> Author: Gert Wohlgemuth </p><p> Maintainer: Dr. Oliver Fiehn </p> formatted with JxBeauty (c) johann.langhofer@nextra.at
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import edu.ucdavis.genomics.metabolomics.util.math.Similarity;
import edu.ucdavis.genomics.metabolomics.util.math.SpectraArrayKey;
import edu.ucdavis.genomics.metabolomics.util.sort.Quicksort;


/**
 * different spectra utilities
 *
 * @author gert wohlgemuth
 */
public final class ValidateSpectra implements SpectraArrayKey {
	
	public static double[][] createSpectra(){
		return new double[MAX_ION][ARRAY_WIDTH];
	}
	
    /**
     * DOCUMENT ME!
     *
     * @param spectra
     *                    DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    /*
    public static Collection getTwentyHighestMasses(double[][] spectra) {
        Collection collection = new Vector();
        spectra = ValidateSpectra.sortSpectra(spectra,
                SpectraArrayKey.FRAGMENT_REL_POSITION);

        if (spectra.length >= 20) {
            for (int i = spectra.length - 1; i >= (spectra.length - 20); i--) {
                System.err.println(i);

                Ion mass = new Ion();
                mass.setMass((int) spectra[i][SpectraArrayKey.FRAGMENT_ION_POSITION]);
                mass.setRelativeIntensity(spectra[i][SpectraArrayKey.FRAGMENT_REL_POSITION]);

                collection.add(mass);
            }
        } else {
            for (int i = 0; i < spectra.length; i++) {
                Ion mass = new Ion();
                mass.setMass((int) spectra[i][SpectraArrayKey.FRAGMENT_ION_POSITION]);
                mass.setRelativeIntensity(spectra[i][SpectraArrayKey.FRAGMENT_REL_POSITION]);

                collection.add(mass);
            }
        }

        return collection;
    }

    */
    
    /**
     * <p>
     * Diese Methode addiert zwei Spectren. Dabei muss sichergestellt sein das
     * beide Spektren nach Fragementen sortiert vorliegen m???ssen. Da aus
     * Performance gr???nden keine interne Sortierung stattfindet. <br>
     * Als Ergebniss wird das sortierte Spektrum zur???ckgeliefert, wobei die
     * Relative Abundance neu berechnet wird.
     * </p>
     *
     * <p>
     * Wenn die beiden Spektren unterschiedliche L???ngen haben, oder ungleich
     * MAX_ION sind wird null zur???ckgegeben, da so nicht mehr sichergestellt
     * ist das die Spektren durch die aktuelle Softwareversion erstellt wurden
     * </p>
     *
     * @author gert wohlgemuth
     * @param spectraOne
     *                    one a
     * @param spectraTwo
     *                    be b
     * @return spectra
     */
    public static  double[][] add(double[][] spectraOne, double[][] spectraTwo) {
        if (spectraOne == null) {
            return spectraTwo;
        }

        if (spectraTwo == null) {
            return spectraOne;
        }

        if (spectraOne.length == spectraTwo.length) {
            if (spectraOne.length == MAX_ION) {
                double[][] spectra = new double[MAX_ION][ARRAY_WIDTH];
                double maxAbundance = 0;

                // addiert spectren
                for (int i = 0; i < MAX_ION; i++) {
                    spectra[i][FRAGMENT_ION_POSITION] = spectraOne[i][FRAGMENT_ION_POSITION];
                    spectra[i][FRAGMENT_ABS_POSITION] = spectraOne[i][FRAGMENT_ABS_POSITION] +
                        spectraTwo[i][FRAGMENT_ABS_POSITION];

                    if (spectra[i][FRAGMENT_ABS_POSITION] > maxAbundance) {
                        maxAbundance = spectra[i][FRAGMENT_ABS_POSITION];
                    }
                }

                return relative(spectra, maxAbundance);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param a
     *                    DOCUMENT ME!
     * @param b
     *                    DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static  double[][] add(String a, String b) {
        return add(convert(a), convert(b));
    }

    /**
     * calculates the basepeak
     *
     * @param spectra
     * @return
     */
    public static  int calculateBasePeak(double[][] spectra) {
        int peak = 0;
        double abundance = 0;

        for (int i = 0; i < spectra.length; i++) {
            if (spectra[i][SpectraArrayKey.FRAGMENT_ABS_POSITION] > abundance) {
                abundance = spectra[i][SpectraArrayKey.FRAGMENT_ABS_POSITION];
                peak = (int) spectra[i][SpectraArrayKey.FRAGMENT_ION_POSITION];
            }
        }

        return peak;
    }

    /**
     * <p>
     * Dies Methode generiert aus einem Pegasus Spectren String ein Array
     * welches die daten enth???hlt Der String muss folgendes Format haben
     * </p>
     *
     * 1:23 2:87 3:43 4:43 5:32
     *
     * <p>
     * wenn das nicht der Fall ist wird eine NullPointer Exception ausgel???st
     * </p>
     *
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
     *
     * <p>
     * Beispiel f???r einen Zugriff:
     * <p>
     * <p>
     * <h4>for(int i = 0; i < ValidateSpectra.MAX_ION; i++){ <br>
     * System.out.println(data[i][FRAGMENT_ION_POSITION]); <br>}<br>
     * </h4>
     * </p>
     *
     * @author gert wohlgemuth
     * @param spectra
     *                    der spektren string
     * @return double Das Array
     */
    public static  double[][] convert(String spectra) {
        spectra =spectra.trim();

        StringTokenizer tokenizer = new StringTokenizer(spectra, " ");
        double maxAbundance = 0;
        Map<Integer,Double> map = new HashMap<Integer,Double>();

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

        return sizeUp(array);
    }

    /**
     * convert collection to string
     *
     * @param spectra
     * @return
     */
    /*
    public static String convert(Collection spectra) {
        return SpectraConverter.collectionToString(spectra);
    }
*/
    /**
     * Konvertiert ein spectren array in einen absoluten spektren string
     *
     * @param spectra
     *                    valid spectra array
     * @return String of this absolute specte 12:4234 34:423 353:65
     */
    public static  String convert(double[][] spectra) {
        StringBuilder out = new StringBuilder();
        spectra = sizeDown(spectra);

        for (int i = 0; i < spectra.length; i++) {
            out.append(String.valueOf((int) spectra[i][FRAGMENT_ION_POSITION]));
            out.append(":");
            out.append(spectra[i][FRAGMENT_ABS_POSITION]);
            out.append(" ");
        }

        return out.toString().trim();
    }

    /**
     * converts an apex string to an int array
     *
     * @param apex
     *                    123+1231+42+4234
     * @return Vector with this String splitted by plus signs
     */
    public static  int[] convertToIntArray(String apex) {
        if (apex.indexOf("+") > -1) {
            StringTokenizer tokenizer = new StringTokenizer(apex, "+");
            int[] x = new int[tokenizer.countTokens()];

            int i = 0;

            while (tokenizer.hasMoreTokens()) {
                x[i] = Integer.parseInt(tokenizer.nextToken());
                i++;
            }

            return x;
        } else {
            return new int[] { Integer.parseInt(apex) };
        }
    }

    /**
     * Konvertiert ein spectren array in einen absoluten spektren string
     *
     * @param spectra
     *                    valid spectra array
     * @return String of this absolute specte 12:4234 34:423 353:65
     */
    public static  String convertToRelative(double[][] spectra) {
    	StringBuilder out = new StringBuilder();
        spectra = sizeDown(spectra);

        for (int i = 0; i < spectra.length; i++) {
            out.append(String.valueOf((int) spectra[i][FRAGMENT_ION_POSITION]));
            out.append(":");
            out.append(spectra[i][FRAGMENT_REL_POSITION]);
            out.append(" ");
        }

        return out.toString().trim();
    }

    /**
     * DOCUMENT ME!
     *
     * @param spectra
     *                    DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static  String convertToRelative(String spectra) {
        return ValidateSpectra.convertToRelative(ValidateSpectra.convert(
                spectra));
    }

    /**
     * generates a subspectra by the given percentage and recalculate the
     * relative values
     *
     * @param spectra
     * @param percentBasePeak
     * @return
     */
    public static  double[][] generateSubSpectra(double[][] spectra,
        double percentBasePeak) {
        spectra = sizeUp(spectra);

        double[][] array = new double[MAX_ION][ARRAY_WIDTH];

        double max = 0;

        for (int i = 0; i < MAX_ION; i++) {
            double relative = spectra[i][FRAGMENT_REL_POSITION];
            array[i][FRAGMENT_ION_POSITION] = spectra[i][FRAGMENT_ION_POSITION];

            if (spectra[i][FRAGMENT_ABS_POSITION] > max) {
                max = spectra[i][FRAGMENT_ABS_POSITION];
            }

            if (relative >= percentBasePeak) {
                array[i][FRAGMENT_ABS_POSITION] = spectra[i][FRAGMENT_ABS_POSITION];
                array[i][FRAGMENT_REL_POSITION] = spectra[i][FRAGMENT_REL_POSITION];
            } else {
                array[i][FRAGMENT_ABS_POSITION] = 0;
                array[i][FRAGMENT_REL_POSITION] = 0;
            }
        }

        return relative(array, max);
    }


    /**
     * Gibt das Spektrum auf der Kommandozeile aus
     *
     * @author gert wohlgemuth
     * @param spectra
     *                    spektrum
     */
    public static  void print(double[][] spectra) {
        try {
            print(spectra, System.out);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * gibt das spektrum ausputstream
     *
     * @author gert wohlgemuth
     * @param spectra
     *                    spektrum
     * @param out
     *                    stream
     * @exception IOException
     */
    public static  void print(double[][] spectra, OutputStream out)
        throws IOException {
        print(spectra, new BufferedOutputStream(out));
    }

    /**
     * gibt das spektrum ausputstream
     *
     * @author gert wohlgemuth
     * @param spectra
     *                    spektrum
     * @param out
     *                    stream
     * @exception IOException
     */
    public static  void print(double[][] spectra, BufferedOutputStream out)
        throws IOException {
        for (int i = 0; i < spectra.length; i++) {
            for (int x = 0; x < spectra[i].length; x++) {
                out.write((spectra[i][x] + "\t").getBytes());
            }

            out.write(("\n").getBytes());
        }

        out.flush();
    }

    /**
     * setzt alle fragmente welche eine relative abundance von y relOffset auf 0
     * und f?hrt dadurch ein renoising durch, wenn das fragment jedoch eine
     * Apexmasse ist findet kein renosing statt!
     *
     * @param spectra
     *                    das spektrum
     * @param relOffset
     *                    das relative offset was mindestens erreicht werden muss
     * @return
     *
     * methode ge?ndert! im fehlerfall zur?cksetzen!
     */
    public static  double[][] renoising(double[][] spectra, double relOffset,
        String apexMasses) {
        Collection data = ValidateApexMasses.convert(apexMasses);

        if (spectra.length != MAX_ION) {
            spectra = sizeUp(spectra);
        }

        double[] apexing = new double[MAX_ION];

        Iterator it = data.iterator();

        while (it.hasNext()) {
            double value = Double.parseDouble((String) it.next());
            apexing[(int) value - 1] = value;
        }

        for (int i = 0; i < spectra.length; i++) {
            if (spectra[i][FRAGMENT_REL_POSITION] < relOffset) {
                if ((int) spectra[i][FRAGMENT_ION_POSITION] != (int) apexing[i]) {
                    spectra[i][FRAGMENT_REL_POSITION] = 0;
                    spectra[i][FRAGMENT_ABS_POSITION] = 0;
                }
            }
        }

        return sizeUp(spectra);
    }

    /**
     * DOCUMENT ME!
     *
     * @param spectra DOCUMENT ME!
     * @param relOffset DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static  double[][] renoising(double[][] spectra, double relOffset) {
        if (spectra.length != MAX_ION) {
            spectra = sizeUp(spectra);
        }

        double[][] renoised = new double[MAX_ION][ARRAY_WIDTH];

        for (int i = 0; i < spectra.length; i++) {
            if (spectra[i][FRAGMENT_REL_POSITION] < relOffset) {
                renoised[i][FRAGMENT_ION_POSITION] = spectra[i][FRAGMENT_ION_POSITION];
                renoised[i][FRAGMENT_REL_POSITION] = 0;
                renoised[i][FRAGMENT_ABS_POSITION] = 0;
            } else {
                renoised[i][FRAGMENT_ION_POSITION] = spectra[i][FRAGMENT_ION_POSITION];
                renoised[i][FRAGMENT_REL_POSITION] = spectra[i][FRAGMENT_REL_POSITION];
                renoised[i][FRAGMENT_ABS_POSITION] = spectra[i][FRAGMENT_REL_POSITION];
            }
        }

        return sizeUp(renoised);
    }

    /**
     * kopiert das gegebenen spectrum auf den bereich zwischen den grenzen
     * firstFragement,maxFragement. diese methode dient nur zum verkleinern!
     *
     * @author gert wohlgemuth
     * @param spectra
     *                    das original spectrum
     * @param firstFragment
     *                    das erste gew???nschte fragment
     * @param lastFragment
     *                    das letzte gew???nschte fragment
     * @return
     */
    public static  double[][] resize(double[][] spectra, int firstFragment,
        int lastFragment) {
        int length = lastFragment - firstFragment + 1;
        double[][] spec = new double[length][ARRAY_WIDTH];
        spectra = sizeUp(spectra);

        int counter = 0;

        for (int i = 0; i < MAX_ION; i++) {
            if ((spectra[i][FRAGMENT_ION_POSITION] >= firstFragment) &&
                    (spectra[i][FRAGMENT_ION_POSITION] <= lastFragment)) {
                spec[counter][FRAGMENT_ION_POSITION] = spectra[i][FRAGMENT_ION_POSITION];
                spec[counter][FRAGMENT_ABS_POSITION] = spectra[i][FRAGMENT_ABS_POSITION];
                spec[counter][FRAGMENT_REL_POSITION] = spectra[i][FRAGMENT_REL_POSITION];
                counter++;
            }
        }

        return spec;
    }


    /**
     * <p>
     * Diese Methode berechnet die Similarity zwischen zwei Spektren. Diese
     * liegt zwischen 0 und 1000. Falls das nicht der Fall ist, liegt ein Fehler
     * im Algorythmus vor.
     * </p>
     *
     *                    a
     *                    b
     * @return similarity
     */
    public static  double similarity(double[][] library, double[][] unknown) {
    	try{
    		Similarity sim = new Similarity();
        sim.setLibrarySpectra(sizeUp(library));
        sim.setUnknownSpectra(sizeUp(unknown));

        return sim.calculateSimimlarity();
    	}
    	catch (ArrayIndexOutOfBoundsException e) {
    		throw e;
		}
    }

    /**
     * DOCUMENT ME!
     *
     * @param library
     *                    DOCUMENT ME!
     * @param unknown
     *                    DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public   double similarity(String library, String unknown) {
		Similarity sim = new Similarity();

        sim.setLibrarySpectra(sizeUp(convert(library)));
        sim.setUnknownSpectra(sizeUp(convert(unknown)));

        return sim.calculateSimimlarity();
    }

    /**
     * entfernt leere strings aus dem string so das die n?chste umwandlung
     * schneller geht
     *
     * @param spectra
     * @return
     */
    public static  String sizeDown(String spectra) {
        StringTokenizer token = new StringTokenizer(spectra, " ");
        StringBuilder result = new StringBuilder();

        try {
            while (token.hasMoreElements()) {
                StringTokenizer to = new StringTokenizer(token.nextToken(), ":");
                String ion = to.nextToken();
                String value = to.nextToken();

                if (Math.abs(Double.parseDouble(ion) - 0) < 0.0001) {
                } else {
                    result.append(ion + ":" + value + " ");
                }
            }
        } catch (NumberFormatException e) {
            throw new SpectraConversionException(e);
        }

        return result.toString().trim();
    }

    /**
     * <p>
     * L???scht aus dem gegebenen Spektrum alle Fragmente welche eine Abundance
     * von 0 haben
     * </p>
     *
     * @author gert wohlgemuth
     * @param spectra
     *                    spektrum
     * @return kleineres spektrum
     */
    public static  double[][] sizeDown(double[][] spectra) {
        int size = 0;

        for (int i = 0; i < spectra.length; i++) {
            if (spectra[i][FRAGMENT_ABS_POSITION] > 0) {
                size++;
            }
        }

        double[][] smallSpectra = new double[size][ARRAY_WIDTH];
        int counter = 0;

        for (int i = 0; i < spectra.length; i++) {
            if (spectra[i][FRAGMENT_ABS_POSITION] > 0) {
                smallSpectra[counter][FRAGMENT_ION_POSITION] = spectra[i][FRAGMENT_ION_POSITION];
                smallSpectra[counter][FRAGMENT_ABS_POSITION] = spectra[i][FRAGMENT_ABS_POSITION];
                smallSpectra[counter][FRAGMENT_REL_POSITION] = spectra[i][FRAGMENT_REL_POSITION];
                counter++;
            }
        }

        return smallSpectra;
    }

    /**
     * <p>
     * skaliert das spektrum auf max ion
     * </p>
     *
     * @author gert wohlgemuth
     * @param spectra
     *                    specumtrum
     * @return komplettes spektr
     */
    public static  double[][] sizeUp(double[][] spectra) {
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
     * sortiert das spektra mittels quicksort nach dem angegbenen key
     *
     * @param spectra
     *                    spectra
     * @param key
     *                    gew?nschter key
     * @return
     */
    public static  double[][] sortSpectra(double[][] spectra, int key) {
        spectra = sizeDown(spectra);
        spectra = new Quicksort().sort(spectra, key);

        return spectra;
    }

    /**
     * <p>
     * Generiert aus den absoluten werten des Spektrum ein relatives Spektrum
     * </p>
     *
     * @author gert wohlgemuth
     * @param spectra
     *                    Spektrum
     * @param maxAbundance
     *                    maximale absolute abundance
     * @return Spektrum
     */
    private static  double[][] relative(double[][] spectra, double maxAbundance) {
        for (int i = 0; i < MAX_ION; i++) {
            spectra[i][FRAGMENT_REL_POSITION] = spectra[i][FRAGMENT_ABS_POSITION] / maxAbundance * 100;
        }

        return spectra;
    }
    
    /**
     * calculates the relative abundance
     * @author wohlgemuth
     * @version Nov 8, 2006
     * @param spectra
     * @return
     */
    public static  double[][] convertToRelativeDouble(double[][] spectra){
    	return relative(spectra, spectra[calculateBasePeak(spectra)-1][FRAGMENT_ABS_POSITION]);
    }
}
