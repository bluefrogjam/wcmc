/*
 * Created on 12.05.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.ucdavis.genomics.metabolomics.util.math;


/**
 * @author wohlgemuth
 * <p>
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface SpectraArrayKey {
    /**
     * Definiert die weite eines spectrums
     */
    int ARRAY_WIDTH = 3;

    /**
     * Definiert die Position f???r die Absolute Abundance
     */
    int FRAGMENT_ABS_POSITION = 1;

    /**
     * Definiert die Position f???r die Fragmente
     */
    int FRAGMENT_ION_POSITION = 0;

    /**
     * Definiert die Position f???r die Relative Abundance
     */
    int FRAGMENT_REL_POSITION = 2;

    /**
     * Definiert die maximal gr???sse eines spectrums
     */
    int MAX_ION = 1000;

}
