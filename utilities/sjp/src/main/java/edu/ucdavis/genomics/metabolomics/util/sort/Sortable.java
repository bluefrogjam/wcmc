package edu.ucdavis.genomics.metabolomics.util.sort;


/**
 * <p>
 * @author wohlgemuth
 * @version Jul 17, 2003
 * </p>
 *
 * <h4>File: Sortable.java </h4>
 * <h4>Project: glibj </h4>
 * <h4>Package: edu.ucdavis.genomics.metabolomics.glibj.alg.sort </h4>
 * <h4>Type: Sortable </h4>
 *
 */
public interface Sortable {
    /**
     * sortiert das Array
     * @param array das zu sortierende Array
     *
     * @return das sortierte Array
     */
    double[] sort(double[] array);

    /**
     * sortiert das Array nach der gew?nschten Spalte
     * @param array das zu sortierende Array
     * @param key die gew?nschte Spalte
     *
     * @return das nach der gew?nschten Spalte sortierte array
     */
    double[][] sort(double[][] array, int key);
}
