package edu.ucdavis.genomics.metabolomics.util.search;


/**
 * <p>
 *
 * @author wohlgemuth
 * @version Jul 17, 2003
 * </p>
 * <p>
 * <h4>File: Searchable.java </h4>
 * <h4>Project: glibj </h4>
 * <h4>Package: edu.ucdavis.genomics.metabolomics.glibj.alg.search </h4>
 * <h4>Type: Searchable </h4>
 */
public interface Searchable {
    /**
     * durchsucht ein array nach einem gew?nschten wert
     *
     * @param array das zu durchsuchende array
     */
    int search(double[] array, double value);

    /**
     * durchsucht ein mehrdimensionales array nach einem gew?nschten wert
     * in der gew?nschten s?ule
     *
     * @param array  das zu durchsuchende array
     * @param column die gew?nschte s?ule
     */
    int search(double[][] array, int column, double value);
}
