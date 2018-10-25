package edu.ucdavis.genomics.metabolomics.binbase.algorythm.util;

/**
 * simple sample callback to let others know a sample got processed
 */
public interface SampleCallback {
    void processed(int sampleId, boolean success);
}
