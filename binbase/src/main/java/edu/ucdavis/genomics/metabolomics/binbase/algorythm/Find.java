/*
 * Created on 02.06.2003
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


/**
 * @author wohlgemuth stellt verschiedene m?glichkeiten zum suchen von spektren
 *         bereit. die spektren m?ssen dabei als collection von maps vorliegen
 *         und die key m?ssen den datenbankfeldern der importtabelle entsprechen
 */
public final class Find {
    /**
     * DOCUMENT ME!
     *
     * @param collection DOCUMENT ME!
     * @param binRi DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Collection calculateRiDifference(Collection collection,
        double binRi) {
        Collection col = new Vector();

        Iterator it = collection.iterator();

        while (it.hasNext()) {
            Object obj = it.next();

            Map map = (Map) obj;

            double ri = Double.parseDouble((String) map.get("retention_index"));
            double dif = Math.sqrt((binRi - ri) * (binRi - ri));

            if (map.get("ASSIGNED") == null) {
                map.put("DIFRENCE", new Double(dif));
                map.put("BIN_RI", new Double(binRi));

                col.add(map);
            }
        }

        return col;
    }

    /**
     * sucht das unbekannte spektrum heraus was die h?chste similarity hat
     *
     * @param col
     *            collection
     * @return Spektrum als Map
     */
    public static Map findBestQuantify(Collection col) {
        Map best = null;

        double lastQuantify = -1;

        Iterator it = col.iterator();

        while (it.hasNext()) {
            Map map = (Map) it.next();
            double quantify = Double.parseDouble((String) ((Map) map.get("BIN")).get(
                        "quantifier"));

            if (Math.abs(lastQuantify - (-1)) < 0.0001) {
                lastQuantify = quantify;
                best = map;
            }

            if (quantify > lastQuantify) {
                lastQuantify = quantify;
                best = map;
            }
        }

        return best;
    }

    /**
     * sucht das unbekannte spektrum heraus was am n?chsten am gew?nschten bin
     * dran ist
     *
     * @param col
     *            collection
     * @return Spectrum als map
     */
    public static Map findBestRetentionindexDifference(Collection col) {
        Map best = null;
        double lastRi = -1;

        Iterator it = col.iterator();

        while (it.hasNext()) {
            Map map = (Map) it.next();
            double diff = ((Double) map.get("DIFRENCE")).doubleValue();

            if (Math.abs(lastRi - (-1)) < 0.0001) {
                lastRi = diff;
                best = map;
            }

            if (diff < lastRi) {
                lastRi = diff;
                best = map;
            }
        }

        return best;
    }

    /**
     * sucht das unbekannte spektrum heraus was die h?chste similarity hat
     *
     * @param col
     *            collection
     * @return Spektrum als Map
     */
    public static Map findBestSimilarity(Collection col) {
        Map best = null;

        double lastSim = -1;

        Iterator it = col.iterator();

        while (it.hasNext()) {
            Map map = (Map) it.next();
            double sim = Double.parseDouble((String) map.get("similarity"));

            if (Math.abs(lastSim - (-1)) < 0.0001) {
                lastSim = sim;
                best = map;
            }

            if (sim > lastSim) {
                lastSim = sim;
                best = map;
            }
        }

        return best;
    }

    /**
     * sucht compounds im bereich von minimal/maximal heraus.
     *
     * die collection muss dazu elemente
     *
     * @param collection
     *            die collection
     * @param minimalRi
     *            der minimal ri
     * @param maximalRi
     *            der maximal ri
     * @param binRi
     * @return
     * @throws Exception
     */
    public static Collection findCompounds(Collection collection,
        double minimalRi, double maximalRi, double binRi)
        throws Exception {
        Collection col = new Vector();

        Iterator it = collection.iterator();

        while (it.hasNext()) {
            Object obj = it.next();

            Map map = (Map) obj;

            double ri = Double.parseDouble((String) map.get("retention_index"));
            double dif = Math.sqrt((binRi - ri) * (binRi - ri));

            //StaticLogger.getStaticLogger().debug("ri = " + ri + " dif " + dif + " bin ri " + binRi + " original ri " + map.get("retention_time"));
            if ((ri >= minimalRi) && (ri <= maximalRi)) {
                //                if (map.get("ASSIGNED") == null) {
                map.put("DIFRENCE", new Double(dif));
                map.put("BIN_RI", new Double(binRi));
                col.add(map);

                //StaticLogger.getStaticLogger().debug("add to collection...");
                //             } else {
                //StaticLogger.getStaticLogger().debug("already assigned...");
                //              }
            } else {
                //StaticLogger.getStaticLogger().debug("discard at filter");
            }
        }

        return col;
    }

    /**
     * sucht alle spectren aus der sammlung raus welche ein bestimmtes ion
     * enthalten, ist aber recht langsam da es eine lineare suche ist!
     *
     * @param col
     *            collection von spektrenmaps
     * @param ion
     *            array von ions
     * @return collection welche diese ionen enthalten oder eine leere
     *         collection
     */
    public static Collection findSpecsWithIonTrace(Collection col, int[] ion) {
        Collection result = new Vector();

        Iterator it = col.iterator();

        while (it.hasNext()) {
            Map map = (Map) it.next();
            double[][] masspsec = ValidateSpectra.convert((String) map.get(
                        "spectra"));

            for (int i = 0; i < ion.length; i++) {
                if (ion[i] <= ValidateSpectra.MAX_ION) {
                    if (masspsec[ion[i]][ValidateSpectra.FRAGMENT_ABS_POSITION] > 0) {
                        result.add(result);
                        i = ion.length + 1;
                    }
                }
            }
        }

        return result;
    }
}
