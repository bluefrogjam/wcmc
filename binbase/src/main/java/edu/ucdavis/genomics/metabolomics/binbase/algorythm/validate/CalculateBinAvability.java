/*
 * Created on Oct 20, 2003
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate;

import edu.ucdavis.genomics.metabolomics.util.SQLObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author wohlgemuth
 * @version Sep 18, 2003
 * <br>
 * BinBase
 * @description
 */
public class CalculateBinAvability extends SQLObject {
    /**
     * DOCUMENT ME!
     */
    public static final int ALL = 0;

    /**
     * DOCUMENT ME!
     */
    public static final int FINISHED = 1;

    /**
     * DOCUMENT ME!
     */
    public static final int NOT_FINISHED = -1;
    PreparedStatement bin;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement all;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement finished;

    /**
     * DOCUMENT ME!
     */
    private PreparedStatement notFinished;

    /**
     * berechnet die prozentuale h???ufigkeit f???r diesen bin und den angegeben genotypen
     * @param binId Die Id
     * @param classname Der Classname
     * @param mode
     * @return
     */
    public double calculatePercentual(int binId, String classname, int mode)
        throws SQLException {
        switch (mode) {
            case ALL:
                return this.calculate(binId, classname, all);

            case FINISHED:
                return this.calculate(binId, classname, finished);

            case NOT_FINISHED:
                return this.calculate(binId, classname, notFinished);

            default:
                throw new RuntimeException("Error not supported mode: " + mode);
        }
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.util.SQLObject#prepareStatements()
     */
    protected void prepareStatements() throws Exception {
        super.prepareStatements();

        bin = this.getConnection().prepareStatement(SQL_CONFIG.getValue(CLASS +
                    ".bin"));
        all = this.getConnection().prepareStatement(SQL_CONFIG.getValue(CLASS +
                    ".all"));
        finished = this.getConnection().prepareStatement(SQL_CONFIG.getValue(CLASS +
                    ".finished"));
        notFinished = this.getConnection().prepareStatement(SQL_CONFIG.getValue(CLASS +
                    ".notfinished"));
    }

    /**
     * berechnet die werte als solches
     * @param binId
     * @param classname
     * @param statement
     * @return
     * @throws SQLException
     */
    private double calculate(int binId, String classname,
        PreparedStatement statement) throws SQLException {
        statement.setString(1, classname);
        bin.setInt(2, binId);

        ResultSet result = statement.executeQuery();
        int count = 0;
        int binCount = 0;

        while (result.next() == true) {
            bin.setInt(1, result.getInt("sample_id"));

            //bin vorhanden
            if (bin.executeQuery().next() == true) {
                binCount++;
            }

            count++;
        }

        result.close();

        logger.debug("defined: " + count);
        logger.debug("found:   " + binCount);

        try {
            return (double) binCount / (double) count;
        } catch (ArithmeticException e) {
            return 0;
        }
    }
}
