/*
 * Created on Aug 26, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.handler;

import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.Diagnostics;
import edu.ucdavis.genomics.metabolomics.util.SQLable;
import edu.ucdavis.genomics.metabolomics.util.config.SQLConfigable;

import java.util.Map;


/**
 * @author wohlgemuth
 * @version Aug 26, 2003
 * <br>
 * BinBaseDatabase
 * @description
 */
public interface ImportHandler extends Diagnostics {
    /**
     * importiert die sample id in die datenbank
     * @version Aug 26, 2003
     * @author wohlgemuth
     * <br>
     * @param data
     * @throws Exception
     */
    void importSampleMap(Map data) throws Exception;

    /**
     *
     * @version Aug 26, 2003
     * @author wohlgemuth
     * <br>
     * @param data
     * @throws Exception
     * importiert spectren in die datenbank
     */
    void importSpectraMap(Map data) throws Exception;
    
    /**
     * fires a batch insert, if supported
     * @throws Exception
     */
    public void fireBatchInsert() throws Exception;
    
    /**
     * does this handler supports batching
     * @return
     */
    public boolean supportBatchMode();

}
