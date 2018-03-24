package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.annotation.Unique;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.action.ResultFileAction;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.correction.CorrectionCache;
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.math.Regression;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.SimpleDatafile;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.HeaderFormat;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;

/**
 * generates a simple correction report for a sample
 * 
 * @author wohlgemuth
 * 
 */
@Unique
public class CorrectionReport extends ResultFileAction {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public String getFolder() {
		return "report";
	}

	@Override
	public void runDatafile(ResultDataFile data, Element configuration,
			Source rawdata, Source sop) {

		List<SampleObject<String>> samples = data.getSamples();

		SimpleDatafile file = new SimpleDatafile();

		file.addEmptyColumn("sample");
		file.addEmptyColumn("sample id");
		file.addEmptyColumn("machine");
		
		file.addEmptyColumn("failed");
		file.addEmptyColumn("corrected with sample");		
		file.addEmptyColumn("difference in days");		
		
		file.addEmptyColumn("correction id");
		file.addEmptyColumn("machine");

		file.addEmptyColumn("");
		file.addEmptyColumn("");
		file.addEmptyColumn("");

		try {
			CorrectionCache cache = new CorrectionCache();

			ConnectionFactory factory = ConnectionFactory.getFactory();

			Connection connection = createDatabaseConnection(this.getColumn(),
					factory);
			cache.setConnection(connection);

			PreparedStatement correctedWith = connection
					.prepareStatement("select \"correctedWith\",correction_failed,machine,date from samples where sample_id = ?");
			PreparedStatement correctionData = connection
					.prepareStatement("select sample_name,machine,date from samples where sample_id = ?");

			try {
				for (SampleObject<String> sample : samples) {
					String name = sample.getValue();
					int id = Integer.parseInt(sample.getAttributes().get("id"));

					correctedWith.setInt(1, id);

					ResultSet res = correctedWith.executeQuery();

					if (res.next()) {
						int correctionId = res.getInt(1);
						String failed = res.getString(2);
						String machine = res.getString(3);
						Date sampleDate = new Date(res.getDate(4).getTime());
						
						correctionData.setInt(1, correctionId);
						ResultSet res2 = correctionData.executeQuery();

						if (res2.next()) {
							String correctionName = res2.getString(1);
							Date correctionDate = new Date(res2.getDate(3).getTime());

							long diff = Math.abs(sampleDate.getTime() - correctionDate.getTime());
							
							logger.info("difference: " + diff + " - " + sampleDate + "/" + correctionDate);
							List<Object> list = new Vector<Object>();
							list.add(new HeaderFormat<String>(name));
							list.add(new ContentObject<Integer>(id));
							list.add(new ContentObject<String>(machine));
							list.add(new ContentObject<String>(failed));

							if (Boolean.parseBoolean(failed)) {
								list.add(new ContentObject<String>(
										correctionName));
								list.add(new ContentObject<Double>((double)diff / (double)(24 * 60 * 60 * 1000)));

								list.add(new ContentObject<Integer>(
										correctionId));
								list.add(new ContentObject<String>(res2.getString(2)));
								

							} else {
								list.add(new ContentObject<String>(""));
								list.add(new ContentObject<String>(""));
								list.add(new ContentObject<String>(""));
								list.add(new ContentObject<String>(""));
								

							}
							Regression regression = cache
									.getRTvsRIRegression(correctionId);

							for (String f : regression.getFormulas()) {
								list.add(new ContentObject<String>(f));

							}

							file.addRow(list);
						}
					}
					res.close();
				}

				writeObject(file, configuration, "correction");

			} finally {
				factory.close(connection);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public String getDescription(){
		return "generates a report of the retention index of all samples in this dataset";
	}
}
