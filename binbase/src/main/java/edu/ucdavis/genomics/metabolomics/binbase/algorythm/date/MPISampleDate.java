/*
 * Created on Sep 26, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.date;

import java.util.Calendar;
import java.util.Date;


/**
 * converts the mpi sample date
 * @author wohlgemuth
 *
 */
public class MPISampleDate extends SampleDate {
    /**
     *
     */
    public static final String PATTERN = "[0-9][0-9][0-9][0-9][a-z][a-z][0-9][0-9]_[0-9]";
    Date date;
    int number;

    String operator;
    
    String machine;

    int runNumber;
    
    /**
     * @param sample
     */
    public MPISampleDate(String sample) {
        if (sample.matches(PATTERN)) {
            int year = Integer.parseInt("200" + sample.substring(0, 1));
            int doy = Integer.parseInt(sample.substring(1, 4));
            number = Integer.parseInt(sample.substring(6, 8));
            operator = sample.substring(5,6);
            machine = sample.substring(4,5);
            runNumber = Integer.parseInt(sample.substring(9));
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.DAY_OF_YEAR, doy);

            date = c.getTime();
        } else {
            throw new RuntimeException("pattern didn't match this sample");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Date getDate() {
        return date;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getNumberOfDay() {
        return number;
    }

	@Override
	public String getMachine() {
		return machine;
	}

	@Override
	public String getOperator() {
		return operator;
	}

	@Override
	public int getRunNumber() {
		return runNumber;
	}
}
