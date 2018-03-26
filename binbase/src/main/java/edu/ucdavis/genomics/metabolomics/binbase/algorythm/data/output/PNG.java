package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.output;

import edu.ucdavis.genomics.metabolomics.util.io.source.Source;
import edu.ucdavis.genomics.metabolomics.util.statistics.data.DataFile;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

public class PNG implements Writer {

	@Override
	public boolean isDatafileSupported() {
		return false;
	}

	@Override
	public boolean isSourceSupported() {
		return false;
	}

	@Override
	public void write(OutputStream out, DataFile file) throws IOException {

	}

	@Override
	public void write(OutputStream out, Source content) throws IOException {
	}

	@Override
	public void write(OutputStream out, Object content) throws IOException {
		ImageIO.write((RenderedImage) content, "png", out);
	}

	@Override
	public String toString() {
		return "png";
	}
}
