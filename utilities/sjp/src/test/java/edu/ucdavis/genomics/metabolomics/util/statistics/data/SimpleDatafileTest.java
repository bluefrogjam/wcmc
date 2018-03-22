package edu.ucdavis.genomics.metabolomics.util.statistics.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.Min;
import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.NonZeroMin;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ReplaceWithMin;

public class SimpleDatafileTest extends TestCase {

	private DataFile file;

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(SimpleDatafileTest.class);
	}

	public SimpleDatafileTest(final String arg0) {
		super(arg0);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		file = createTestFile();
		file.read(new FileInputStream(("src/test/resources/test/221.txt")));
		file.setIgnoreRows(new int[] { 0, 1, 2, 3 });
		file.setIgnoreColumns(new int[] { 0, 1 });

	}

	protected SimpleDatafile createTestFile() {
		return new SimpleDatafile();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		file = null;
		System.gc();
	}

	/*
	 * Test method for
	 * 'edu.ucdavis.genomics.metabolomics.util.statistics.data.TxTDatafile.getColumnCount()'
	 */
	public void testGetColumnCount() {
		System.err.println(file.getColumnCount());
		assertTrue(file.getColumnCount() == 753);
	}

	/*
	 * Test method for
	 * 'edu.ucdavis.genomics.metabolomics.util.statistics.data.TxTDatafile.getRowCount()'
	 */
	public void testGetRowCount() {
		assertTrue(file.getRowCount() == 294);
	}

	/*
	 * Test method for
	 * 'edu.ucdavis.genomics.metabolomics.util.statistics.data.TxTDatafile.getRowName(int)'
	 */
	public void testGetRowName() {

	}

	/*
	 * Test method for
	 * 'edu.ucdavis.genomics.metabolomics.util.statistics.data.TxTDatafile.getRowPosition(String)'
	 */
	public void testGetRowPosition() {

	}

	/*
	 * Test method for
	 * 'edu.ucdavis.genomics.metabolomics.util.statistics.data.TxTDatafile.deleteColumn(int)'
	 */
	public void testDeleteColumnInt() {

		int columnCount = file.getColumnCount();
		int totalColumnount = file.getTotalColumnCount();
		System.out.println(columnCount);
		file.deleteColumn(2);
		columnCount = columnCount - 1;
		totalColumnount = totalColumnount - 1;
		System.out.println(columnCount);
		assertTrue(file.getColumnCount() == columnCount);
		assertTrue(file.getTotalColumnCount() == totalColumnount);
	}

	/*
	 * Test method for
	 * 'edu.ucdavis.genomics.metabolomics.util.statistics.data.TxTDatafile.sizeDown(double)'
	 */
	public void testSizeDownDouble() {
		file.sizeDown(80);
		assertTrue(file.getColumnCount() == 3);
		assertTrue(file.getTotalColumnCount() == 5);

	}

	/*
	 * Test method for
	 * 'edu.ucdavis.genomics.metabolomics.util.statistics.data.TxTDatafile.sizeDown(boolean,
	 * int, double)'
	 */
	public void testSizeDownBooleanIntDouble() throws FileNotFoundException {
		file.sizeDown(true, 1, 100);
		assertTrue(file.getTotalColumnCount() == 427);
	}

	public void testgetGroupsInt() {
		assertTrue(file.getGroups(1).size() == 49);
		assertTrue(file.getGroups(0).size() == 294);
	}

	public void testFindRowsByGroupObjectInt() {
		final Collection c = file.findRowsByGroup("2a10", 1);

		assertTrue(c.size() == 6);
		final Iterator it = c.iterator();

		int i = file.getIgnoreRows().length;
		while (it.hasNext()) {
			final Position p = (Position) it.next();
			assertTrue(p.column == 1);
			assertTrue(p.row == i);
			i++;
		}
	}

	/**
	 * test to test the zeroreplacement over the hole datafile with no grouping
	 * 
	 * @author wohlgemuth
	 * @version Nov 21, 2005
	 */
	public void testReplaceZerosZeroReplaceAble() {

		// a simple datfile for the test
		final SimpleDatafile file = createTestFile();
		{
			final List data = new Vector();
			data.add("A"); // 0
			data.add("B"); // 1
			data.add("C"); // 2
			data.add("D"); // 3
			data.add("E"); // 4
			data.add("F"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("B"); // 0
			data.add("1"); // 1
			data.add("1"); // 2
			data.add("X"); // 3
			data.add("2"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("C"); // 0
			data.add("1"); // 1
			data.add("3"); // 2
			data.add("X"); // 3
			data.add("3"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("D"); // 0
			data.add("X"); // 1
			data.add("X"); // 2
			data.add("X"); // 3
			data.add("X"); // 4
			data.add("X"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("E"); // 0
			data.add("2"); // 1
			data.add("4"); // 2
			data.add("X"); // 3
			data.add("4"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("F"); // 0
			data.add("2"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("1"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}

		System.out.println("before...");
		file.print(System.out);

		file.setIgnoreColumns(new int[] { 0, 3 });
		file.setIgnoreRows(new int[] { 0, 3 });

		file.replaceZeros(new ReplaceWithMin());
		System.out.println("after...");
		file.print(System.out);

		List column = file.getColumn(0);

		column = file.getColumn(2);
		assertTrue(Double.parseDouble(column.get(5).toString()) == 1);

		column = file.getColumn(5);
		assertTrue(Double.parseDouble(column.get(4).toString()) == 2);

	}

	/**
	 * test to test the zeroreplacement over the hole datafile with no grouping
	 * 
	 * @author wohlgemuth
	 * @version Nov 21, 2005
	 */
	public void testReplaceZerosZeroReplaceAbleIntGroupByColumn() {

		// a simple datfile for the test
		final SimpleDatafile file = createTestFile();
		{
			final List data = new Vector();
			data.add("A"); // 0
			data.add("B"); // 1
			data.add("C"); // 2
			data.add("D"); // 3
			data.add("E"); // 4
			data.add("F"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("B"); // 0
			data.add("1"); // 1
			data.add("1"); // 2
			data.add("X"); // 3
			data.add("2"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("C"); // 0
			data.add("1"); // 1
			data.add("3"); // 2
			data.add("X"); // 3
			data.add("3"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("D"); // 0
			data.add("X"); // 1
			data.add("X"); // 2
			data.add("X"); // 3
			data.add("X"); // 4
			data.add("X"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("E"); // 0
			data.add("2"); // 1
			data.add("4"); // 2
			data.add("X"); // 3
			data.add("4"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("F"); // 0
			data.add("2"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("1"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("G"); // 0
			data.add("3"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("0"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("H"); // 0
			data.add("3"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("0"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		file.print(System.out);
		file.setIgnoreColumns(new int[] { 0, 1, 3 });
		file.setIgnoreRows(new int[] { 0, 3 });

		file.replaceZeros(new ReplaceWithMin(), 1);
		file.print(System.out);

		List column = null;
		column = file.getColumn(2);

		assertTrue(Double.parseDouble(column.get(5).toString()) == 4);

		column = file.getColumn(5);
		assertTrue(Double.parseDouble(column.get(4).toString()) == 2);

		column = file.getColumn(5);
		assertTrue(Double.parseDouble(column.get(6).toString()) == 1);
		assertTrue(Double.parseDouble(column.get(7).toString()) == 1);

	}

	/**
	 * tests if we can clone a datafile
	 * 
	 * @author wohlgemuth
	 * @version Nov 22, 2005
	 * @throws CloneNotSupportedException
	 */
	public void testCombineColumns() {
		// a simple datfile for the test
		final DataFile file = createTestFile();
		{
			final List data = new Vector();
			data.add("A"); // 0
			data.add("B"); // 1
			data.add("C"); // 2
			data.add("D"); // 3
			data.add("E"); // 4
			data.add("F"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("B"); // 0
			data.add("1"); // 1
			data.add("1"); // 2
			data.add("X"); // 3
			data.add("2"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("C"); // 0
			data.add("1"); // 1
			data.add("3"); // 2
			data.add("X"); // 3
			data.add("3"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("D"); // 0
			data.add("X"); // 1
			data.add("X"); // 2
			data.add("X"); // 3
			data.add("X"); // 4
			data.add("X"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("E"); // 0
			data.add("2"); // 1
			data.add("4"); // 2
			data.add("X"); // 3
			data.add("4"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("F"); // 0
			data.add("2"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("1"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}

		file.setIgnoreColumns(new int[] { 0, 3 });
		file.setIgnoreRows(new int[] { 0, 3 });
		file.print(System.out);

		final ColumnCombiner c = new ColumnCombiner() {
			@Override
			public Object doWork(final List data) {

				System.err.println(data);
				assertTrue(data.size() == 2);
				return "Z";
			}
		};

		assertTrue(file.getTotalColumnCount() == 6);
		file.combineColumns(new int[] { 1, 2 }, c);
		assertTrue(file.getTotalColumnCount() == 5);
		file.combineColumns(new int[] { 3, 4 }, c);
		assertTrue(file.getTotalColumnCount() == 4);
		file.print(System.out);

	}

	/**
	 * tests if we can clone a datafile
	 * 
	 * @author wohlgemuth
	 * @version Nov 22, 2005
	 * @throws CloneNotSupportedException
	 */
	public void testClone() throws CloneNotSupportedException {
		// a simple datfile for the test
		final DataFile file = createTestFile();
		{
			final List data = new Vector();
			data.add("A"); // 0
			data.add("B"); // 1
			data.add("C"); // 2
			data.add("D"); // 3
			data.add("E"); // 4
			data.add("F"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("B"); // 0
			data.add("1"); // 1
			data.add("1"); // 2
			data.add("X"); // 3
			data.add("2"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("C"); // 0
			data.add("1"); // 1
			data.add("3"); // 2
			data.add("X"); // 3
			data.add("3"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("D"); // 0
			data.add("X"); // 1
			data.add("X"); // 2
			data.add("X"); // 3
			data.add("X"); // 4
			data.add("X"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("E"); // 0
			data.add("2"); // 1
			data.add("4"); // 2
			data.add("X"); // 3
			data.add("4"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("F"); // 0
			data.add("2"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("1"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}

		final DataFile test = (DataFile) file.clone();
		test.addEmptyRow();
		test.addEmptyRow();

		assertTrue(test.getRowCount() == (file.getRowCount() + 2));
	}

	public void testrunStatisticalMethodDeskriptiveMethod() {

		// a simple datfile for the test
		final DataFile file = createTestFile();
		{
			final List data = new Vector();
			data.add("A"); // 0
			data.add("B"); // 1
			data.add("C"); // 2
			data.add("D"); // 3
			data.add("E"); // 4
			data.add("F"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("B"); // 0
			data.add("1"); // 1
			data.add("1"); // 2
			data.add("X"); // 3
			data.add("2"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("C"); // 0
			data.add("1"); // 1
			data.add("3"); // 2
			data.add("X"); // 3
			data.add("3"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("D"); // 0
			data.add("X"); // 1
			data.add("X"); // 2
			data.add("X"); // 3
			data.add("X"); // 4
			data.add("X"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("E"); // 0
			data.add("2"); // 1
			data.add("4"); // 2
			data.add("X"); // 3
			data.add("4"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("F"); // 0
			data.add("2"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("1"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("G"); // 0
			data.add("3"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("0"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("H"); // 0
			data.add("3"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("0"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		file.setIgnoreColumns(new int[] { 0, 3 });
		file.setIgnoreRows(new int[] { 0, 3 });
		file.print(System.out);

		double result = file.runStatisticalMethod(new Min());
		assertEquals(0.0,result);
		result = file.runStatisticalMethod(new NonZeroMin());
		assertEquals(1.0,result);

	}

	public void testAddColumnAtPosition() {
		final DataFile file = createTestFile();
		{
			final List data = new Vector();
			data.add("A"); // 0
			data.add("B"); // 1
			data.add("C"); // 2
			data.add("D"); // 3
			data.add("E"); // 4
			data.add("F"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("B"); // 0
			data.add("1"); // 1
			data.add("1"); // 2
			data.add("X"); // 3
			data.add("2"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("C"); // 0
			data.add("1"); // 1
			data.add("3"); // 2
			data.add("X"); // 3
			data.add("3"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("D"); // 0
			data.add("X"); // 1
			data.add("X"); // 2
			data.add("X"); // 3
			data.add("X"); // 4
			data.add("X"); // 5
			file.addRow(data);
		}
		{
			final List data = new Vector();
			data.add("E"); // 0
			data.add("2"); // 1
			data.add("4"); // 2
			data.add("X"); // 3
			data.add("4"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("F"); // 0
			data.add("2"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("1"); // 4
			data.add("2"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("G"); // 0
			data.add("3"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("0"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		{
			final List data = new Vector();
			data.add("H"); // 0
			data.add("3"); // 1
			data.add("0"); // 2
			data.add("X"); // 3
			data.add("0"); // 4
			data.add("0"); // 5
			file.addRow(data);
		}

		file.setIgnoreColumns(new int[] { 0, 3 });
		file.setIgnoreRows(new int[] { 0, 3 });
		file.print(System.out);

		System.out.println("we should have now a row at position 0 and 4");

		int size = file.getColumnCount();
		file.addEmptyColumn("test", 0);
		file.addEmptyColumn("test", 4);

		file.print(System.out);

		assertTrue(file.getColumnCount() == size + 2);
		assertTrue(file.ignoreColumn(0) == false);
		assertTrue(file.ignoreColumn(1) == true);
		assertTrue(file.ignoreColumn(5) == true);

		file.setIgnoreColumn(0, true);
		file.setIgnoreColumn(1, false);

		assertTrue(file.ignoreColumn(1) == false);
		assertTrue(file.ignoreColumn(0) == true);

	}
}
