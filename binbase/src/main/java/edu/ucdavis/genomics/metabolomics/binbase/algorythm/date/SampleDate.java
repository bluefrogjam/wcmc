/*
 * Created on Sep 26, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.date;

import java.util.Date;


/**
 * generates the sample date from a given sample
 *
 * @author wohlgemuth
 *
 */
public abstract class SampleDate {
    /**
     * calculates the date of the sample
     *
     * @return
     */
    public abstract Date getDate();

    /**
     * calcutes the number of the day, like first sample, second sample
     *
     * @return
     */
    public abstract int getNumberOfDay();

    /**
     * the operator name
     * @return
     */
    public abstract String getOperator();
    
    /**
     * machine name
     * @return
     */
    public abstract String getMachine();
    
    /**
     * the run number
     * @return
     */
    public abstract int getRunNumber();
    
    /**
     * creates an instance of the sample date, based on the given name. we try
     * to generate the pattern from this
     *
     * @param sample
     * @return
     */
    public static SampleDate createInstance(String sample) {
        if (sample.matches(MPISampleDate.PATTERN)) {
            return new MPISampleDate(sample);
        } else {
            return new UCSampleDate(sample);
        }
    }

    /**
     * returns the date as sql date
     * @return
     */
    public java.sql.Date getDateAsSQL() {
        Date date = this.getDate();
        java.sql.Date sql = new java.sql.Date(date.getTime());

        return sql;
    }
}
