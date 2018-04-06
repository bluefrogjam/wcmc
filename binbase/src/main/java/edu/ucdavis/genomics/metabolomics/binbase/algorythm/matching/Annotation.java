/*
 * Created on 01.10.2004
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.AlgorythmHandler;


/**
 * bechreibt das grundlegene matching und soll dazu dienen die alten mpi klassen im laufe der zeit zu ersetzen
 * und zu verbessern
 * @author wohlgemuth
 */
public interface Annotation {
    /**
     * setzt den algorythmushandler
     * @see Matchable#setAlgorythmHandler(edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.AlgorythmHandler)
     */
    void setAlgorythmHandler(AlgorythmHandler handler);

    /**
     * setzt das sample
     * @see Matchable#setSampleId(int)
     */
    void setSampleId(int id);

    /**
     * startet die anotation
     * @see edu.ucdavis.genomics.metabolomics.binbase.binlib.algorythm.matching.DefaultMatching#run()
     */
    int run();
}
