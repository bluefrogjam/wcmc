package edu.ucdavis.genomics.metabolomics.util.statistics.data;

import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.DeskriptiveMethod;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ZeroReplaceable;

import java.io.*;
import java.util.Collection;
import java.util.List;

public interface DataFile extends Serializable {

    /**
     * setzt den wert einer zelle an einer bestimmten position
     *
     * @param column
     * @param row
     * @return
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract boolean setCell(int column, int row, Object value);

    /**
     * gibt das object an der position aus
     *
     * @param column
     * @param row
     * @return
     * @version Aug 20, 2003
     * @author wohlgemuth <br>
     */
    public abstract Object getCell(int column, int row);

    /**
     * setzt die daten einer column
     *
     * @param position
     * @param column
     * @version Aug 21, 2003
     * @author wohlgemuth <br>
     */
    public abstract void setColumn(int position, List<?> column);

    /**
     * gibt die angegebene column zur?ck
     *
     * @param position
     * @return
     * @version Aug 21, 2003
     * @author wohlgemuth <br>
     */
    public abstract List<?> getColumn(int position);

    /**
     * gibt die anzahl der columns zur?ck
     *
     * @return
     * @version Aug 21, 2003
     * @author wohlgemuth <br>
     */
    public abstract int getColumnCount();

    /**
     * @return total count of column include ignored columns
     * @author wohlgemuth
     * @version Nov 1, 2005
     */
    public abstract int getTotalColumnCount();

    /**
     * gibt alle daten zur?ck
     *
     * @return
     * @version Aug 20, 2003
     * @author wohlgemuth <br>
     * @uml.property name="data"
     */
    public abstract List<?> getData();

    /**
     * setzt die gr?sse der matrix und f?llt sie mit Double objekten vom wert 0
     *
     * @param row
     * @param columns
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract void setDimension(int row, int columns);

    /**
     * setzt die daten einer row
     *
     * @param position
     * @param row
     * @version Aug 21, 2003
     * @author wohlgemuth <br>
     */
    public abstract void setRow(int position, List<?> row);

    /**
     * gibt die angegebene row zur?ck
     *
     * @param position
     * @return
     * @version Aug 21, 2003
     * @author wohlgemuth <br>
     */
    public abstract List<?> getRow(int position);

    /**
     * gibt die anzahl der rows zur?ck
     *
     * @return
     * @version Aug 21, 2003
     * @author wohlgemuth <br>
     */
    public abstract int getRowCount();

    /**
     * @return total row count includind ignored rows
     * @author wohlgemuth
     * @version Nov 1, 2005
     */
    public abstract int getTotalRowCount();

    /**
     * f?gt eine neue column ein
     *
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.data.Datafile#addColumn(String,
     * List)
     */
    public abstract int addColumn(String name, List<?> column);

    /**
     * f?gt eine lehre columns ein
     *
     * @param label
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract int addEmptyColumn(String label);

    /**
     * f?gt eine lehre s?ule ein
     *
     * @param label
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract void addEmptyRow();

    public abstract void addEmptyRowAtPosition(int position);


    /**
     * f?gt eine neue row ein
     *
     * @param row
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract void addRow(List<?> row);

    /**
     * l?scht eine column
     *
     * @param position
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract void deleteColumn(int position);

    /**
     * l?scht eine zeile
     *
     * @param position
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract void deleteRow(int position);

    /**
     * gibt die positionen der objektes zur?ck, sehr langsam, da linear ?ber die
     * gesamte matrix gesucht wird
     *
     * @param object
     * @return Liste mit Positionsobjekten
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract List<?> findAllObject(Object object);

    /**
     * gibt die position des objektes zur?ck, sehr langsam, da linear ?ber die
     * gesamte matrix gesucht wird
     *
     * @param object
     * @return
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract Position findObject(Object object);

    /**
     * druckt die tabelle mittels des streams aus
     *
     * @param out
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract void print(PrintStream out);

    /**
     * @param read
     * @throws IOException
     * @author wohlgemuth
     * @version Nov 19, 2005
     */
    public abstract void read(InputStream read) throws IOException;

    /**
     * ersetzt alle objekte welche dem object old entspricht gegen das neue
     * objekt
     *
     * @param old
     * @param new_
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract void replaceAll(Object old, Object new_);

    /**
     * gibt alle objekte des datenfiles als liste zur?ck ohne header
     * informationen
     *
     * @return list welche alle daten enth?lt
     * @version Sep 4, 2003
     * @author wohlgemuth <br>
     */
    public abstract List<?> toList();

    /**
     * @param writer
     * @throws IOException
     * @author wohlgemuth
     * @version Nov 19, 2005
     */
    public abstract void write(OutputStream out) throws IOException;

