package edu.ucdavis.genomics.metabolomics.sjp;

import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;

import java.io.*;

/**
 * finds the right parser for you based on a given source
 * 
 * @author wohlgemuth
 */
public abstract class Finder {

	/**
	 * searches for a parser for you
	 * 
	 * @param in
	 * @return
	 */
	protected abstract Parser find(BufferedReader in) throws ParserException, IOException;

	public Parser find(Reader in) throws ParserException, IOException {
		return find(new BufferedReader(in));
	}

	public Parser find(InputStream in) throws ParserException, IOException {
		return find(new InputStreamReader(in));
	}

	public Parser find(String in) throws ParserException, IOException {
		return find(new StringReader(in));
	}

	public Parser find(File in) throws ParserException, IOException {
		return find(new FileReader(in));
	}

}
