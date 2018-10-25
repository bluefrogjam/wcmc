/*
 * Created on Oct 8, 2003
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.export;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.binbase.bci.setupX.SetupXProvider;
import edu.ucdavis.genomics.metabolomics.util.SQLObject;
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory;
import edu.ucdavis.genomics.metabolomics.util.math.SpectraArrayKey;
import edu.ucdavis.genomics.metabolomics.util.search.BinarySearch;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Properties;

/**
 * @author wohlgemuth
 * @version Sep 18, 2003 <br>
 *          BinBase
 * @description
 */
public class SampleExport extends SQLObject {
	/**
	 * 
	 * @uml.property name="search"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	BinarySearch search = new BinarySearch();

	DecimalFormat df = new DecimalFormat("####0.00000000");

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement binStatement;

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement quantStatement;

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement sampleStatement;

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement virtualBinStatement;

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement virtualProperty;

	/**
	 * DOCUMENT ME!
	 */
	private PreparedStatement virtualQuantStatement;

	/**
	 * DOCUMENT ME!
	 */
	private StringWriter writer;

	public static void main(String[] args) throws Exception{
		ConnectionFactory factory = ConnectionFactory.createFactory();

		Properties p = System.getProperties();
		p.setProperty("Binbase.database", "binbase");
		p.setProperty("Binbase.type", "3");


		factory.setProperties(p);
		Connection c = factory.getConnection();

		SampleExport export = new SampleExport();
		export.setConnection(c);

		System.out.println(export.getQuantifiedSampleAsXml(559285));

	}
	/**
	 * exportiert eine quantifizierte probe
	 * 
	 * @param sampleId
	 * @return
	 */
	public String getQuantifiedSampleAsXml(int sampleId) {
		try {

			logger.debug("quantify sample for sample id: " + sampleId);

			this.writer = (new StringWriter());

			this.quantStatement.setInt(1, sampleId);
			this.virtualQuantStatement.setInt(1, sampleId);
			this.sampleStatement.setInt(1, sampleId);

			ResultSet sample = this.sampleStatement.executeQuery();

			if (sample.next()) {
				logger.debug("write sample header...");

				String setupXId = null;
				try {
					SetupXProvider provider = SetupXFactory.newInstance().createProvider();
					setupXId = provider.getSetupXId(sample.getString(1));
				} catch (Throwable e) {
					logger.warn("couldnt update setupX id's "  + e.getMessage());
				}

				if (setupXId == null) {
					logger.warn("using default SX id");
					setupXId = sample.getString(3);

					if(setupXId == null){
						logger.warn("\t=> still no id found, using sample name instead!");
						setupXId = sample.getString(1).trim();
					}
				}

				String correctionFailed = "TRUE";
				if(sample.getString(4) == null){
					correctionFailed = "TRUE";
				}
				else{
					correctionFailed = sample.getString(4).trim();
				}

				writer.write("<sample name=\"" + sample.getString(1).trim() + "\" id=\"" + sampleId + "\" class=\"" + sample.getString(2).trim() + " \" setupX=\"" + setupXId.trim()
						+ "\" correctionFailed=\""+ correctionFailed + "\">\n");

				// rufe die bins ab
				ResultSet bin = this.binStatement.executeQuery();

				// schreibt die bins in die datei
				while (bin.next()) {
					String name = bin.getString("name");
					int id = bin.getInt("bin_id");
					int mass = bin.getInt("quantmass");
					this.quantStatement.setInt(2, id);

					ResultSet quant = this.quantStatement.executeQuery();

					writer.write("\t<bin name=\"" + name + "\" id=\"" + id + "\" ");

					// ermitteln der informationen
					if (quant.next()) {
						String spectra = quant.getString("spectra");
						double[][] spec = ValidateSpectra.convert(spectra);
						int position = search.search(spec, SpectraArrayKey.FRAGMENT_ION_POSITION, mass);

						String height = String.valueOf(spec[position][SpectraArrayKey.FRAGMENT_ABS_POSITION]);

						writer.write("spectra_id=\"" + quant.getString("spectra_id").trim() + "\" ");
						writer.write("apex_sn=\"" + quant.getString("apex_sn").trim() + "\" ");
						writer.write("unqiuemass=\"" + quant.getString("uniquemass").trim() + "\" ");
						writer.write("quantmass=\"" + mass + "\" ");
						writer.write("purity=\"" + quant.getString("purity").trim() + "\" ");
						writer.write("match =\"" + quant.getString("match").trim() + "\" ");
						writer.write("signalnoise =\"" + quant.getString("signal_noise").trim() + "\" ");
						writer.write("retentionindex=\"" + quant.getString("retention_index").trim() + "\" ");
						writer.write("retentiontime=\"" + quant.getString("retention_time").trim() + "\" ");

						writer.write("height=\"" + df.format(Double.parseDouble(height)).trim() + "\" ");
						writer.write("problematic=\"" + quant.getString("problematic").trim() + "\" ");
						writer.write("spectra=\"\" ");
						writer.write("sample_id=\"" + sampleId + "\"");
					} else {
						writer.write("spectra_id=\"0\" ");
						writer.write("apex_sn=\"0\" ");
						writer.write("unqiuemass=\"0\" ");
						writer.write("quantmass=\"0\" ");
						writer.write("purity=\"0\" ");
						writer.write("match =\"0\" ");
						writer.write("signalnoise =\"0\" ");
						writer.write("retentionindex=\"0\" ");
						writer.write("retentiontime=\"0\" ");
						writer.write("height=\"" + 0 + "\" ");
						writer.write("problematic=\"\" ");
						writer.write("spectra=\"\"");
						writer.write(" sample_id=\"" + sampleId + "\" ");
					}

					writer.write("/>\n");
					quant.close();
				}

				bin.close();

				// schreibt die virtuellen bins in die datei
				bin = this.virtualBinStatement.executeQuery();

				while (bin.next()) {
					String name = bin.getString("name");
					int id = bin.getInt("bin_id");
					int mass = bin.getInt("quantmass");
					this.virtualQuantStatement.setInt(2, id);

					ResultSet quant = this.virtualQuantStatement.executeQuery();

					writer.write("\t<bin name=\"" + name + "\" id=\"" + id + "\" ");

					// ermitteln der informationen
					if (quant.next()) {
						String spectra = quant.getString("spectra");
						int realBin = quant.getInt("real_bin");

						writer.write("spectra_id=\"" + quant.getString("spectra_id") + "\" ");
						writer.write("apex_sn=\"" + quant.getString("apex_sn") + "\" ");
						writer.write("unqiuemass=\"" + quant.getString("uniquemass") + "\" ");
						writer.write("quantmass=\"" + mass + "\" ");
						writer.write("purity=\"" + quant.getString("purity") + "\" ");
						writer.write("match =\"" + quant.getString("match") + "\" ");
						writer.write("signalnoise =\"" + quant.getString("signal_noise") + "\" ");
						writer.write("retentionindex=\"" + quant.getString("retention_index") + "\" ");
						writer.write("retentiontime=\"" + quant.getString("retention_time") + "\" ");

						writer.write("height=\"" + df.format(this.calculateHeight(spectra, mass, id, realBin)) + "\" ");
						writer.write("spectra=\"" + spectra + "\"");
					} else {
						writer.write("spectra_id=\"0\" ");
						writer.write("apex_sn=\"0\" ");
						writer.write("unqiuemass=\"0\" ");
						writer.write("quantmass=\"0\" ");
						writer.write("purity=\"0\" ");
						writer.write("match =\"0\" ");
						writer.write("signalnoise =\"0\" ");
						writer.write("retentionindex=\"0\" ");
						writer.write("retentiontime=\"0\" ");
						writer.write("height=\"" + 0 + "\" ");
						writer.write("spectra=\"\"");
					}

					writer.write("/>\n");
					quant.close();
				}

				bin.close();
				writer.write("</sample>\n");

				logger.debug("flush writer...");
				writer.flush();
				writer.close();
				sample.close();

				return writer.toString();
			} else {
				sample.close();
				logger.warn("sample not found! with id: " + sampleId);

				return ("<sample name=\"not found\" id=\"" + sampleId + "\" class=\"not found\"/>");
			}
		} catch (Exception e) {
			sendException(e);
		}

		return null;
	}

