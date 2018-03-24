package edu.ucdavis.genomics.metabolomics.binbase.bci.server;

import edu.ucdavis.genomics.metabolomics.binbase.bci.server.types.DSL;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

import javax.ejb.Remote;
import java.io.Serializable;

@Remote
public interface BinBaseService extends Serializable {

    long serialVersionUID = 2L;

    /**
     * stores a sample on the server side
     *
     * @param sample
     * @param column
     * @throws BinBaseException
     */
    void storeSample(ExperimentSample sample, String column) throws BinBaseException;

    /**
     * returns all the registered database
     */
    String[] getRegisteredColumns() throws BinBaseException;

    /**
     * returns the specified netcdf file from the server
     */
    byte[] getNetCdfFile(String sampleName) throws BinBaseException;

    /**
     * checks if this result file exists
     *
     * @param sampleName
     * @return
     * @throws BinBaseException
     */
    boolean hasNetCdfFile(String sampleName) throws BinBaseException;

    /**
     * checks if this txt file exist
     *
     * @param sampleName
     * @return
     * @throws BinBaseException
     */
    boolean hasTextFile(String sampleName) throws BinBaseException;

    /**
     * returns the ids of all the available sops
     */
    String[] getAvailableSops() throws BinBaseException;

    /**
     * uploads a new sop to the server
     */
    void uploadSop(String name, String xmlContent) throws BinBaseException;

    /**
     * returns the timestamp for this sample on the harddrive or complains if it
     * wasn't found
     *
     * @param sample
     * @return
     * @throws BinBaseException
     */
    long getTimeStampForSample(String sample) throws BinBaseException;

    void triggerDSLCalculations(DSL dsl)
            throws BinBaseException;

}