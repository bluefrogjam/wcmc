package edu.ucdavis.genomics.metabolomics.util.statistics.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.DeskriptiveMethod;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ZeroReplaceable;

public interface DataFile extends Serializable {

	/**
	 * setzt den wert einer zelle an einer bestimmten position
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @param column
	 * @param row
	 * @return 
	 */
	public abstract boolean setCell(int column, int row, Object value);

	/**
	 * gibt das object an der position aus
	 * 
	 * @version Aug 20, 2003
	 * @author wohlgemuth <br>
	 * @param column
	 * @param row
	 * @return
	 */
	public abstract Object getCell(int column, int row);

	/**
	 * setzt die daten einer column
	 * 
	 * @version Aug 21, 2003
	 * @author wohlgemuth <br>
	 * @param position
	 * @param column
	 */
	public abstract void setColumn(int position, List<?> column);

	/**
	 * gibt die angegebene column zur?ck
	 * 
	 * @version Aug 21, 2003
	 * @author wohlgemuth <br>
	 * @param position
	 * @return
	 */
	public abstract List<?> getColumn(int position);

	/**
	 * gibt die anzahl der columns zur?ck
	 * 
	 * @version Aug 21, 2003
	 * @author wohlgemuth <br>
	 * @return
	 */
	public abstract int getColumnCount();

	/**
	 * @author wohlgemuth
	 * @version Nov 1, 2005
	 * @return total count of column include ignored columns
	 */
	public abstract int getTotalColumnCount();

	/**
	 * gibt alle daten zur?ck
	 * 
	 * @version Aug 20, 2003
	 * @author wohlgemuth <br>
	 * @return
	 * 
	 * @uml.property name="data"
	 */
	public abstract List<?> getData();

	/**
	 * setzt die gr?sse der matrix und f?llt sie mit Double objekten vom wert 0
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @param row
	 * @param columns
	 */
	public abstract void setDimension(int row, int columns);

	/**
	 * setzt die daten einer row
	 * 
	 * @version Aug 21, 2003
	 * @author wohlgemuth <br>
	 * @param position
	 * @param row
	 */
	public abstract void setRow(int position, List<?> row);

	/**
	 * gibt die angegebene row zur?ck
	 * 
	 * @version Aug 21, 2003
	 * @author wohlgemuth <br>
	 * @param position
	 * @return
	 */
	public abstract List<?> getRow(int position);

	/**
	 * gibt die anzahl der rows zur?ck
	 * 
	 * @version Aug 21, 2003
	 * @author wohlgemuth <br>
	 * @return
	 */
	public abstract int getRowCount();

	/**
	 * @author wohlgemuth
	 * @version Nov 1, 2005
	 * @return total row count includind ignored rows
	 */
	public abstract int getTotalRowCount();

	/**
	 * f?gt eine neue column ein
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @see edu.ucdavis.genomics.metabolomics.binbase.utils.statistics.data.Datafile#addColumn(String,
	 *      List)
	 */
	public abstract int addColumn(String name, List<?> column);

	/**
	 * f?gt eine lehre columns ein
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @param label
	 */
	public abstract int addEmptyColumn(String label);

	/**
	 * f?gt eine lehre s?ule ein
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @param label
	 */
	public abstract void addEmptyRow();

	public abstract void addEmptyRowAtPosition(int position);

	
	/**
	 * f?gt eine neue row ein
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @param row
	 */
	public abstract void addRow(List<?> row);

	/**
	 * l?scht eine column
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @param position
	 */
	public abstract void deleteColumn(int position);

	/**
	 * l?scht eine zeile
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @param position
	 */
	public abstract void deleteRow(int position);

	/**
	 * gibt die positionen der objektes zur?ck, sehr langsam, da linear ?ber die
	 * gesamte matrix gesucht wird
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @param object
	 * @return Liste mit Positionsobjekten
	 */
	public abstract List<?> findAllObject(Object object);

	/**
	 * gibt die position des objektes zur?ck, sehr langsam, da linear ?ber die
	 * gesamte matrix gesucht wird
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @param object
	 * @return
	 */
	public abstract Position findObject(Object object);

	/**
	 * druckt die tabelle mittels des streams aus
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @param out
	 */
	public abstract void print(PrintStream out);

	/**
	 * 
	 * @author wohlgemuth
	 * @version Nov 19, 2005
	 * @param read
	 * @throws IOException
	 */
	public abstract void read(InputStream read) throws IOException;

	/**
	 * ersetzt alle objekte welche dem object old entspricht gegen das neue
	 * objekt
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @param old
	 * @param new_
	 */
	public abstract void replaceAll(Object old, Object new_);

	/**
	 * gibt alle objekte des datenfiles als liste zur?ck ohne header
	 * informationen
	 * 
	 * @version Sep 4, 2003
	 * @author wohlgemuth <br>
	 * @return list welche alle daten enth?lt
	 */
	public abstract List<?> toList();

	/**
	 * 
	 * @author wohlgemuth
	 * @version Nov 19, 2005
	 * @param writer
	 * @throws IOException
	 */
	public abstract void write(OutputStream out) throws IOException;

