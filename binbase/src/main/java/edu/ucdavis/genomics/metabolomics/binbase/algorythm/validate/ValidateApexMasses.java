/*
 * Created on 08.05.2003
 *
 * To change the template for this generated file go to Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate;

import edu.ucdavis.genomics.metabolomics.exception.ValueNotFoundException;
import edu.ucdavis.genomics.metabolomics.util.BasicObject;
import edu.ucdavis.genomics.metabolomics.util.math.SpectraArrayKey;
import edu.ucdavis.genomics.metabolomics.util.search.BinarySearch;
import edu.ucdavis.genomics.metabolomics.util.search.Searchable;
import edu.ucdavis.genomics.metabolomics.util.sort.Quicksort;
import edu.ucdavis.genomics.metabolomics.util.sort.Sortable;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * @author wohlgemuth
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ValidateApexMasses extends BasicObject {
    /**
     * contact two apex masses strings, if size grater than x forgett
     *
     * @param apexOne
     * @param apexTwo
     * @return
     */
    public static final String add(String apexOne, String apexTwo) {
        Collection apexVectorOne = convert(apexOne);
        Collection apexVectorTwo = convert(apexTwo);
        StringBuffer buffer = new StringBuffer();

        Iterator it = apexVectorOne.iterator();

        while (it.hasNext() == true) {
            Object temp = it.next();

            if (apexVectorTwo.contains(temp) == false) {
                apexVectorTwo.add(temp);
            }
        }

        it = apexVectorTwo.iterator();

        while (it.hasNext() == true) {
            buffer.append((String) it.next());

            if (it.hasNext() == true) {
                buffer.append("+");
            }
        }

        return buffer.toString();
    }

    /**
     * reinigt die apexmasses
     *
     * @param apex
     *            der apexstring
     * @param spectra
     *            das dazugeh?rige spektrum
     * @return der gereinigte apexstring, falls die settings aus der
     *         konfigurationsdatei erf?llt wurden, falls nicht wird der
     *         urspr?ngliche string zur?ckgeliefert
     */
    public static final  String cleanApex(Collection col,
        double[][] spectraArray, int uniqueMass) {
        if (col == null) {
            throw new RuntimeException("ups you must habe save apexmasses");
        }

        Searchable search = new BinarySearch();
        Sortable sort = new Quicksort();

        double[][] quantMasses = new double[col.size()][SpectraArrayKey.ARRAY_WIDTH];

        spectraArray = ValidateSpectra.sortSpectra(spectraArray,
                SpectraArrayKey.FRAGMENT_ABS_POSITION);

        Iterator it = col.iterator();
        int i = 0;

        while (it.hasNext()) {
            try {
                double value = Double.parseDouble((String) it.next());

                value = search.search(spectraArray,
                        SpectraArrayKey.FRAGMENT_ION_POSITION, (int) value);
                quantMasses[i][SpectraArrayKey.FRAGMENT_ION_POSITION] = spectraArray[(int) value][SpectraArrayKey.FRAGMENT_ION_POSITION];
                quantMasses[i][SpectraArrayKey.FRAGMENT_ABS_POSITION] = spectraArray[(int) value][SpectraArrayKey.FRAGMENT_ABS_POSITION];
                quantMasses[i][SpectraArrayKey.FRAGMENT_REL_POSITION] = spectraArray[(int) value][SpectraArrayKey.FRAGMENT_REL_POSITION];
                i++;
            } catch (ValueNotFoundException e) {
            }
        }

        quantMasses = sort.sort(quantMasses,
                SpectraArrayKey.FRAGMENT_ABS_POSITION);

        quantMasses = ValidateSpectra.sizeDown(quantMasses);

        int count = Integer.parseInt(CONFIG.getValue("apex.count"));
        double minOffset = Double.parseDouble(CONFIG.getValue(
                    "apex.basePeakOffset"));
        int[] data;
        double[] factor;

        //der basepeak
        double max = 0;

        if (quantMasses.length >= count) {
            data = new int[count];
            factor = new double[count];

            int x = 0;

            for (int y = quantMasses.length - 1;
                    y >= (quantMasses.length - count); y--) {
                data[x] = (int) quantMasses[y][SpectraArrayKey.FRAGMENT_ION_POSITION];
                factor[x] = quantMasses[y][SpectraArrayKey.FRAGMENT_REL_POSITION];

                if (factor[x] > max) {
                    max = factor[x];
                }

                x++;
            }
        } else {
            data = new int[quantMasses.length];
            factor = new double[quantMasses.length];

            for (int y = 0; y < quantMasses.length; y++) {
                data[y] = (int) quantMasses[y][SpectraArrayKey.FRAGMENT_ION_POSITION];
                factor[y] = quantMasses[y][SpectraArrayKey.FRAGMENT_REL_POSITION];

                if (factor[y] > max) {
                    max = factor[y];
                }
            }
        }

        boolean containsUniqueMass = false;

        for (int x = 0; x < data.length; x++) {
            if (data[x] == uniqueMass) {
                containsUniqueMass = true;

                break;
            }
        }

        if (containsUniqueMass == true) {
            StringBuilder buffer = new StringBuilder();

            for (int x = 0; x < data.length; x++) {
                if ((factor[x] / max * 100) >= minOffset) {
                    buffer.append(String.valueOf(data[x]));
                    buffer.append("+");
                }
            }

            return buffer.toString();
        } else {
        	StringBuilder buffer = new StringBuilder();

            for (int x = 0; x < data.length; x++) {
                if ((factor[x] / max * 100) >= minOffset) {
                    buffer.append(String.valueOf(data[x]));
                    buffer.append("+");
                }
            }

            buffer.append(uniqueMass);

            return buffer.toString();
        }
    }

    /**
     * reinigt die apexmasses
     *
     * @param apex
     *            der apexstring
     * @param spectra
     *            das dazugeh?rige spektrum
     * @return der gereinigte apexstring, falls die settings aus der
     *         konfigurationsdatei erf?llt wurden, falls nicht wird der
     *         urspr?ngliche string zur?ckgeliefert
     */
    public static final  String cleanApex(String apex, String spectra,
        int uniqueMass) {
        double[][] spectraArray = ValidateSpectra.convert(spectra);

        if (apex.length() == 0) {
            return cleanApex(generateAutomaticApexMasses(spectra, uniqueMass),
                spectra, uniqueMass);
        } else {
            Collection col = ValidateApexMasses.convert(apex);

            return cleanApex(col, spectraArray, uniqueMass);
        }
    }

    /**
     * contact two apex masses strings
     *
     * @param apexOne
     * @param apexTwo
     * @return
     */
    public static final  String contact(String apexOne, String apexTwo) {
        Collection apexVectorOne = convert(apexOne);
        Collection apexVectorTwo = convert(apexTwo);
        StringBuffer buffer = new StringBuffer();

        Iterator it = apexVectorOne.iterator();

        while (it.hasNext() == true) {
            Object temp = it.next();

            if (apexVectorTwo.contains(temp) == false) {
                apexVectorTwo.add(temp);
            }
        }

        it = apexVectorTwo.iterator();

        while (it.hasNext() == true) {
            buffer.append((String) it.next());

            if (it.hasNext() == true) {
                buffer.append("+");
            }
        }

        return buffer.toString();
    }

    /**
     * tests if the string contains the specific apex mass
     *
     * @param apex
     *            123+312+214
     * @param mass
     *            123
     * @return true or false
     */
    public static final  boolean contains(String apex, int unique, int mass) {
        Collection data = convert(apex);

        if (unique == mass) {
            return true;
        }

        return data.contains(String.valueOf(mass));
    }

    /**
     * tests if the string contains the specific apex mass
     *
     * @param apex
     *            123+312+214
     * @param mass
     *            123
     * @return true or false
     */
    public static final  boolean  contains(String apex, int mass) {
        Collection data = convert(apex);

        return data.contains(String.valueOf(mass));
    }

    /**
     * converts an apex vector to an apex string
     *
     * @param apex
     *            vector with apex entrys
     * @return String 123+132+41+414
     */
    public static final  String convert(Collection apex) {
        StringBuffer buffer = new StringBuffer();

        Iterator it = apex.iterator();

        while (it.hasNext() == true) {
            buffer.append((String) it.next());
            buffer.append("+");
        }

        buffer.deleteCharAt(buffer.length() - 1);

        return buffer.toString();
    }

    /**
     * converts an apex arrray to an apex string
     *
     * @param apex
     *            vector with apex entrys
     * @return String 123+132+41+414
     */
    public static final  String convert(int[] apex) {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < apex.length; i++) {
            buffer.append(String.valueOf(apex[i]));
            buffer.append("+");
        }

        return buffer.toString();
    }

    /**
     * converts an apex string to an apex vector
     *
     * @param apex
     *            123+1231+42+4234
     * @return Vector with this String splitted by plus signs
     */
    public static final  Collection convert(String apex) {
        Vector quantIons = new Vector();

        if (apex.indexOf("+") > -1) {
            StringTokenizer tokenizer = new StringTokenizer(apex, "+");

            while (tokenizer.hasMoreTokens()) {
                quantIons.add(tokenizer.nextToken());
            }
        } else {
            quantIons.add(apex);
        }

        return quantIons;
    }

    /**
     * generates valid unique masses
     *
     * @param apex
     *            string seperated by +
     * @param unique
     *            mass as integer!
     * @return vector with apex masses
     */
    public static final  Collection generate(String apex, String unique) {
        if (apex.indexOf(unique) == -1) {
            apex = apex + "+" + unique;
        }

        return convert(apex);
    }

    /**
     * generiert automatisch apexmassen indem es die n gr?ssten massen aus der
     * konfigurationsdatei ausliest
     *
     * @param spectra
     *            das spektrum
     * @return die apexmassen
     */
    public static final  String generateAutomaticApexMasses(double[][] spectra,
        int unique) {
    	StringBuilder apex = new StringBuilder();

        Sortable sort = new Quicksort();

        spectra = ValidateSpectra.sizeDown(spectra);
        spectra = sort.sort(spectra, SpectraArrayKey.FRAGMENT_ABS_POSITION);

        boolean uniqueFound = false;

        for (int i = 0; i < spectra.length; i++) {
            apex.append((int) spectra[i][SpectraArrayKey.FRAGMENT_ION_POSITION]);
            apex.append("+");

            if ((int) spectra[i][SpectraArrayKey.FRAGMENT_ION_POSITION] == unique) {
                uniqueFound = true;
            }
        }

        if (uniqueFound == true) {
            apex.append(unique);
        } else {
            apex.deleteCharAt(apex.length() - 1);
        }

        return apex.toString();
    }

    /**
     * generiert automatisch apexmassen indem es die n gr?ssten massen aus der
     * konfigurationsdatei ausliest
     *
     * @param spectra
     *            das spektrum
     * @return die apexmassen
     */
    public static final  String generateAutomaticApexMasses(String spectra,
        int unique) {
        return generateAutomaticApexMasses(ValidateSpectra.convert(spectra),
            unique);
    }

    /**
     * remove the apex mass
     *
     * @param apex
     *            1234+1321+342
     * @param mass
     *            342
     * @return 1234+1231
     */
    public static final  String remove(String apex, int mass) {
        if (apex.indexOf(String.valueOf(mass)) > -1) {
            Collection data = convert(apex);
            data.remove(String.valueOf(mass));

            return convert(data);
        } else {
            return apex;
        }
    }

    /**
     * tests if the string is a apex string and not a spectra string
     *
     * @param apex
     *            String (123+3+23+43+...)
     * @return true or false
     */
    public static final  boolean validate(String apex) {
        if (apex.indexOf(":") > -1) {
            throw new RuntimeException(
                "This Sting is not a valid apex String (" + apex + ")");
        }

        return true;
    }
}