	/**
	 * exportiert die angegebenen quantifizierten proben
	 * 
	 * @param sampleId
	 * @return
	 */
	public String getQuantifiedSamplesAsXml(int[] sampleId) {
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < sampleId.length; i++) {
			buffer.append(getQuantifiedSampleAsXml(sampleId[i]));
			buffer.append("\n");
		}

		return buffer.toString();
	}

	/**
	 * @see edu.ucdavis.genomics.metabolomics.util.SQLObject#prepareStatements()
	 */
	protected void prepareStatements() throws Exception {
		super.prepareStatements();
		this.quantStatement = this.getConnection().prepareStatement(SQL_CONFIG.getValue(CLASS + ".quant"));
		this.sampleStatement = this.getConnection().prepareStatement(SQL_CONFIG.getValue(CLASS + ".sample"));

		this.binStatement = this.getConnection().prepareStatement(SQL_CONFIG.getValue(CLASS + ".bin"));

		this.virtualQuantStatement = this.getConnection().prepareStatement(SQL_CONFIG.getValue(CLASS + ".virtualquant"));

		this.virtualBinStatement = this.getConnection().prepareStatement(SQL_CONFIG.getValue(CLASS + ".virtualbin"));

		this.virtualProperty = this.getConnection().prepareStatement(SQL_CONFIG.getValue(CLASS + ".virtualProperty"));
	}

	/**
	 * 
	 * @return
	 */
	private double calculateHeight(String spectra, int quantmass, int virtualId, int realBin) {
		try {
			this.virtualProperty.setInt(1, realBin);

			ResultSet result = this.virtualProperty.executeQuery();

			// die bins
			int binA;
			int binB;

			// die ionen
			int ta;
			int sa;
			int tb;
			int sb;

			// die verh?ltnisse
			double ca;
			double cb;

			// die intensit?ten
			double ita;
			double isa;
			double itb;
			double isb;

			// das ergebniss
			// es gibt immer 2 virtuelle bin zu einem eltern spektrum -> in
			// dieser version darum diese l?sung
			// der erste bin
			result.next();

			ta = result.getInt("ion_a");
			sa = result.getInt("ion_b");
			binA = result.getInt("bin_id");
			ca = result.getDouble("ratio");

			// der zweite bin
			result.next();

			tb = result.getInt("ion_a");
			sb = result.getInt("ion_b");
			binB = result.getInt("bin_id");
			cb = result.getDouble("ratio");

			if (binA == virtualId) {
				double[][] spec = ValidateSpectra.convert(spectra);
				int position = 0;

				double i1;
				double i2;

				position = search.search(spec, SpectraArrayKey.FRAGMENT_ION_POSITION, ta);
				i1 = (spec[position][SpectraArrayKey.FRAGMENT_REL_POSITION]);

				position = search.search(spec, SpectraArrayKey.FRAGMENT_ION_POSITION, sa);
				i2 = (spec[position][SpectraArrayKey.FRAGMENT_REL_POSITION]);

				ita = ((ca * i1) - (ca * cb * i2)) / (ca - cb);
				isa = (i1 - (cb * i2)) / (ca - cb);

				if (quantmass == ta) {
					logger.debug(" using ita");

					return ita;
				} else if (quantmass == sa) {
					logger.debug(" using isa");

					return isa;
				} else {
					logger.error(" wrong qunatmass defined for virtual bin: " + virtualId + " using mass: " + ta);

					return ita;
				}
			} else {
				double[][] spec = ValidateSpectra.convert(spectra);
				int position = 0;

				double i1;
				double i2;

				position = search.search(spec, SpectraArrayKey.FRAGMENT_ION_POSITION, tb);
				i1 = (spec[position][SpectraArrayKey.FRAGMENT_REL_POSITION]);

				position = search.search(spec, SpectraArrayKey.FRAGMENT_ION_POSITION, sb);
				i2 = (spec[position][SpectraArrayKey.FRAGMENT_REL_POSITION]);

				itb = ((-cb * i1) + (ca * cb * i2)) / (ca - cb);
				isb = (-i1 + (ca * i2)) / (ca - cb);

				if (quantmass == ta) {
					logger.debug(" using itb");

					return itb;
				} else if (quantmass == sa) {
					logger.debug(" using isb");

					return isb;
				} else {
					logger.error(" wrong qunatmass defined for virtual bin: " + virtualId + " using mass: " + tb);

					return itb;
				}
			}
		} catch (SQLException e) {
			sendException(e);

			return -1;
		}
	}
}
