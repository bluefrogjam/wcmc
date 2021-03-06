/*
 * Created on Aug 20, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.replacement;

import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Meanable;

import java.util.List;


/**
 * @author wohlgemuth
 * @version Aug 20, 2003
 * <br>
 * BinBaseDatabase
 * @description
 */
public class ReplaceZeroValues {
    /**
     * @uml.property name="file"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    DataFile file = null;

    /**
     * @uml.property name="mean"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    Meanable mean = null;

    /**
     * @author wohlgemuth
     * @version Aug 20, 2003
     * <br>
     */
    public ReplaceZeroValues() {
        super();
    }

    /**
     * @param datafile
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @uml.property name="file"
     */
    public void setFile(DataFile datafile) {
        file = datafile;
    }

    /**
     * @return
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @uml.property name="file"
     */
    public DataFile getFile() {
        return file;
    }

    /**
     * @param meanable
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @uml.property name="mean"
     */
    public void setMean(Meanable meanable) {
        mean = meanable;
    }

    /**
     * @return
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @uml.property name="mean"
     */
    public Meanable getMean() {
        return mean;
    }

    /**
     * f?hrt die ersetzungen durch
     *
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     */
    public void run() {
        int columnCount = file.getColumnCount();
        this.mean.enableZeros(false);

        for (int i = 0; i < columnCount; i++) {
            List column = file.getColumn(i);

            double mean = this.mean.calculate(column);

            for (int x = 0; x < column.size(); x++) {
                Object o = column.get(x);

                if (o instanceof Number) {
                    if (Math.abs(((Number) o).doubleValue() - 0) < 0.0001) {
                        column.set(x, new Double(mean));
                    }
                }
            }

            file.setColumn(i, column);
        }
    }
}
