/*
 * Created on Jun 20, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ucdavis.genomics.metabolomics.util.statistics.data.SimpleDatafile;
import org.slf4j.Logger;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.date.DateUtil;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.date.PatternException;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.date.SampleDate;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.thread.ExecutorsServiceFactory;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.ColumnCombiner;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ZeroReplaceable;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.BinFormat;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.BinObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.HeaderFormat;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.MetaObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.NullObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ZeroObject;
import edu.ucdavis.genomics.metabolomics.util.type.converter.BooleanConverter;

/**
 * extends the datafile to represent the binbase xml result and allows querrys
 * on it
 * 
 * @author wohlgemuth
 * @version Jun 20, 2006
 */
public class ResultDataFile extends SimpleDatafile {

	public static final String RELATED_TO_INCULDED_BIN = "binIsRelatedToIncludedBin";

	private String database;

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	private int resultId;

	public int getResultId() {
		return resultId;
	}

	protected void setResultId(int resultId) {
		this.resultId = resultId;
	}

	private SampleTimeResolver resolver;

	/**
	 * internally used group cache to associated bin names, with groupId's
	 */
	private Map<Integer, Set<String>> groupCache = new HashMap<Integer, Set<String>>();

	private boolean indexed = false;

	public boolean isIndexed() {
		return indexed;
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	private ResultDataFileIndex index = null;

	public SampleTimeResolver getResolver() {
		return resolver;
	}

	public void setResolver(SampleTimeResolver resolver) {
		getLogger().info("setting resolver: " + resolver);
		this.resolver = resolver;
	}

	private final Map<String, Date> times = new HashMap<String, Date>();

	/**
     *
     */
	private static final long serialVersionUID = 2L;

	public static final String PARENT_BIN_OF_RELATED_BIN = "parent bin of a group of bins";

	protected Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

	/**
	 * looks for the format object with the given bin id
	 * 
	 * @param id
	 * @return
	 * @author wohlgemuth
	 * @version Jun 20, 2006
	 */
	@SuppressWarnings("unchecked")
	public HeaderFormat<String> getBin(final int id) {

		if (isIndexed()) {
			HeaderFormat<String> result = this.index.getBin(id);

			if (result != null) {
				return result;
			}
		}

		final List<FormatObject> list = getRow(3);
		for (int i = 0; i < list.size(); i++) {

			if (list.get(i).getValue()
					.equals(String.valueOf(Integer.valueOf(id)))) {
				return (HeaderFormat<String>) getCell(i, 0);
			}
		}
		return null;
	}

	public HeaderFormat<String> getBin(final String name) {

		final List<FormatObject> list = getRow(0);
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getValue().equals(name)) {
				return (HeaderFormat<String>) getCell(i, 0);
			}
		}
		return null;
	}

	/**
	 * gives us all bins for the given group
	 * 
	 * @param id
	 * @return
	 * @author wohlgemuth
	 * @version Feb 27, 2007
	 */
	public List<Integer> getBinsForGroup(final int id) {
		final List<Integer> result = new ArrayList<Integer>();

		for (final HeaderFormat<String> bin : this.getBins()) {
			if (id == Integer.parseInt((bin.getAttributes().get("group")
					.toString()))) {
				result.add(Integer.parseInt((bin.getAttributes().get("id")
						.toString())));
			}
		}
		return result;
	}

	/**
	 * returns all available names in this group
	 * 
	 * @param id
	 * @return
	 */
	public Set<String> getBinNamesForGroup(Integer id) {
		return this.groupCache.get(id);
	}

	/**
	 * removes a bin from the dataset
	 * 
	 * @param bin
	 * @author wohlgemuth
	 * @version Nov 2, 2006
	 */
	public void removeBin(final HeaderFormat<String> bin) {
		this.removeBin(bin, false);
	}

	public void removeBin(final HeaderFormat<String> bin, boolean includeFame) {
		int id = Integer.parseInt(bin.getAttributes().get("id").toString());

		if (includeFame) {
			// remove this bin from the ignore position
			this.ignoreColumn(getBinPosition(id), false);
		}

		deleteColumn(getBinPosition(id));

		if (isIndexed()) {
			this.index.removeBin(id);
		}
	}

	public void removeSample(final SampleObject<String> sample) {
		deleteRow(getSamplePosition(sample.getValue()));

		if (isIndexed()) {
			index.removeSample(sample.getValue());
		}
	}

	public BinObject<String> getBinForColumn(final int column) {
		return (BinObject<String>) getCell(column, 3);
	}

	@SuppressWarnings("unchecked")
	public void applyFunction(final Function function)
			throws NumberFormatException, BinBaseException {

		for (int i = 0; i < getTotalRowCount(); i++) {
			boolean ignore = false;

			for (int x = 0; x < getIgnoreRows().length; x++) {
				if (getIgnoreRows()[x] == i) {
					ignore = true;
				}
			}

			if (ignore == false) {

				for (int x = 0; x < getTotalColumnCount(); x++) {
					ignore = false;

					for (int z = 0; z < getIgnoreColumns().length; z++) {
						if (getIgnoreColumns()[z] == x) {
							ignore = true;
							z = 2 * getIgnoreColumns().length;
						}
					}

					if (ignore == false) {
						final Object value = function.apply(getCell(x, i),
								getBinForColumn(x), getSampleForRow(i));
						setCell(x, i, value);
					}
				}
			}
		}
	}

	/**
	 * returns the position of the column containing this bin
	 * 
	 * @param id
	 * @return
	 * @author wohlgemuth
	 * @version Jun 30, 2006
	 */
	@SuppressWarnings("unchecked")
	public int getBinPosition(final int id) {

		getLogger().debug("searching for bin position: " + id);
		final List<MetaObject<String>> list = getRow(3);

		for (int i = 0; i < list.size(); i++) {

			MetaObject<String> object = list.get(i);

			if (object != null) {
				if (object.getValue().equals(
						String.valueOf(Integer.valueOf(id)))) {
					return i;
				}
			}

		}
		throw new BinNotFoundException("can't find this bin: " + id);
	}

	@SuppressWarnings("unchecked")
	public int getBinPosition(String name) {
		name = name.trim();
		getLogger().debug("searching for bin position: " + name);

		final List<FormatObject<String>> list = getRow(0);

		// search by name
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getAttributes() != null
					&& list.get(i).getAttributes().containsKey("name")) {
				if (list.get(i).getAttributes().get("name").equals(name)) {
					return i;
				}
			}

			if (list.get(i).getValue().equals(name)) {

				return i;
			}

		}

		getLogger()
				.debug("searching by alternative group name, in case the file has been combined...");
		// search by group name
		for (int i = 0; i < list.size(); i++) {

			if (list.get(i).getAttributes() != null
					&& list.get(i).getAttributes().containsKey("group")) {
				Integer groupId = Integer.parseInt(list.get(i).getAttributes()
						.get("group"));

				if (groupId > 0) {
					Set<String> names = getBinNamesForGroup(groupId);

					if (names.contains(name)) {
						return i;
					}
				}
			}
		}

		throw new BinNotFoundException("can't find this bin: " + name);
	}

	@SuppressWarnings("unchecked")
	public SampleObject<String> getSample(final String name) {

		if (isIndexed()) {
			SampleObject<String> result = this.index.getSample(name);
			if (result != null) {
				return result;
			}
		}

		final List<FormatObject> list = getColumn(0);
		for (int i = 0; i < list.size(); i++) {
			boolean skip = false;
			for (int x = 0; x < getIgnoreRows().length; x++) {
				if (i == getIgnoreRows()[x]) {
					skip = true;
					x = getIgnoreRows().length;
				}
			}
			if (!skip) {
				if (list.get(i).getValue().equals(name)) {
					return (SampleObject<String>) getCell(0, i);
				}
			}
		}
		return null;
	}

	/**
	 * returns the sample for this index
	 * 
	 * @param row
	 * @return
	 */
	public SampleObject<String> getSampleForRow(final int row) {
		return (SampleObject<String>) getRow(row).get(0);
	}

	/**
	 * returns the spectra for a bin and sample or null if its a null value
	 * 
	 * @param sampleName
	 * @param binId
	 * @return
	 */
	public FormatObject<?> getSpectra(String sampleName, int binId) {

		
		int rowNumber = this.getSamplePosition(sampleName);
		int columnNumber = this.getBinPosition(binId);

		//getLogger().info("found " + sampleName + " and bin " + binId + " at " + rowNumber + " and " + columnNumber);
		FormatObject<?> spect = (FormatObject<?>)this.getRow(rowNumber).get(columnNumber);
		//getLogger().info("spectra is: " + spect);
		return spect ;
	}

	public FormatObject<?> getSpectra(String sampleName, String binName) {

		int rowNumber = this.getSamplePosition(sampleName);
		int columnNumber = this.getBinPosition(binName);

		return (FormatObject<?>) this.getRow(rowNumber).get(columnNumber);
	}

	/**
	 * returns the position of the sample
	 * 
	 * @param name
	 * @return
	 * @author wohlgemuth
	 * @version Nov 2, 2006
	 */
	@SuppressWarnings("unchecked")
	public int getSamplePosition(final String name) {

		final List<FormatObject> list = getColumn(0);
		for (int i = 0; i < list.size(); i++) {
			boolean skip = false;
			for (int x = 0; x < getIgnoreRows().length; x++) {
				if (i == getIgnoreRows()[x]) {
					skip = true;
					x = getIgnoreRows().length;
				}
			}
			if (!skip) {
				if (list.get(i).getValue().equals(name)) {
					return i;
				}
			}

		}

		throw new RuntimeException("can't find this sample: " + name);
	}

	@SuppressWarnings("unchecked")
	public SampleObject<String> getSample(final int id) {

		final List<FormatObject> list = getColumn(0);
		for (int i = 0; i < list.size(); i++) {
			boolean skip = false;
			for (int x = 0; x < getIgnoreRows().length; x++) {
				if (i == getIgnoreRows()[x]) {
					skip = true;
					x = getIgnoreRows().length;
				}
			}
			if (!skip) {
				if (list.get(i).getAttributes().get("id")
						.equals(String.valueOf(id))) {
					return (SampleObject<String>) getCell(0, i);
				}
			}
		}
		return null;
	}

	/**
	 * returns all massspecs for the given bin
	 * 
	 * @param id
	 * @return
	 * @author wohlgemuth
	 * @version Jun 20, 2006
	 */
	@SuppressWarnings("unchecked")
	public List<ContentObject<Double>> getMassspecsForBin(final int id,
			boolean ignoreNull) {

		if (isIndexed()) {
			getLogger().debug("it's indexed...");
			List<ContentObject<Double>> list = this.index.getMassSpecs(id);

			if (list != null) {
				return list;
			}
		}

		final List<FormatObject> list = getRow(3);
		final List<ContentObject<Double>> result = new ArrayList<ContentObject<Double>>();

		final int[] ignore = getIgnoreColumns();

		for (int i = 0; i < list.size(); i++) {
			boolean skip = false;
			for (int x = 0; x < ignore.length; x++) {
				if (ignore[x] == i) {
					skip = true;
					x = ignore.length;
				}
			}
			if (!skip) {

				if (list.get(i).getValue()
						.equals(String.valueOf(Integer.valueOf(id)))) {

					final List temp = getColumn(i);
					i = list.size();

					for (int y = 0; y < temp.size(); y++) {
						skip = false;
						for (int x = 0; x < getIgnoreRows().length; x++) {
							if (y == getIgnoreRows()[x]) {
								skip = true;
								x = getIgnoreRows().length;
							}
						}
						if (!skip) {
							if (temp.get(y) != null) {

								if (ignoreNull == false) {
									result.add((ContentObject<Double>) temp
											.get(y));

								} else {

									if (temp.get(y) instanceof NullObject == false) {
										ContentObject<Double> value = (ContentObject<Double>) temp
												.get(y);

										result.add(value);
									}
								}
							}

						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * returns all massspecs for the given bin
	 * 
	 * @param id
	 * @return
	 * @author wohlgemuth
	 * @version Jun 20, 2006
	 */
	@SuppressWarnings("unchecked")
	public List<ContentObject<Double>> getMassspecsForBin(final int id) {
		return getMassspecsForBin(id, false);
	}

	public List<ContentObject<Double>> getMassspecsForBinGroup(final int id) {

		final List<ContentObject<Double>> result = new ArrayList<ContentObject<Double>>();
		for (final int bin : getBinsForGroup(id)) {
			for (final ContentObject<Double> massspec : getMassspecsForBin(bin)) {
				result.add(massspec);
			}
		}
		return result;
	}

	/**
	 * returns associated massspecs to this sample
	 * 
	 * @param name
	 * @return
	 * @author wohlgemuth
	 * @version Jun 21, 2006
	 */
	public List<ContentObject<Double>> getMassspecsForSample(final String name) {
		return getMassspecsForSample(name, false);
	}

	@SuppressWarnings("unchecked")
	public List<ContentObject<Double>> getMassspecsForSample(final String name,
			final boolean includedIgnoredStandards) {

		final List<FormatObject> list = getColumn(0);
		final List<ContentObject<Double>> result = Collections
				.synchronizedList(new ArrayList<ContentObject<Double>>());

		ExecutorService service = ExecutorsServiceFactory.createService();

		for (int i = 0; i < list.size(); i++) {
			boolean skip = false;
			for (int x = 0; x < getIgnoreRows().length; x++) {
				if (i == getIgnoreRows()[x]) {
					skip = true;
					x = getIgnoreRows().length;
				}
			}
			if (!skip) {
				if (list.get(i).getValue().equals(name)) {

					final int position = i;

					Runnable run = new Runnable() {

						@Override
						public void run() {
							final List temp = getRow(position);

							boolean skip = false;

							for (int y = 0; y < temp.size(); y++) {
								skip = false;

								if (includedIgnoredStandards == true) {
									// make sure that standards are not ignored
									if (temp.get(y) instanceof BinObject<?>) {
										final BinObject<String> bin = getBinForColumn(y);

										if (bin != null
												&& bin.getAttributes() != null) {
											if (bin.getAttributes()
													.get("standard")
													.equals("TRUE") == false) {
												skip = isColumnSupposedToBeSkipped(
														skip, y);
											}
										} else {
											skip = isColumnSupposedToBeSkipped(
													skip, y);
										}
									}
								} else {
									skip = isColumnSupposedToBeSkipped(skip, y);

								}

								if (!skip) {
									if (temp.get(y) != null) {
										if (temp.get(y) instanceof NullObject == false
												&& temp.get(y) instanceof ContentObject) {

											result.add((ContentObject<Double>) temp
													.get(y));
										}
									}
								}
							}

						}
					};

					// service.execute(run);
					run.run();
				}
			}
		}

		ExecutorsServiceFactory.shutdownService(service);

		return result;
	}

	private boolean isColumnSupposedToBeSkipped(boolean skip, final int column) {
		for (int x = 0; x < getIgnoreColumns().length; x++) {

			if (column == getIgnoreColumns()[x]) {
				skip = true;
				x = getIgnoreColumns().length;
			}
		}
		return skip;
	}

	/**
	 * give us the list with the retention time markers for the current sample
	 * 
	 * @param sampleName
	 * @return
	 * @author wohlgemuth
	 * @version Jun 20, 2006
	 */
	@SuppressWarnings("unchecked")
	public List<ContentObject<Double>> getStandardForSample(
			final String sampleName) {
		final List<ContentObject<Double>> result = new ArrayList<ContentObject<Double>>();

		final List<ContentObject<Double>> list = this.getMassspecsForSample(
				sampleName, true);

		for (int i = 0; i < list.size(); i++) {
			final Map attributes = list.get(i).getAttributes();
			if (attributes.isEmpty() == false) {
				final int id = Integer
						.parseInt(attributes.get("id").toString());
				final HeaderFormat<String> bin = getBin(id);

				if (bin != null) {
					if (BooleanConverter.StringtoBoolean(bin.getAttributes()
							.get("standard")) == true) {
						if (list.get(i) instanceof NullObject == false) {
							result.add(list.get(i));
						} else {
							getLogger()
									.info("standard was null, so not added!");
						}
					}
				} else {
					// bin was removed during combining, nothing we can do about
					// it
				}
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<HeaderFormat<String>> getStandards() {
		final List<HeaderFormat<String>> result = new ArrayList<HeaderFormat<String>>();

		final List<HeaderFormat<String>> list = this.getBins(true);

		for (int i = 0; i < list.size(); i++) {
			final Map attributes = list.get(i).getAttributes();
			if (attributes.isEmpty() == false) {
				if (BooleanConverter.StringtoBoolean((String) attributes
						.get("standard")) == true) {
					result.add(list.get(i));
				}
			}
		}

		return result;
	}

	/**
	 * removes the fame markers from this dataset
	 */
	public void removeFameMarkers() {
		final List<HeaderFormat<String>> list = this.getBins(true);

		for (int i = 0; i < list.size(); i++) {
			final Map attributes = list.get(i).getAttributes();
			if (attributes.isEmpty() == false) {
				if (BooleanConverter.StringtoBoolean((String) attributes
						.get("standard")) == true) {
					removeBin(list.get(i), true);
				}
			}
		}

	}

	/**
	 * returns the list of defined standards
	 * 
	 * @return
	 * @author wohlgemuth
	 * @version Jun 22, 2006
	 */
	public List<HeaderFormat<String>> getDefinedStandards() {
		final List<HeaderFormat<String>> data = this.getBins();
		final List<HeaderFormat<String>> result = new ArrayList<HeaderFormat<String>>();

		final Iterator<HeaderFormat<String>> it = data.iterator();

		while (it.hasNext()) {
			final HeaderFormat<String> x = it.next();

			if (x.getAttributes().get("standard") == null) {
				// no standard found
			} else if (BooleanConverter.StringtoBoolean(x.getAttributes()
					.get("standard").toString())) {
				result.add(x);
			}
		}
		return result;

	}

	/**
	 * returns all bins
	 * 
	 * @return
	 * @author wohlgemuth
	 * @version Jun 20, 2006
	 */
	@SuppressWarnings("unchecked")
	public List<HeaderFormat<String>> getBins() {
		return getBins(false);
	}

	/**
	 * returns all bin names in a set
	 * 
	 * @return
	 */
	public Set<String> getAllBinNames() {

		Set<String> bins = new HashSet<String>();

		for (HeaderFormat<String> b : getBins(true)) {
			bins.add(b.getAttributes().get("name"));
		}

		return bins;
	}

	@SuppressWarnings("unchecked")
	public List<HeaderFormat<String>> getBins(
			final boolean includedIgnoredStandards) {
		final List<HeaderFormat<String>> result = new ArrayList<HeaderFormat<String>>();

		final List list = getRow(0);

		for (int y = 0; y < list.size(); y++) {
			boolean skip = false;

			if (list.get(y) instanceof BinFormat<?>) {
				if (includedIgnoredStandards == true) {
					// make sure that standards are not ignored
					final BinObject<String> bin = getBinForColumn(y);

					if (bin != null && bin.getAttributes() != null) {
						if (bin.getAttributes().get("standard").equals("TRUE") == false) {
							skip = isColumnSupposedToBeSkipped(skip, y);
						}
					} else {
						skip = isColumnSupposedToBeSkipped(skip, y);
					}
				} else {
					skip = isColumnSupposedToBeSkipped(skip, y);

				}

				if (!skip) {
					result.add((HeaderFormat<String>) list.get(y));
				}
			}
		}

		return result;
	}

	/**
	 * returns all samples
	 * 
	 * @return
	 * @author wohlgemuth
	 * @version Jun 20, 2006
	 */
	@SuppressWarnings("unchecked")
	public List<SampleObject<String>> getSamples() {

		final List list = getColumn(0);
		final List<SampleObject<String>> result = new ArrayList<SampleObject<String>>(list.size());

		for (int i = 0; i < list.size(); i++) {
			boolean skip = false;
			for (int x = 0; x < getIgnoreRows().length; x++) {
				if (i == getIgnoreRows()[x]) {
					skip = true;
					x = getIgnoreRows().length;
				}
			}
			if (!skip) {
				result.add((SampleObject<String>) list.get(i));
			}
		}
		return result;
	}

	@Override
	public void replaceZeros(final ZeroReplaceable replace, final boolean sample) {
		if (replace instanceof BinBaseResultZeroReplaceable) {
			((BinBaseResultZeroReplaceable) replace).setSampleBased(sample);
			((BinBaseResultZeroReplaceable) replace)
					.provideMissingDataForNullObjects();
		}
		super.replaceZeros(replace, sample);
	}

	@Override
	public void replaceZeros(final ZeroReplaceable replace,
			final int columnToGroupBy) {
		if (replace instanceof BinBaseResultZeroReplaceable) {
			((BinBaseResultZeroReplaceable) replace).setSampleBased(false);
			((BinBaseResultZeroReplaceable) replace)
					.provideMissingDataForNullObjects();
		}
		super.replaceZeros(replace, columnToGroupBy);
	}

	/**
	 * returns all columns for a given group
	 * 
	 * @param groupId
	 * @return
	 */
	public List<Integer> getColumnsForGroup(int groupId) {
		final List<HeaderFormat<String>> list = this.getBins();
		final List<Integer> groups = new ArrayList<Integer>();

		Integer firstColumn = null;

		for (int i = 0; i < list.size(); i++) {
			final HeaderFormat<String> o = list.get(i);

			final int group = Integer.parseInt((o.getAttributes().get("group")
					.toString()));

			if (group == groupId) {
				if (o.getAttributes().get(
						ResultDataFile.PARENT_BIN_OF_RELATED_BIN) != null
						&& Boolean.parseBoolean(o.getAttributes().get(
								ResultDataFile.PARENT_BIN_OF_RELATED_BIN))) {
					firstColumn = getBinPosition(Integer.parseInt(o
							.getAttributes().get("id")));

				} else {
					int column = getBinPosition(Integer.parseInt(o
							.getAttributes().get("id")));
					groups.add(column);
				}
			}

		}

		// pushes the main column to the first
		if(firstColumn != null){
			groups.add(0, firstColumn);
		}
		return groups;
	}

	/**
	 * returns a collection of all the group ids we have
	 * 
	 * @return
	 */
	public List<Integer> getAllGroups() {
		final List<HeaderFormat<String>> list = this.getBins();
		final List<Integer> groups = new ArrayList<Integer>();

		for (int i = 0; i < list.size(); i++) {
			final HeaderFormat<String> o = list.get(i);

			final int group = Integer.parseInt((o.getAttributes().get("group")
					.toString()));

			if (group != 0) {
				if (groups.contains(group) == false) {
					getLogger().debug("detected group: " + group);
					groups.add(group);
				}
			}
		}
		return groups;
	}

	@Override
	/**
	 * this methods will search for the bins which should be combined and
	 * combines than the columns bins with the grou_id 0 will not be grouped
	 * together
	 */
	public void combineColumns(final ColumnCombiner combine) {

		/*
		 * final List<HeaderFormat<String>> list = this.getBins(); final
		 * List<Integer> groups = new ArrayList<Integer>();
		 * 
		 * for (int i = 0; i < list.size(); i++) { final HeaderFormat<String> o
		 * = list.get(i);
		 * 
		 * final int group = Integer.parseInt((o.getAttributes().get("group")
		 * .toString()));
		 * 
		 * // we ignore number 0, is the default value if (group != 0) {
		 * 
		 * // we dont want to group bins twice if (groups.contains(group) ==
		 * false) {
		 * 
		 * final List<Integer> ids = new ArrayList<Integer>();
		 * ids.add(getBinPosition(Integer.parseInt((o.getAttributes()
		 * .get("id").toString()))));
		 * 
		 * groups.add(group);
		 * 
		 * for (int x = 0; x < list.size(); x++) { final HeaderFormat<String> b
		 * = list.get(x); final int groupB = Integer.parseInt((b.getAttributes()
		 * .get("group").toString()));
		 * 
		 * if (groupB == group) { ids.add(getBinPosition(Integer.parseInt((b
		 * .getAttributes().get("id").toString())))); } }
		 * 
		 * final int array[] = new int[ids.size()];
		 * 
		 * for (int y = 0; y < array.length; y++) { array[y] = ids.get(y); }
		 * 
		 * // combine the selected data this.combineColumns(array, combine); } }
		 * }
		 */

		List<Integer> groups = this.getAllGroups();

		for (Integer group : groups) {
			List<Integer> columns = this.getColumnsForGroup(group);

			getLogger().info("columns for group: " + group + " - " + columns);

			// order columns so that the one with our parent attribute is in the
			// first for

			int[] array = new int[2];

			//really really inefficient
			while (columns.size() >  1) {
				for (int i = 0; i < 2; i++) {
					array[i] = columns.get(i);
				}

				this.combineColumns(array, combine);
				columns = this.getColumnsForGroup(group);
				getLogger().info("columns now for group: " + group + " - " + columns);

			}
		}
	}

	/**
	 * removes failed samples out of the datafile
	 * 
	 * @author wohlgemuth
	 * @version Jul 13, 2006
	 */
	@SuppressWarnings("unchecked")
	public void removeFailedSamples() {
		for (int i = 0; i < getTotalRowCount(); i++) {
			boolean skip = false;
			for (int x = 0; x < getIgnoreRows().length; x++) {
				if (i == getIgnoreRows()[x]) {
					skip = true;
					x = getIgnoreRows().length;
				}
			}
			if (!skip) {
				final SampleObject<String> sample = (SampleObject<String>) getCell(
						0, i);
				if (BooleanConverter.StringtoBoolean(sample.getAttributes()
						.get("correctionFailed")) == true) {
					deleteRow(i);
					i--;
				}
			}
		}
	}

	@Override
	public void sizeDown(final boolean group, final int column,
			final double percent) {
		final int oldIgnore[] = getIgnoreColumns();
		final List<HeaderFormat<String>> standards = getDefinedStandards();
		final Iterator<HeaderFormat<String>> it = standards.iterator();

		final ArrayList<Integer> collection = new ArrayList<Integer>();

		while (it.hasNext()) {
			collection.add(getBinPosition(Integer.parseInt(it.next()
					.getAttributes().get("id").toString())));
		}

		for (final int element : oldIgnore) {
			collection.add(element);
		}

		final int[] result = new int[collection.size()];

		for (int i = 0; i < result.length; i++) {
			result[i] = collection.get(i);
		}

		try {
			setIgnoreColumns(result);
			super.sizeDown(group, column, percent);
		} finally {
			setIgnoreColumns(oldIgnore);
		}
	}

	@Override
	public void sizeDown(final double percent) {
		final int oldIgnore[] = getIgnoreColumns();
		final List<HeaderFormat<String>> standards = getDefinedStandards();
		final Iterator<HeaderFormat<String>> it = standards.iterator();

		final ArrayList<Integer> collection = new ArrayList<Integer>();

		while (it.hasNext()) {
			collection.add(getBinPosition(Integer.parseInt(it.next()
					.getAttributes().get("id").toString())));
		}

		for (final int element : oldIgnore) {
			collection.add(element);
		}

		final int[] result = new int[collection.size()];

		for (int i = 0; i < result.length; i++) {
			result[i] = collection.get(i);
		}

		try {
			setIgnoreColumns(result);
			super.sizeDown(percent);
		} finally {
			setIgnoreColumns(oldIgnore);
		}
	}

	/**
	 * filter all the bins and gives us the option to keep ignored bins. This
	 * will only be done based on id and nothing else!
	 * 
	 * @param toKeep
	 * @param ignoreCombinedBins
	 */
	public void filterBins(final List<String> toKeep, boolean ignoreCombinedBins) {
		final List<HeaderFormat<String>> bins = this.getBins();
		final Iterator<HeaderFormat<String>> bi = bins.iterator();
		final List<HeaderFormat<String>> deleteBin = new ArrayList<HeaderFormat<String>>();

		// contains all the groups we want to keep
		Set<Integer> relatedBins = new HashSet<Integer>();
		// doing the actual filtering
		while (bi.hasNext()) {
			final HeaderFormat<String> o = bi.next();

			final Iterator<String> it = toKeep.iterator();

			boolean match = false;
			while (it.hasNext() && match == false) {

				try {
					final int id = Integer.parseInt(o.getAttributes().get("id")
							.trim());
					final int p = Integer.parseInt(it.next());

					if (id == p) {
						match = true;
					} else {
						match = false;
					}
				} catch (NumberFormatException e) {
					match = false;
				}

			}

			// keep track of all the related bins
			if (match) {
				if (o.getAttributes().get("other_bins_in_group").isEmpty() == false) {
					for (String s : o.getAttributes()
							.get("other_bins_in_group").split(",")) {
						Integer i = Integer.parseInt(s);
						relatedBins.add(i);
					}
				}
			}
			if (o.getAttributes().get("standard").equals("TRUE") == false) {

				if (match == false) {
					deleteBin.add(o);
				} else {
					o.addAttribute(PARENT_BIN_OF_RELATED_BIN, "true");

				}
			}
		}

		final Iterator<HeaderFormat<String>> it = deleteBin.iterator();

		while (it.hasNext()) {
			if (ignoreCombinedBins == false) {
				HeaderFormat<String> next = it.next();
				Integer currentId = Integer.parseInt(next.getAttributes().get(
						"id"));

				if (relatedBins.contains(currentId)) {
					getLogger().debug("kept related bin " + currentId);
					next.addAttribute(RELATED_TO_INCULDED_BIN, "true");
				} else {
					removeBin(next);
				}
			} else {
				removeBin(it.next());
			}
		}
	}

	/**
	 * exports only the samples in the given list --> works on reg expressions
	 * 
	 * @param toKeep
	 * @author wohlgemuth
	 * @version Nov 2, 2006
	 */
	public void filterSamples(final List<String> toKeep) {
		final List<SampleObject<String>> samples = getSamples();
		final Iterator<SampleObject<String>> bi = samples.iterator();
		final List<SampleObject<String>> deleteSample = new ArrayList<SampleObject<String>>();

		while (bi.hasNext()) {
			final SampleObject<String> o = bi.next();
			final String name = o.getValue().toString();

			final Iterator<String> it = toKeep.iterator();

			boolean match = false;
			while (it.hasNext()) {
				if (name.matches(it.next())) {
					match = true;
				}
			}

			if (match == false) {
				deleteSample.add(o);
			}
		}

		final Iterator<SampleObject<String>> it = deleteSample.iterator();

		while (it.hasNext()) {
			removeSample(it.next());
		}
	}

	/**
	 * returns the average retention time for this bin
	 * 
	 * @param id
	 * @return
	 */
	public Double getAverageRetentionTimeForBin(final int id) {
		return this.averageRT.get(id);
	}

	/**
	 * calculates the average retention time for the given bin over all classes
	 * 
	 * @return
	 * @author wohlgemuth
	 * @version Jan 30, 2007
	 */
	protected double calculateAverageRetentionTimeForBin(final int id) {
		final List<ContentObject<Double>> x = getMassspecsForBin(id);

		double last = Double.MIN_VALUE;
		int count = 0;

		for (final ContentObject<Double> a : x) {

			if (a.getAttributes().isEmpty()) {
			} else {
				final Double value = Double.valueOf(a.getAttributes()
						.get("retentiontime").toString());

				if (value == 0) {

				} else if (last == Double.MIN_VALUE) {
					last = value;
					count++;
				} else {
					last = last + value;
					count++;
				}
			}
		}
		return last / count;
	}

	/**
	 * gets the average retention index for this bin
	 * 
	 * @param id
	 * @return
	 */
	public double getAverageRetentionIndexForBin(final int id) {
		return this.averageRI.get(id);
	}

	protected double calculateAverageRetentionIndexForBin(final int id) {
		final List<ContentObject<Double>> x = getMassspecsForBin(id);

		double last = Double.MIN_VALUE;
		int count = 0;

		for (final ContentObject<Double> a : x) {

			if (a.getAttributes().isEmpty()) {
			} else {
				final Double value = Double.valueOf(a.getAttributes()
						.get("retentionindex").toString());

				if (value == 0) {

				} else if (last == Double.MIN_VALUE) {
					last = value;
					count++;
				} else {
					last = last + value;
					count++;
				}
			}
		}
		return last / count;
	}

	/**
	 * calculates average retiontime for this bin for all samples of the same
	 * day like the given sample
	 * 
	 * @param id
	 * @param sample
	 * @return
	 * @throws BinBaseException
	 * @throws BinBaseException
	 */
	public Double getAverageRetentionTimeForBin(final int id,
			final String sample) throws BinBaseException {
		Map<Integer, Double> map = this.averageRTForSample
				.get(calculateTimeOfSample(sample));

		if (map == null) {
			return Double.NEGATIVE_INFINITY;
		}
		return map.get(id);
	}

	protected double calcuateAverageRetentionTimeForBin(final int id,
			final String sample) {

		try {

			final List<ContentObject<Double>> x = getMassspecsForBinAndDate(id,
					calculateTimeOfSample(sample));

			getLogger().info(
					"need to calculate " + x.size()
							+ " spectra to find the average value of bin " + id
							+ " and sample " + sample);
			double last = Double.MIN_VALUE;
			int count = 0;

			for (final ContentObject<Double> a : x) {
				final Double value = Double.valueOf(a.getAttributes()
						.get("retentiontime").toString());
				if (value <= 0) {
					getLogger().debug("object is null so is ignored");
				} else if (last == Double.MIN_VALUE) {
					last = value;
					count++;
				} else {
					last = last + value;
					count++;
				}
			}

			getLogger().info(
					"found for date: " + count + " - " + sample + " and bin "
							+ id);
			return last / count;
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	public Double getAverageRetentionIndexForBin(final int id,
			final String sample) throws BinBaseException {
		Map<Integer, Double> map = this.averageRIForSample
				.get(calculateTimeOfSample(sample));

		if (map == null) {
			return Double.NEGATIVE_INFINITY;
		}
		return map.get(id);
	}

	/**
	 * returns all massspecs for the given bin and date
	 * 
	 * @param id
	 * @param date
	 * @return
	 * @throws BinBaseException
	 */
	protected List<ContentObject<Double>> getMassspecsForBinAndDate(int id,
			long date) throws BinBaseException {

		if (isIndexed()) {
			List<ContentObject<Double>> result = this.index
					.getMassspecsForBinAndDate(id, date);

			if (result != null) {
				return result;
			}
		}

		final List<ContentObject<Double>> x = getMassspecsForBin(id);
		final List<ContentObject<Double>> result = new ArrayList<ContentObject<Double>>();

		for (final ContentObject<Double> a : x) {

			if (a.getAttributes().isEmpty()) {
				// should not really be...
			} else {

				final String second = this.getSample(
						Integer.parseInt(a.getAttributes().get("sample_id")))
						.getValue();
				long s = calculateTimeOfSample(second);

				if (date == s) {
					result.add(a);
				}
			}
		}
		return result;
	}

	protected double calculateAverageRetentionIndexForBin(final int id,
			final String sample) {
		try {
			final List<ContentObject<Double>> x = getMassspecsForBinAndDate(id,
					calculateTimeOfSample(sample));
			double last = Double.MIN_VALUE;
			int count = 0;
			for (final ContentObject<Double> a : x) {
				final Double value = Double.valueOf(a.getAttributes()
						.get("retentionindex").toString());
				if (value <= 0) {
					getLogger().debug("object is null so is ignored");
				} else if (last == Double.MIN_VALUE) {
					last = value;
					count++;
				} else {
					last = last + value;
					count++;
				}
			}

			getLogger().debug("found for date: " + count + " - " + sample);
			return last / count;
		} catch (BinBaseException e) {
			throw new RuntimeException(e.getMessage(), e);

		}
	}

	/**
	 * calculates the measurment data for the given sample
	 * 
	 * @param sample
	 * @return
	 * @throws BinBaseException
	 */
	public final long calculateTimeOfSample(final String sample)
			throws BinBaseException {

		if (resolver == null) {
			resolver = new RemoteSampleTimeResolver();
		}

		Date f = times.get(sample);

		if (f == null) {
			try {
				getLogger().debug(
						"putting timestamp in cache for sample " + sample);

				f = new Date(resolver.resolveTime(sample));
			} catch (BinBaseException e) {
				if (e.getMessage().indexOf("couldn't find file") > -1) {
					getLogger().warn(e.getMessage());

					try {
						f = SampleDate.createInstance(sample).getDate();
					} catch (PatternException ex) {
						getLogger()
								.warn("sorry there was an error with the filename and we assume a date from today as default! "
										+ e.getMessage(), e);
						f = new Date();
					}

				} else {
					throw e;
				}
			} catch (NullPointerException e) {
				getLogger().error(e.getMessage(), e);
			}

			times.put(sample, DateUtil.stripTime(f));

		}

		getLogger().debug(sample + " - " + DateUtil.stripTime(f).getTime());
		return DateUtil.stripTime(f).getTime();
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 1, 2005
	 * @see edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile#write(Writer)
	 */
	@Override
	public void write(final Writer writer) throws IOException {

		for (int i = 0; i < getData().size(); i++) {
			final List list = (List) getData().get(i);

			for (int x = 0; x < list.size(); x++) {
				// we want to wrap these objects in brackets
				if (list.get(x) instanceof ZeroObject) {
					writer.write("[" + ((FormatObject) list.get(x)).getValue()
							+ "]\t");
				} else if (list.get(x) instanceof NullObject) {
					writer.write("[" + ((FormatObject) list.get(x)).getValue()
							+ "]\t");
				} else if (list.get(x) instanceof FormatObject) {
					writer.write(((FormatObject) list.get(x)).getValue() + "\t");
				} else {
					writer.write(list.get(x) + "\t");
				}
			}

			writer.write("\n");
		}

		writer.flush();
	}

	public boolean containsBin(final int id) {
		return getBin(id) != null;
	}

	public boolean containsGroup(final int group) {
		return getBinsForGroup(group).size() > 0;
	}

	/**
	 * returns the class
	 * 
	 * @param string
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List getClazz(final String string) {
		final List<FormatObject<String>> data = getColumn(1);
		final List<Object> result = new ArrayList<Object>();

		int i = 0;
		for (final FormatObject<String> o : data) {
			if (o.getValue() != null) {
				if (o.getValue().equals(string)) {
					result.add(getRow(i));
				}
			}
			i++;
		}
		return result;
	}

	/**
	 * returns the count of classes
	 * 
	 * @return
	 */
	public int getClazzCount() {
		return getClazzNames().size();
	}

	/**
	 * returns all the class names
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> getClazzNames() {
		final List<FormatObject<String>> data = getColumn(1);
		final List<String> result = new ArrayList<String>();

		int i = 0;
		for (final FormatObject<String> o : data) {
			if (o instanceof HeaderFormat<?>) {
			} else if (o instanceof NullObject<?>) {

			} else if (o.getValue() != null) {
				if (o.getValue().toString().length() == 0) {
				} else if (result.contains(o.getValue()) == false) {
					result.add(o.getValue());
				}
			}
			i++;
		}
		return result;
	}

	/**
	 * initialize internal statistics
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void initialize() throws Exception {

		getLogger().info("initialize datafile");

		if (isIndexed()) {
			getLogger().info("indexing the datafile...");
			this.index = new ResultDataFileIndex(this);
			this.index.build();
			getLogger().info("file is indexed...");
		} else {
			getLogger().info("indexing is disabled");
		}

		// initialize our resolver
		if (resolver == null) {
			resolver = new RemoteSampleTimeResolver();
		}
		getLogger().info("clearing all caches");
		averageRI.clear();
		averageRT.clear();

		averageRTForSample.clear();
		averageRIForSample.clear();

		getLogger().info("filling retention index cache for optimized access");
		fillRICache();

		getLogger().info("done with intializing");

	}

	/**
	 * adds a value to the average retentime cache
	 * 
	 * @param binId
	 * @param time
	 */
	protected void addToAverageRetentionTimeCache(int binId, double time) {
		averageRT.put(binId, time);
	}

	/**
	 * takes care of the retention index and retention time cache
	 * 
	 * @throws ConfigurationException
	 */
	protected void fillRICache() throws ConfigurationException {

		final List<HeaderFormat<String>> binIds = this.getBins();
		// calculate average times

		ExecutorService service = Executors.newCachedThreadPool();

		// start a thread to calculate the average retention time for a bin
		service.submit(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				Thread.currentThread().setName("initialize-avg-rt");
				long before = System.currentTimeMillis();

				for (final FormatObject<?> binId : binIds) {
					int id = Integer.parseInt(binId.getAttributes().get("id")
							.toString());

					double time = calculateAverageRetentionTimeForBin(id);
					addToAverageRetentionTimeCache(id, time);

				}
				long after = System.currentTimeMillis();
				getLogger().info(
						"required time: " + ((after - before))
								+ " ms to calculate average time for "
								+ binIds.size() + " bins");

				return null;
			}

		});

		// start a thread to calculate the average retention index for a bin
		service.submit(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				Thread.currentThread().setName("initialize-avg-ri");
				long before = System.currentTimeMillis();

				for (final FormatObject<?> binId : binIds) {

					int id = Integer.parseInt(binId.getAttributes().get("id")
							.toString());

					double time = calculateAverageRetentionIndexForBin(id);

					addToAverageRetentionIndexCache(id, time);

				}
				long after = System.currentTimeMillis();

				getLogger().info(
						"required time: " + ((after - before))
								+ " ms to calculate average time for "
								+ binIds.size() + " bins");

				return null;
			}
		});

		// calculate average times based on samples

		List<SampleObject<String>> samples = this.getSamples();

		for (SampleObject<String> sample : samples) {
			final String s = sample.getValue();

			service.submit(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					Thread.currentThread().setName(
							"initialize-avg-ri/rt sample: " + s);

					Map<Integer, Double> mapRT = new ConcurrentHashMap<Integer, Double>();
					Map<Integer, Double> mapRI = new ConcurrentHashMap<Integer, Double>();

					long before = System.currentTimeMillis();
					for (FormatObject<?> binId : binIds) {
						int id = Integer.parseInt(binId.getAttributes()
								.get("id").toString());

						double rtTime = calcuateAverageRetentionTimeForBin(id,
								s);
						double riTime = calculateAverageRetentionIndexForBin(
								id, s);

						mapRT.put(id, rtTime);
						mapRI.put(id, riTime);

					}

					long after = System.currentTimeMillis();
					long date = calculateTimeOfSample(s);
					cacheRIDate(mapRI, date);
					cacheRTDate(mapRT, date);

					getLogger().info(
							"required time: " + ((after - before) / 1000 / 60)
									+ " minutes to calculate average time for "
									+ binIds.size() + " bins");
					return null;
				}
			});
		}

		ExecutorsServiceFactory.shutdownService(service);
	}

	protected final void addToAverageRetentionIndexCache(int id, double time) {
		averageRI.put(id, time);
	}

	protected final void cacheRIDate(Map<Integer, Double> mapRI, long date) {
		averageRIForSample.put(date, mapRI);
	}

	protected void cacheRTDate(Map<Integer, Double> mapRT, long date) {
		averageRTForSample.put(date, mapRT);
	}

	// contains average ri
	Map<Integer, Double> averageRI = new ConcurrentHashMap<Integer, Double>();

	// contains average rt
	Map<Integer, Double> averageRT = new ConcurrentHashMap<Integer, Double>();

	/**
	 * contains the average rt for a sample
	 */
	Map<Long, Map<Integer, Double>> averageRTForSample = new ConcurrentHashMap<Long, Map<Integer, Double>>();

	/**
	 * contains average ri for a sample
	 */
	Map<Long, Map<Integer, Double>> averageRIForSample = new ConcurrentHashMap<Long, Map<Integer, Double>>();

	/**
	 * updates the internal group cache
	 */
	public void updateGroupCache() {
		for (HeaderFormat<String> bin : this.getBins()) {
			Integer groupId = Integer
					.parseInt(bin.getAttributes().get("group"));

			if (groupId > 0) {
				Set<String> names = groupCache.get(groupId);

				if (names == null) {
					names = new HashSet<String>();
				}

				names.add(bin.getValue());

				groupCache.put(groupId, names);
			}
		}

		getLogger()
				.info("generated group cache with " + groupCache.size()
						+ " elements");
	}

}
