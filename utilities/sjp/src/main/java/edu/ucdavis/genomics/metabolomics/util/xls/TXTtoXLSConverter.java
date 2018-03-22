/**
 * 
 */
package edu.ucdavis.genomics.metabolomics.util.xls;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Workbook;

import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.FormatObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.MetaObject;

/**
 * @author wohlgemuth
 * converts a txt tab delimeted inputstream to an xls outputstream
 */
public class TXTtoXLSConverter {

	/**
	 * 
	 */
	public TXTtoXLSConverter() {
		super();
	}

	
	/**
	 * 
	 * @param stream the tab delimited inputstream
	 * @return the workbook
	 */
	public Workbook convert(InputStream stream) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		Splitter splitter = new ColoredSplitToSheets();
		
		String line = null;
		
		int i = 0;
		
		while((line = reader.readLine()) != null){
			String[] data = line.split("\t");
			if(i == 0){
				splitter.setHeader(true);
			}
			else{
				splitter.setHeader(false);
			}
			Vector <FormatObject<?>>vector = new Vector<FormatObject<?>>();
			
			for(int x = 0; x < data.length; x++){
				vector.add(new MetaObject<String>(data[x]));
			}
			splitter.addLine(vector);
			i++;
		}
		return splitter.getBook();
	}
	
	/**
	 * args[0] = input file
	 * args[1] = output file
	 * @param args
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		new TXTtoXLSConverter().convert(new FileInputStream(args[0])).write(new FileOutputStream(args[1]));
	}
}