	/**
	 * converts the datafile into an inputstream so that we can read it from
	 * another application
	 * 
	 * @author wohlgemuth
	 * @version Nov 19, 2005
	 * @param writer
	 * @throws IOException
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
	 *          "percent"
	 */
	public abstract void sizeDown(double percent);

	/**
	 * @author wohlgemuth
	 * @version Nov 1, 2005
	 * @param column
	 *            column used grouping
	 * @param group
	 *            are we want to group
	 * @param percent
	 *            if smaller then given value column will be removed, if
	 * 
	 * a) group = true -> we delete the column if we have in one of the grouping
	 * less values than <percent> b) group = false -> we delete the column if we
	 * have less values than <percent>
	 * 
	 */
	public abstract void sizeDown(boolean group, int column, double percent);

	/**
	 * returns all groups created by the given column
	 * 
	 * @author wohlgemuth
	 * @version Nov 21, 2005
	 * @param column
	 * @return
	 */
	public abstract Collection<?> getGroups(int column);

	public abstract int[] getIgnoreColumns();

	/**
	 * which columns are ignored
	 * 
	 * @author wohlgemuth
	 * @version Nov 21, 2005
	 * @param ignoreColumns
	 */
	public abstract void setIgnoreColumns(int[] ignoreColumns);

	public abstract int[] getIgnoreRows();

	/**
	 * which rows are ignored
	 * 
	 * @author wohlgemuth
	 * @version Nov 21, 2005
	 * @param ignoreRows
	 */
	public abstract void setIgnoreRows(int[] ignoreRows);

	/**
	 * 
	 * returns all rows of the given class
	 * 
	 * @author wohlgemuth
	 * @version Nov 19, 2005
	 * @param group
	 *            group by modifier
	 * @param column
	 *            the column to use for grouping
	 * @return
	 */
	public abstract List<?> findRowsByGroup(Object group, int column);

	/**
	 * replace zeros over the hole dataset based on bins
	 * 
	 * @author wohlgemuth
	 * @version Nov 21, 2005
	 * @param replace
	 */
	public void replaceZeros(ZeroReplaceable replace);

	/**
	 * replace zeros based on sample/bin
	 * @author wohlgemuth
	 * @version Jul 7, 2006
	 * @param replace
	 * @param sample true = sample, false = bin mode
	 */
	public void replaceZeros(ZeroReplaceable replace, boolean sample);

	/**
	 * replace zeros based on the groups
	 * 
	 * @author wohlgemuth
	 * @version Nov 21, 2005
	 * @param replace
	 * @param columnToGroupBy
	 *            the grouping column
	 */
	public void replaceZeros(ZeroReplaceable replace, int columnToGroupBy);

	/**
	 * clones this object
	 * 
	 * @author wohlgemuth
	 * @version Nov 22, 2005
	 * @return
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException;

	/**
	 * runs a desscriptive method and returns the result for a specific column
	 * 
	 * @author wohlgemuth
	 * @version Nov 29, 2005
	 * @param column
	 *            on which column
	 * @param statistic
	 *            what kind of statistical method
	 * @return the calculated result
	 */
	public abstract double runStatisticalMethod(int column, DeskriptiveMethod statistic);

	/**
	 * runs a desscriptive method over the hole dataset and returns the result
	 * 
	 * @author wohlgemuth
	 * @version Nov 29, 2005
	 * @param statistic
	 *            what kind of statistical method
	 * @return the calculated result
	 */
	public abstract double runStatisticalMethod(DeskriptiveMethod statistic);

	/**
	 * runs a desscriptive method and returns the result
	 * 
	 * @author wohlgemuth
	 * @version Nov 29, 2005
	 * @param column
	 *            on which column
	 * @param groupingColumn
	 *            which column is used for grouping
	 * @param statistic
	 *            what kind of statistical method
	 * @return the calculated results for the given groups
	 */
	public abstract double[] runStatisticalMethod(int column, int groupingColumn, DeskriptiveMethod statistic);

	/**
	 * combines columns, the implementation needs to select the columns by it
	 * selfs
	 * 
	 * @author wohlgemuth
	 * @version Jun 27, 2006
	 * @param combine
	 */
	public void combineColumns(ColumnCombiner combine);

	/**
	 * combines columns, the list contatins the columns to combine
	 * 
	 * @author wohlgemuth
	 * @version Jun 27, 2006
	 * @param combine
	 */
	public void combineColumns(int ids[], ColumnCombiner combine);

	public int addEmptyColumn(String label, int position);

	 boolean ignoreColumn(final int i) ;
	 
	 boolean ignoreRow(final int i) ;
	 
	 /**
	  * defines that this column will be ignored
	  * @param i
	  * @param ignore
	  */
	 public void setIgnoreColumn(int i, boolean ignore);
	 
	 /**
	  * definers that this row will be ignored
	  * @param i
	  * @param ignore
	  */
	 public void setIgnoreRow(int i, boolean ignore);

	/**
	 * adds anoter row with the contents of this
	 * @param cell
	 */
	public void addRow(Object... cell);
	 
	 
	 
}