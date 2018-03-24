package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.action;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.ResultDataFile;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output.Writer;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool.BasicProccessable;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.tool.Processable;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;

/**
 * adpater to user actions as processables
 * @author wohlgemuth
 *
 */
public abstract class ActionAdapter extends BasicAction implements Processable  {

	private ActionAdapter myAction = this;
	
	BasicProccessable process = new BasicProccessable(){

		@Override
		public DataFile process(ResultDataFile datafile, Element configuration)
				throws BinBaseException {
			return myAction.processFile(datafile,configuration);
		}

		@Override
		public String getFolder() {
			return myAction.getFolder();
		}

		@Override
		public String getDescription() {
			return myAction.getDescription();
		}
		
	};
	
	public DataFile process(ResultDataFile datafile, Element configuration)
			throws BinBaseException {
		return process.process(datafile, configuration);
	}

	public String getFileIdentifier() {
		return process.getFileIdentifier();
	}

	public DataFile simpleProcess(DataFile datafile, Element configuration)
			throws BinBaseException {
		return process.simpleProcess(datafile, configuration);
	}

	public void setCurrentFolder(String folder) {
		process.setCurrentFolder(folder);
	}

	public void setCurrentId(String id) {
		process.setCurrentId(id);
	}

	public void setDestinationIds(Collection<String> destinationIds) {
		process.setDestinationIds(destinationIds);
	}

	public void writeObject(Object content, Element configuration,
			String identifier) throws IOException {
		process.writeObject(content, configuration, identifier);
	}

	public void writeObject(Object content, String identifier, Writer writer)
			throws IOException {
		process.writeObject(content, identifier, writer);
	}

	public void writeJPEGImage(BufferedImage content, String identifier)
			throws IOException {
		process.writeJPEGImage(content, identifier);
	}

	public void writePNGImage(BufferedImage content, String identifier)
			throws IOException {
		process.writePNGImage(content, identifier);
	}

	public void writeObject(Object content, Element configuration,
			String identifier, String subFolder) throws IOException {
		process.writeObject(content, configuration, identifier, subFolder);
	}

	public void writeObject(Object content, String identifier, Writer writer,
			String subFolder) throws IOException {
		process.writeObject(content, identifier, writer, subFolder);
	}

	public void writePNGImage(BufferedImage content, String identifier,
			String subFolder) throws IOException {
		process.writePNGImage(content, identifier, subFolder);
	}

	public boolean writeResultToFile() {
		return process.writeResultToFile();
	}
	
	protected abstract DataFile processFile(ResultDataFile datafile, Element configuration);

}
