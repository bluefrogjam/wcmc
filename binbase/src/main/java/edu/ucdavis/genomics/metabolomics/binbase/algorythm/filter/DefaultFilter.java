/*
 * Created on 03.11.2004
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter;

import java.util.Map;

import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsService;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsServiceFactory;


/**
 * a default filter accepting everything
 * @author wohlgemuth
 */
public final class DefaultFilter implements MassSpecFilter {
    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter.MassSpecFilter#accept(Map)
     */
    public boolean accept(Map spec) {
        return true;
    }
    

	@Override
	public DiagnosticsService getDiagnosticsService() {
		return service;
	}

	private DiagnosticsService service = DiagnosticsServiceFactory
			.newInstance().createService();
}
