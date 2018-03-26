/**
 * Created on Jul 17, 2003
 */
package edu.ucdavis.genomics.metabolomics.util.search;

import edu.ucdavis.genomics.metabolomics.exception.ValueNotFoundException;
import edu.ucdavis.genomics.metabolomics.util.sort.Quicksort;
import edu.ucdavis.genomics.metabolomics.util.sort.Sortable;


/**
 * <p>
 * @author wohlgemuth
 * @version Jul 17, 2003
 * </p>
 *
 * <h4>File: BinarySearch.java </h4>
 * <h4>Project: glibj </h4>
 * <h4>Package: edu.ucdavis.genomics.metabolomics.glibj.alg.search </h4>
 * <h4>Type: BinarySearch </h4>
 */
public class BinarySearch implements Searchable {
    /**
     * DOCUMENT ME!
     *
     * @uml.property name="sort"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private Sortable sort = new Quicksort();

    /**
     * @see edu.ucdavis.genomics.metabolomics.glibj.alg.search.Searchable#search(double[])
     */
    public int search(double[] array, double value) {
        int left = 0;
        int right = array.length - 1;
        int middle = 0;

        while (left <= right) {
            middle = (left + right) / 2;

            if (Math.abs(array[middle] - value) < 0.0001) {
                break;
            }

            if (array[middle] < value) {
                left = middle + 1;
            } else {
                right = middle - 1;
            }
        }

        if (left > right) {
            throw new ValueNotFoundException("sorry value not found");
        } else {
            return middle;
        }
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.glibj.alg.search.Searchable#search(double[][], int)
     */
    public int search(double[][] array, int column, double value) {
        array = sort.sort(array, column);

        int left = 0;
        int right = array.length - 1;
        int middle = 0;

        while (left <= right) {
            middle = (left + right) / 2;

            if (Math.abs(array[middle][column] - value) < 0.0001) {
                break;
            }

            if (array[middle][column] < value) {
                left = middle + 1;
            } else {
                right = middle - 1;
            }
        }

        if (left > right) {
            throw new ValueNotFoundException("sorry value not found: " + value);
        } else {
            return middle;
        }
    }
}