    /**
     * converts the datafile into an inputstream so that we can read it from
     * another application
     *
     * @param writer
     * @throws IOException
     * @author wohlgemuth
     * @version Nov 19, 2005
     */
    public abstract InputStream toInputStream() throws IOException;

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see edu.ucdavis.genomics.metabolomics.util.statistics.data.Datafile#getColumnSize(int)
     */
    public abstract int getColumnSize(int position);

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005 removes all colums which contains less values then
     * "percent"
     */
    public abstract void sizeDown(double percent);

    /**
     * @param column  column used grouping
     * @param group   are we want to group
     * @param percent if smaller then given value column will be removed, if
     *                <p>
     *                a) group = true -> we delete the column if we have in one of the grouping
     *                less values than <percent> b) group = false -> we delete the column if we
     *                have less values than <percent>
     * @author wohlgemuth
     * @version Nov 1, 2005
     */
    public abstract void sizeDown(boolean group, int column, double percent);

    /**
     * returns all groups created by the given column
     *
     * @param column
     * @return
     * @author wohlgemuth
     * @version Nov 21, 2005
     */
    public abstract Collection<?> getGroups(int column);

    public abstract int[] getIgnoreColumns();

    /**
     * which columns are ignored
     *
     * @param ignoreColumns
     * @author wohlgemuth
     * @version Nov 21, 2005
     */
    public abstract void setIgnoreColumns(int[] ignoreColumns);

    public abstract int[] getIgnoreRows();

    /**
     * which rows are ignored
     *
     * @param ignoreRows
     * @author wohlgemuth
     * @version Nov 21, 2005
     */
    public abstract void setIgnoreRows(int[] ignoreRows);

    /**
     * returns all rows of the given class
     *
     * @param group  group by modifier
     * @param column the column to use for grouping
     * @return
     * @author wohlgemuth
     * @version Nov 19, 2005
     */
    public abstract List<?> findRowsByGroup(Object group, int column);

    /**
     * replace zeros over the hole dataset based on bins
     *
     * @param replace
     * @author wohlgemuth
     * @version Nov 21, 2005
     */
    public void replaceZeros(ZeroReplaceable replace);

    /**
     * replace zeros based on sample/bin
     *
     * @param replace
     * @param sample  true = sample, false = bin mode
     * @author wohlgemuth
     * @version Jul 7, 2006
     */
    public void replaceZeros(ZeroReplaceable replace, boolean sample);

    /**
     * replace zeros based on the groups
     *
     * @param replace
     * @param columnToGroupBy the grouping column
     * @author wohlgemuth
     * @version Nov 21, 2005
     */
    public void replaceZeros(ZeroReplaceable replace, int columnToGroupBy);

    /**
     * clones this object
     *
     * @return
     * @throws CloneNotSupportedException
     * @author wohlgemuth
     * @version Nov 22, 2005
     */
    public Object clone() throws CloneNotSupportedException;

    /**
     * runs a desscriptive method and returns the result for a specific column
     *
     * @param column    on which column
     * @param statistic what kind of statistical method
     * @return the calculated result
     * @author wohlgemuth
     * @version Nov 29, 2005
     */
    public abstract double runStatisticalMethod(int column, DeskriptiveMethod statistic);

    /**
     * runs a desscriptive method over the hole dataset and returns the result
     *
     * @param statistic what kind of statistical method
     * @return the calculated result
     * @author wohlgemuth
     * @version Nov 29, 2005
     */
    public abstract double runStatisticalMethod(DeskriptiveMethod statistic);

    /**
     * runs a desscriptive method and returns the result
     *
     * @param column         on which column
     * @param groupingColumn which column is used for grouping
     * @param statistic      what kind of statistical method
     * @return the calculated results for the given groups
     * @author wohlgemuth
     * @version Nov 29, 2005
     */
    public abstract double[] runStatisticalMethod(int column, int groupingColumn, DeskriptiveMethod statistic);

    /**
     * combines columns, the implementation needs to select the columns by it
     * selfs
     *
     * @param combine
     * @author wohlgemuth
     * @version Jun 27, 2006
     */
    public void combineColumns(ColumnCombiner combine);

    /**
     * combines columns, the list contatins the columns to combine
     *
     * @param combine
     * @author wohlgemuth
     * @version Jun 27, 2006
     */
    public void combineColumns(int ids[], ColumnCombiner combine);

    public int addEmptyColumn(String label, int position);

    boolean ignoreColumn(final int i);

    boolean ignoreRow(final int i);

    /**
     * defines that this column will be ignored
     *
     * @param i
     * @param ignore
     */
    public void setIgnoreColumn(int i, boolean ignore);

    /**
     * definers that this row will be ignored
     *
     * @param i
     * @param ignore
     */
    public void setIgnoreRow(int i, boolean ignore);

    /**
     * adds anoter row with the contents of this
     *
     * @param cell
     */
    public void addRow(Object... cell);


}
