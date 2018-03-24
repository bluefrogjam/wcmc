package edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.modify;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.MassSpecModifier;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;

/**
 * removes a defiend ion from the filter
 * @author wohlgemuth
 *
 */
public class RemoveIonModifier extends MassSpecModifier{

	private Set<Integer> ions = new HashSet<Integer>();
	
	public RemoveIonModifier() {
	}

	@Override
	protected void doConfigure(List<Element> e) {
		for(Element el : e){
			if(el.getName().equals("ion")){
				ions.add(Integer.parseInt(el.getAttributeValue("value")));
			}
		}
	}

	/**
	 * removes the specified ions from the spectra
	 */
	@Override
	public Map<String, Object> modify(Map<String, Object> spectra) {

		String spectraString = (String) spectra.get("spectra");
		double[][] converted = ValidateSpectra.convert(spectraString);
		
		for(Integer i: ions){
			converted[i+1][ValidateSpectra.FRAGMENT_ABS_POSITION] = 0;
			converted[i+1][ValidateSpectra.FRAGMENT_REL_POSITION] = 0;			
		}
		
		spectraString = ValidateSpectra.convert(converted);
		spectra.put("spectra",spectraString);
		spectra.put("CALCULATED_SPECTRA",ValidateSpectra.convert(spectraString));
		
		return spectra;
	}

}
