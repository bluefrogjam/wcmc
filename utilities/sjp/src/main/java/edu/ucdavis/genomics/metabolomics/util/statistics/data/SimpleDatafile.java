/*
 * Created on Aug 20, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.data;

import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.DeskriptiveMethod;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.NonZeroMin;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ZeroReplaceable;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.NullObject;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * @author wohlgemuth
 * @version Nov 1, 2005
 */
public class SimpleDatafile implements DataFile {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -2506102493489642191L;

    /**
     * contains postion with ignored rows wich will not be used for calculation
     */
    private int[] ignoreRows = new int[]{};

    /**
     * contains postion with ignored columns wich will not be used for
     * calculation
     */
    private int[] ignoreColumns = new int[]{};

    /**
     * contains all data
     */
    private List<List<?>> data = new ArrayList<List<?>>();

    private int columnCount = 0;

    /**
     * default constructor creates an empty datafile
     *
     * @author wohlgemuth
     * @version Nov 22, 2005
     */
    public SimpleDatafile() {
        final Logger logger = LoggerFactory.getLogger(getClass());
        logger.debug("using for storage: " + data.getClass());
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#addColumn(String,
     * List)
     */
    public int addColumn(final String name, final List<?> column) {
        if (getRowCount() == column.size()) {
            return this.addEmptyColumn(name);
        } else {
            throw new RuntimeException("column length must equals rowcount!");
        }
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#addEmptyColumn(String)
     */
    public int addEmptyColumn() {
        if (getRowCount() == 0) {
            throw new RuntimeException("you must defined a dimension first!");
        }

        for (int i = 0; i < data.size(); i++) {
            ((List) data.get(i)).add(new Double(0));
        }

        final int position = columnCount;
        columnCount++;
        return position;
    }

    /**
     * do we want to ignore this column
     *
     * @param position
     * @param ignore
     */
    public void ignoreColumn(int position, boolean ignore) {

        Set<Integer> columns = new HashSet<Integer>();

        for (int i : this.ignoreColumns) {
            columns.add(i);
        }

        if (ignore) {
            columns.add(position);
        } else {
            columns.remove(position);
        }

        this.ignoreColumns = ArrayUtils.toPrimitive(columns
            .toArray(new Integer[0]));
    }

    public int addEmptyColumn(Object filler) {
        if (getRowCount() == 0) {
            throw new RuntimeException("you must defined a dimension first!");
        }

        for (int i = 0; i < data.size(); i++) {
            ((List) data.get(i)).add(filler);
        }

        final int position = columnCount;
        columnCount++;
        return position;
    }

    /**
     * @return
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#addEmptyColumn(String)
     */
    public int addEmptyColumn(final String label) {
        if (getRowCount() == 0) {
            List list = new Vector();
            this.addRow(list);
            // throw new
            // RuntimeException("you must defined a dimension first!");
        }

        for (int i = 0; i < data.size(); i++) {
            ((List) data.get(i)).add(label);
        }

        final int position = columnCount;
        columnCount++;
        return position;
    }

    /**
     * adds an empty coulm at the given position
     *
     * @param labe
     * @param position
     */
    public int addEmptyColumn(String label, int position) {
        if (getRowCount() == 0) {
            List list = new Vector();
            this.addRow(list);
            // throw new
            // RuntimeException("you must defined a dimension first!");
        }

        for (int i = 0; i < data.size(); i++) {
            ((List) data.get(i)).add(position, label);
        }

        columnCount++;

        for (int i = 0; i < this.ignoreColumns.length; i++) {
            if (this.ignoreColumns[i] >= position) {
                this.ignoreColumns[i]++;
            }
        }

        return position;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#addEmptyRow(String)
     */
    public void addEmptyRow() {
        final List row = new Vector();

        if (getColumnCount() == 0) {
            throw new RuntimeException("you must defined a dimension first!");
        }

        for (int i = 0; i < getTotalColumnCount(); i++) {
            row.add(new Double("0"));
        }

        data.add(row);
    }

    /**
     * adds an empty coulm at the given position
     *
     * @param labe
     * @param position
     */
    public void addEmptyRowAtPosition(int position) {
        final List row = new Vector();

        if (getColumnCount() == 0) {
            throw new RuntimeException("you must defined a dimension first!");
        }

        for (int i = 0; i < getTotalColumnCount(); i++) {
            row.add(new Double(0));
        }

        data.add(position, row);

        for (int i = 0; i < this.ignoreRows.length; i++) {
            if (this.ignoreRows[i] >= position) {
                this.ignoreRows[i]++;
            }
        }

    }

    public void addEmptyRow(Object filler) {
        final List row = new Vector();

        if (getColumnCount() == 0) {
            throw new RuntimeException("you must defined a dimension first!");
        }

        for (int i = 0; i < getTotalColumnCount(); i++) {
            row.add(filler);
        }

        data.add(row);
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#addRow(String,
     * List)
     */
    public void addRow(final List row) {

        if (getTotalColumnCount() == 0) {
            data.add(row);
            columnCount = row.size();
        } else if (getTotalColumnCount() == row.size()) {
            data.add(row);

        } else {
            throw new RuntimeException("row length must equals columncount!");
        }

    }

    /**
     * clones the complete datafile
     *
     * @author wohlgemuth
     * @version Nov 22, 2005
     * @see Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();

            out = new ObjectOutputStream(bos);
            out.writeObject(this);

            out.flush();
            out.close();

            in = new ObjectInputStream(new ByteArrayInputStream(
                bos.toByteArray()));
            final SimpleDatafile file = (SimpleDatafile) in.readObject();
            in.close();
            return file;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new CloneNotSupportedException(e.getMessage());
        }
    }

    public void combineColumns(final ColumnCombiner combine) {
        // doesnt do anything
    }

    /**
     * combines the columns with the given combiner
     *
     * @author wohlgemuth
     * @version Jun 28, 2006
     * @see DataFile#combineColumns(int[],
     * ColumnCombiner)
     */
    public void combineColumns(final int ids[], final ColumnCombiner combine) {
        final int size = ids.length;
        int length = 0;

        // validate that all have the same length
        if ((size >= 1) == false) {
            throw new RuntimeException("we need at least 2 columns");
        }

        int mainColumn = ids[0];

        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                if (getColumn((ids[i])).size() != length) {
                    throw new RuntimeException(
                        "sorry all columns need to have the same length");
                }
            } else {
                length = getColumn((ids[i])).size();
                mainColumn = ids[i];
            }
        }

        // replace data
        for (int i = 0; i < length; i++) {
            final List data = new Vector();

            final boolean skip = skipRowIndex(i);

            if (skip == false) {

                for (int x = 0; x < size; x++) {
                    data.add(getColumn(ids[x]).get(i));
                }

                final Object result = combine.combine(data);
                // LoggerFactory.getLogger(getClass()).debug("pbject after combining: "
                // + result);
                setCell(mainColumn, i, result);
            }
        }

        // delete columns
        int counter = 0;

        // keep the first column
        for (final int id : ids) {
            // ignore the rows
            boolean skip = false;

            for (int x = 0; x < ignoreColumns.length; x++) {
                if (id == ignoreColumns[x]) {
                    skip = true;
                    x = ignoreColumns.length;
                }
            }

            if (skip == false) {
                getLogger().trace(
                    "attempting to delete column with id: " + id
                        + " and our delete counter is " + counter);
                if (id - counter == mainColumn) {
                    // is our main column we want to keep it

                    getLogger().trace("it's our main column - so we ignore it");
                } else {
                    getLogger().trace("deleting column " + (id - counter));
                    deleteColumn(id - counter);

                    // in case our main column is not the first column we need
                    // to reduce it
                    if (id < mainColumn) {
                        mainColumn = mainColumn - 1;
                        getLogger().trace("main column is now: " + mainColumn);
                    }
                    counter++;
                }
            }
        }
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#deleteColumn(int)
     */
    public void deleteColumn(final int position) {

        for (final int ignoreColumn : ignoreColumns) {
            if (ignoreColumn == position) {
                getLogger().warn(
                    "can't remove column " + position
                        + " => it's in the ignore list!");
                return;
            }
        }

        // algorythmn to shift the ignore columns right
        for (int x = 0; x < ignoreColumns.length; x++) {
            if (ignoreColumns[x] == 0) {
                // ignore its already to the left
            } else {
                // check if the column to the left is a column to ignore
                if (x > 0) {
                    if (ignoreColumns[x - 1] == (ignoreColumns[x] - 1)) {
                        // ignore this column too
                    } else {
                        ignoreColumns[x] = ignoreColumns[x] - 1;
                    }
                } else {
                    ignoreColumns[x] = ignoreColumns[x] - 1;
                }
            }

        }

        for (int i = 0; i < getTotalRowCount(); i++) {
            ((List) data.get(i)).remove(position);

        }

        columnCount--;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#deleteRow(int)
     */
    public void deleteRow(final int position) {

        for (final int ignoreRow : ignoreRows) {
            if (ignoreRow == position) {
                return;
            }
        }

        // algorythmn to shift the ignore rows right
        for (int x = 0; x < ignoreRows.length; x++) {
            if (ignoreRows[x] == 0) {
                // ignore its already to the left
            } else {
                // check if the column to the left is a rows to ignore
                if (x > 0) {
                    if (ignoreRows[x - 1] == (ignoreRows[x] - 1)) {
                        // ignore this rows too
                    } else {
                        ignoreRows[x] = ignoreRows[x] - 1;
                    }
                } else {
                    ignoreRows[x] = ignoreRows[x] - 1;
                }
            }

        }
        data.remove(position);
    }

    @Override
    protected void finalize() throws Throwable {
        data.clear();
        data = null;
        super.finalize();
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#findAllObject(Object)
     */
    public List findAllObject(final Object object) {
        final List result = new Vector();

        for (int i = 0; i < data.size(); i++) {
            final List list = (List) data.get(i);

            for (int x = 0; x < list.size(); x++) {
                if (object.equals(list.get(x)) == true) {
                    final Position pos = new Position();
                    pos.column = x;
                    pos.row = i;

                    result.add(pos);
                }
            }
        }

        return result;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#findObject(Object)
     */
    public Position findObject(final Object object) {

        for (int i = 0; i < data.size(); i++) {
            final List list = (List) data.get(i);

            for (int x = 0; x < list.size(); x++) {
                if (object.equals(list.get(x)) == true) {
                    final Position pos = new Position();
                    pos.column = x;
                    pos.row = i;
                    return pos;
                }
            }
        }

        throw new RuntimeException("object not found!");
    }

    /**
     * looks the row positions for the given group up
     *
     * @param group       the group to search
     * @param groupColumn the column of the group definitions
     * @return the position
     */
    public List findRowsByGroup(final Object group, final int groupColumn) {
        final List list = getColumn(groupColumn);
        final List result = new Vector();

        for (int i = 0; i < list.size(); i++) {
            final Object o = list.get(i);
            if (o.equals(group)) {
                final Position p = new Position();
                p.column = groupColumn;
                p.row = i;
                result.add(p);
            }
        }
        return result;
    }

    /**
     * generates a list representing a sample
     *
     * @param i
     * @param list
     * @return
     */
    protected synchronized List generateListOfValuesForReplacement(final int i,
                                                                   final List list) {
        boolean ignore;
        final List toReplace = new ArrayList(list.size());

        // get columns to ignore
        for (int x = 0; x < getTotalColumnCount(); x++) {
            ignore = false;

            for (int z = 0; z < getIgnoreColumns().length; z++) {
                if (ignoreColumns[z] == x) {
                    ignore = true;
                    z = 2 * ignoreColumns.length;
                }
            }

            if (ignore == false) {
                LoggerFactory.getLogger(getClass()).trace(
                    i + "/" + x + " - add to replace list: " + list.get(x));

                toReplace.add(list.get(x));
            }
        }
        return toReplace;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#getCell(int,
     * int)
     */
    public synchronized Object getCell(final int column, final int row) {

        final Object returnObject = getRow(row).get(column);

        if (returnObject == null) {
            return 0;
        }
        return returnObject;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#getColumn(int)
     */
    public List getColumn(final int position) {

        final List temp = new ArrayList(data.size());

        for (int i = 0; i < data.size(); i++) {
            temp.add(((List) data.get(i)).get(position));
        }
        return temp;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#getColumnCount()
     */
    public int getColumnCount() {
        try {
            // int returnint = ((List) this.data.get(0)).size();
            int returnint = columnCount;
            returnint = returnint - ignoreColumns.length;
            return returnint;
        } catch (final ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#getColumnSize(int)
     */
    public int getColumnSize(final int position) {

        final int returnint = getColumn(position).size();
        return returnint;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#getData()
     */
    public List<List<?>> getData() {
        return data;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#getGroups(int)
     */
    public Collection getGroups(final int column) {

        final List groups = getColumn(column);

        final Collection result = new ArrayList();

        final Iterator it = groups.iterator();

        int i = 0;
        while (it.hasNext()) {
            final Object o = it.next();

            final boolean skip = skipRowIndex(i);
            i++;

            if (!skip) {
                if (result.contains(o) == false) {
                    result.add(o);
                }
            }
        }

        return result;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#getIgnoreColumns()
     */
    public int[] getIgnoreColumns() {
        return ignoreColumns;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#getIgnoreRows()
     */
    public int[] getIgnoreRows() {
        return ignoreRows;
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger("datafile");
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#getRow(int)
     */
    public List getRow(final int position) {
        final List returnList = (List) data.get(position);
        return returnList;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#getRowCount()
     */
    public int getRowCount() {

        int returnint = data.size();

        returnint = returnint - ignoreRows.length;
        return returnint;
    }

    public int getTotalColumnCount() {
        try {
            // if(this.getData().get(0) == null){
            // return 0;
            // }
            // return ((List) this.data.get(0)).size();

            return columnCount;
        } catch (final Exception e) {
            return 0;
        }
    }

    public int getTotalRowCount() {
        return getData().size();
    }

    public boolean ignoreColumn(final int i) {
        boolean ignore = false;

        for (final int ignoreColumn : ignoreColumns) {
            if (ignoreColumn == i) {
                ignore = true;
            }
        }
        return ignore;
    }

    public boolean ignoreRow(final int i) {
        boolean ignore = false;

        for (int x = 0; x < getIgnoreRows().length; x++) {
            if (ignoreRows[x] == i) {
                ignore = true;
            }
        }
        return ignore;
    }

    /**
     * is the given object = null
     *
     * @param o
     * @return
     */
    public boolean isZero(final Object o) {
        try {
            if (o instanceof NullObject) {
                return true;
            }
            if (o instanceof Number) {
                if (((Number) o).doubleValue() == 0) {
                    return true;
                }
            } else if (o == null) {
                return true;
            } else if (o instanceof String) {
                final String s = (String) o;
                if (s.length() == 0) {
                    return true;
                } else if (s.equals("null")) {
                    return true;
                } else if (Double.parseDouble(o.toString()) == 0) {
                    return true;
                }
            } else if (o instanceof FormatObject) {
                return isZero(((FormatObject) o).getValue());
            }
        } catch (final NumberFormatException e) {
        }
        return false;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#print(PrintStream)
     */
    public void print(final PrintStream out) {

        for (int i = 0; i < data.size(); i++) {
            final List list = (List) data.get(i);
            for (int x = 0; x < list.size(); x++) {
                out.print(list.get(x) + "\t");
            }

            out.print("\n");
        }

        out.flush();
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#read(File)
     */
    public void read(final File file) throws IOException {
        this.read(new FileReader(file));
    }

    public void read(final InputStream read) throws IOException {
        this.read(new InputStreamReader(read));
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#read(Reader)
     */
    public void read(final Reader read) throws IOException {

        final BufferedReader reader = new BufferedReader(read);
        String line = null;

        while ((line = reader.readLine()) != null) {
            final String[] tempData = line.split("\t");
            final List temp = new ArrayList(tempData.length);

            if (tempData.length > 0) {
                columnCount = tempData.length;
                for (final String element : tempData) {
                    try {
                        final Double value = new Double(element);
                        temp.add(value);
                    } catch (final NumberFormatException e) {
                        temp.add(element);
                    }
                }

                data.add(temp);
            }
        }
        reader.close();
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#replaceAll(Object,
     * Object)
     */
    public void replaceAll(final Object old, final Object new_) {

        final List temp = findAllObject(old);

        for (int i = 0; i < temp.size(); i++) {
            final Position pos = (Position) temp.get(i);
            setCell(pos.column, pos.row, new_);
        }
    }

    /**
     * replace action based on clumns
     *
     * @param replace
     */
    protected void replaceColumnBased(final ZeroReplaceable replace) {
        for (int i = 0; i < getTotalColumnCount(); i++) {
            boolean ignore = ignoreColumn(i);

            // ok column was accepted
            if (ignore == false) {
                // get column
                final List list = getColumn(i);
                final List toReplace = new ArrayList(getTotalRowCount());

                // get rows to ignore
                for (int x = 0; x < getTotalRowCount(); x++) {

                    ignore = ignoreRow(x);

                    if (ignore == false) {
                        toReplace.add(list.get(x));
                    }
                }

                // replace the zeros
                final List result = replace.replaceZeros(toReplace);

                writeBackToColumn(i, result);
            }
        }
    }

    /**
     * replace action based on rows
     *
     * @param replace
     */
    protected void replaceRowBased(final ZeroReplaceable replace) {
        LoggerFactory.getLogger(getClass()).debug(
            "replacing zeros with: " + replace.toString());

        // ignore given columns
        for (int i = 0; i < getTotalRowCount(); i++) {
            final boolean ignore = ignoreRow(i);

            // ok row was accepted
            if (ignore == false) {
                LoggerFactory.getLogger(getClass()).debug("replacing line: " + i);
                // get row
                final List toReplace = generateListOfValuesForReplacement(i,
                    getRow(i));

                // replace the zeros
                final List result = replace.replaceZeros(toReplace);

                writeBackToRow(i, result);
            }
        }
    }

    public final void replaceZeros(final ZeroReplaceable replace) {
        replaceZeros(replace, false);
    }

    public void replaceZeros(final ZeroReplaceable replace, final boolean sample) {
        if (sample) {
            replaceRowBased(replace);
        } else {
            // replace it based on bins

            // ignore given columns
            replaceColumnBased(replace);
        }
    }

    /**
     * creates groups based on the columns and replace the zero values groupwise
     *
     * @author wohlgemuth
     * @version Nov 21, 2005
     * @see DataFile#replaceZeros(edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ZeroReplaceable,
     * int)
     */
    public void replaceZeros(final ZeroReplaceable replace,
                             final int columnToGroupBy) {
        // the all groups
        final Collection groups = getGroups(columnToGroupBy);

        // replace NAN with smallest value from the datafile
        final double smallest = this.runStatisticalMethod(new NonZeroMin());

        // ignore given columns
        for (int i = 0; i < getTotalColumnCount(); i++) {
            boolean ignore = ignoreColumn(i);

            // ok column was accepted
            if (ignore == false) {

                final Iterator it = groups.iterator();

                // go throw the given groups
                while (it.hasNext()) {
                    // create empty list
                    final List toReplace = new ArrayList();

                    final Object group = it.next();
                    // get the positions of the current group rows
                    final List position = findRowsByGroup(group,
                        columnToGroupBy);

                    // get rows to ignore
                    for (int x = 0; x < position.size(); x++) {
                        ignore = false;

                        for (int z = 0; z < getIgnoreRows().length; z++) {
                            if (ignoreRows[z] == ((Position) position.get(x)).row) {
                                ignore = true;
                                z = 2 * ignoreRows.length;
                            }
                        }

                        if (ignore == false) {
                            // get the actual column

                            toReplace.add(getCell(i,
                                ((Position) position.get(x)).row));
                        }
                    }
                    // replace the zeros in the current group
                    // result contains the new grouped column values
                    final List result = replace.replaceZeros(toReplace);

                    final List finalResult = new ArrayList(result.size());

                    // in case of an error we want to have the smallest possible
                    // value of a double, because is a irrealistic value
                    final double error = Double.MIN_VALUE;

                    final Iterator itx = result.iterator();

                    while (itx.hasNext()) {
                        Object o = itx.next();
                        FormatObject ob = null;
                        if (o instanceof FormatObject) {
                            ob = (FormatObject) o;
                            o = ob.getValue();
                        }
                        double value = 0;
                        // ok make a doulbe out of this
                        try {
                            value = Double.parseDouble(o.toString());
                        } catch (final Exception e) {
                            value = error;
                        }

                        if (Double.isNaN(value)) {
                            value = smallest;
                        } else if (Double.isInfinite(value)) {
                            value = smallest;
                        }

                        if (ob != null) {
                            ob.setValue(value);
                            finalResult.add(ob);
                        } else {
                            finalResult.add(new Double(value));
                        }
                    }

                    int a = 0;

                    // write back the rows
                    for (int x = 0; x < position.size(); x++) {
                        ignore = false;

                        for (int z = 0; z < getIgnoreRows().length; z++) {
                            if (ignoreRows[z] == ((Position) position.get(x)).row) {
                                ignore = true;
                                z = 2 * ignoreRows.length;
                            }
                        }

                        if (ignore == false) {
                            setCell(i, ((Position) position.get(x)).row,
                                finalResult.get(a));
                            a++;
                        }
                    }
                }
            }
        }
    }

    /**
     * @author wohlgemuth
     * @version Nov 29, 2005
     * @see DataFile#runStatisticalMethod(edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.DeskriptiveMethod)
     */
    public double runStatisticalMethod(final DeskriptiveMethod statistic) {
        final List result = new ArrayList();

        boolean ignore;
        for (int x = 0; x < getTotalColumnCount(); x++) {
            ignore = false;

            for (int z = 0; z < getIgnoreColumns().length; z++) {
                if (ignoreColumns[z] == x) {
                    ignore = true;
                    z = 2 * ignoreColumns.length;
                }
            }

            if (ignore == false) {
                result.add(new Double(this.runStatisticalMethod(x, statistic)));
            }
        }

        return statistic.calculate(result);
    }

    /**
     * @author wohlgemuth
     * @version Nov 29, 2005
     * @see DataFile#runStatisticalMethod(int,
     * edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.DeskriptiveMethod)
     */
    public double runStatisticalMethod(final int column,
                                       final DeskriptiveMethod statistic) {
        final List toCalculate = new Vector();

        for (int i = 0; i < getTotalRowCount(); i++) {
            boolean ignore = false;

            for (final int ignoreRow : ignoreRows) {
                if (ignoreRow == i) {
                    ignore = true;
                }
            }

            if (ignore == false) {
                toCalculate.add(getCell(column, i));
            }
        }

        return statistic.calculate(toCalculate);
    }

    /**
     * @author wohlgemuth
     * @version Nov 29, 2005
     * @see DataFile#runStatisticalMethod(int,
     * int,
     * edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.DeskriptiveMethod)
     */
    public double[] runStatisticalMethod(final int column,
                                         final int groupingColumn, final DeskriptiveMethod statistic) {
        throw new RuntimeException("not yet supported!");
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#setCell(int,
     * int, Object)
     */
    public boolean setCell(final int column, final int row, final Object value) {
        getRow(row).set(column, value);
        return true;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#setColumn(int,
     * List)
     */
    public void setColumn(final int position, final List column) {

        for (final int ignoreColumn : ignoreColumns) {
            if (ignoreColumn == position) {
                return;
            }
        }

        if (position == -1) {
            throw new RuntimeException(
                "wrong position must be greate equals zero!");
        }

        for (int i = 0; i < data.size(); i++) {
            ((List) data.get(i)).set(position, column.get(i));
        }
    }

    public void setData(final List data) {
        this.data = data;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#setDimension(int,
     * int)
     */
    public void setDimension(final int row, final int columns) {

        if ((row == 0) | (columns == 0)) {
            throw new RuntimeException("the values must be grater than zero!");
        }

        data = null;
        data = new ArrayList(row);

        for (int i = 0; i < row; i++) {
            final List temp = new ArrayList(columns);

            for (int x = 0; x < columns; x++) {
                temp.add(null);
            }

            addRow(temp);
        }
    }

    @Override
    public void setIgnoreColumn(int position, boolean ignore) {

        if (ignore) {
            int[] newIgnore = new int[this.ignoreColumns.length + 1];

            for (int i = 0; i < ignoreColumns.length; i++) {
                newIgnore[i] = ignoreColumns[i];
            }
            newIgnore[ignoreColumns.length] = position;
            this.ignoreColumns = newIgnore;
        } else {
            int[] newIgnore = new int[this.ignoreColumns.length - 1];

            int y = 0;
            for (int i = 0; i < ignoreColumns.length; i++) {
                if (ignoreColumns[i] != position) {
                    newIgnore[y] = ignoreColumns[i];
                    y++;
                }
            }
            this.ignoreColumns = newIgnore;
        }
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#setIgnoreColumns(int[])
     */
    public void setIgnoreColumns(final int[] ignoreColumns) {
        Arrays.sort(ignoreColumns);
        this.ignoreColumns = ignoreColumns;
    }

    @Override
    public void setIgnoreRow(int position, boolean ignore) {

        if (ignore) {
            int[] newIgnore = new int[this.ignoreRows.length + 1];

            for (int i = 0; i < ignoreRows.length; i++) {
                newIgnore[i] = ignoreRows[i];
            }
            newIgnore[ignoreRows.length] = position;
            this.ignoreRows = newIgnore;
        } else {
            int[] newIgnore = new int[this.ignoreRows.length - 1];

            int y = 0;
            for (int i = 0; i < ignoreRows.length; i++) {
                if (ignoreRows[i] != position) {
                    newIgnore[y] = ignoreRows[i];
                    y++;
                }
            }
            this.ignoreRows = newIgnore;
        }
    }

    @Override
    public void addRow(Object... cell) {

        List<Object> list = new ArrayList<Object>(cell.length);

        for (Object o : cell) {
            list.add(o);
        }

        addRow(list);
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#setIgnoreRows(int[])
     */
    public void setIgnoreRows(final int[] ignoreRows) {
        Arrays.sort(ignoreRows);
        this.ignoreRows = ignoreRows;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#setRow(int,
     * List)
     */
    public void setRow(final int position, final List row) {

        for (final int ignoreRow : ignoreRows) {
            if (ignoreRow == position) {
                return;
            }
        }

        if (position == -1) {
            throw new RuntimeException(
                "wrong position must be greate equals zero!");
        }

        data.set(position, row);
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#sizeDown(boolean,
     * int, double)
     */
    public void sizeDown(final boolean group, final int column,
                         final double percent) {

        // delete based on complete column
        if (group == false) {

            for (int i = 0; i < getTotalColumnCount(); i++) {

                if (!skipColumnIndex(i)) {
                    final List col = getColumn(i);
                    final Iterator itx = col.iterator();

                    int zeros = 0;
                    int iX = 0;
                    while (itx.hasNext()) {
                        final Object o = itx.next();
                        iX++;

                        if (!skipRowIndex(iX)) {
                            if (isZero(o)) {
                                zeros++;
                            }
                        }
                    }

                    if ((double) ((getRowCount() - zeros) * 100)
                        / (double) getRowCount() < percent) {
                        deleteColumn(i);
                        i--;
                    }
                }
            }
        }
        // delete based on defined variable
        else {

            for (int i = 0; i < getTotalColumnCount(); i++) {
                boolean skip = skipColumnIndex(i);

                if (!skip) {

                    // contains all groups defined by column
                    final Collection groups = getGroups(column);

                    // find rows of each group

                    final Iterator it = groups.iterator();

                    boolean found = false;
                    boolean leave = false;
                    while (leave == false) {
                        // make sure the iterator has objects
                        if (it.hasNext()) {
                            int zeros = 0;

                            final Object o = it.next();

                            final Collection rows = findRowsByGroup(o, column);
                            final int size = rows.size();

                            // calculate zeros
                            final Iterator itx = rows.iterator();
                            while (itx.hasNext()) {
                                final Position p = (Position) itx.next();
                                final int rowId = p.row;
                                final Object data = getRow(rowId).get(i);

                                skip = false;
                                for (int x = 0; x < ignoreRows.length; x++) {
                                    if (rowId == ignoreRows[x]) {
                                        skip = true;
                                        x = ignoreRows.length;
                                    }
                                }

                                if (!skip) {

                                    if (isZero(data)) {
                                        zeros++;
                                    }
                                }
                            }

                            // are in this class more then n percent
                            if ((double) ((size - zeros) * 100) / (double) size >= percent) {

                                // leave the while loop yes we have more than
                                // percent hits
                                found = true;
                                leave = true;
                            }
                        } else {
                            leave = true;
                            found = false;
                        }
                    }

                    // ok we didn't found anything for the given groups so
                    // delete this column

                    if (found == false) {
                        deleteColumn(i);
                        i--;
                    }
                }
            }
        }
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#sizeDown(double)
     */
    public void sizeDown(final double percent) {
        this.sizeDown(false, 0, percent);
    }

    public boolean skipColumnIndex(final int i) {
        boolean skip = false;
        for (int x = 0; x < ignoreColumns.length; x++) {
            if (i == ignoreColumns[x]) {
                skip = true;
                x = ignoreColumns.length;
            }
        }
        return skip;
    }

    public boolean skipRowIndex(final int iX) {
        boolean skip = false;
        for (int x = 0; x < ignoreRows.length; x++) {
            if (iX == ignoreRows[x]) {
                skip = true;
                x = ignoreRows.length;
            }
        }
        return skip;
    }

    /**
     * @author wohlgemuth
     * @version Nov 30, 2005
     * @see DataFile#toInputStream()
     */
    public InputStream toInputStream() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        this.write(out);
        final byte[] data = out.toByteArray();
        out.close();
        return new ByteArrayInputStream(data);
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#toList()
     */
    public List toList() {

        final List liste = new Vector();

        for (int i = 0; i < data.size(); i++) {
            final List list = (List) data.get(i);

            for (int x = 0; x < list.size(); x++) {
                liste.add(list.get(x));
            }
        }
        return liste;
    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#write(File)
     */
    public void write(final File file) throws IOException {

        final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        write(writer);

    }

    public void write(final OutputStream out) throws IOException {
        this.write(new OutputStreamWriter(out));

    }

    /**
     * @author wohlgemuth
     * @version Nov 1, 2005
     * @see DataFile#write(Writer)
     */
    public void write(final Writer writer) throws IOException {

        for (int i = 0; i < data.size(); i++) {
            final List list = (List) data.get(i);

            for (int x = 0; x < list.size(); x++) {
                if (list.get(x) instanceof FormatObject) {
                    writer.write(((FormatObject) list.get(x)).getValue() + "\t");
                } else {
                    writer.write(list.get(x) + "\t");
                }
            }

            writer.write("\n");
        }

        writer.flush();
    }

    protected synchronized void writeBackToColumn(final int i, final List result) {
        boolean ignore;
        int a = 0;
        final List list = getColumn(i);
        // write back the rows
        for (int x = 0; x < getTotalRowCount(); x++) {
            ignore = false;

            for (int z = 0; z < getIgnoreRows().length; z++) {
                if (ignoreRows[z] == x) {
                    ignore = true;
                    z = 2 * ignoreRows.length;
                }
            }

            if (ignore == false) {
                list.set(x, result.get(a));
                a++;
            }
        }
        setColumn(i, list);
    }

    /**
     * writes the given sample back to the responding row in the datafile
     *
     * @param i
     * @param list
     * @param result
     */
    protected synchronized void writeBackToRow(final int i, final List result) {
        boolean ignore;

        final List list = getRow(i);

        int a = 0;
        // write back the rows
        for (int x = 0; x < getTotalColumnCount(); x++) {
            ignore = false;

            for (int z = 0; z < getIgnoreColumns().length; z++) {
                if (ignoreColumns[z] == x) {
                    ignore = true;
                    z = 2 * ignoreColumns.length;
                }
            }

            if (ignore == false) {
                list.set(x, result.get(a));
                a++;
            }
        }
        setRow(i, list);
    }
}
