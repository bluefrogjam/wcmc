package edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter;

import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsService;
import edu.ucdavis.genomics.metabolomics.binbase.diagnostics.DiagnosticsServiceFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wohlgemuth on 11/17/15.
 */
public class CombinedFilter implements MassSpecFilter
{

    List<MassSpecFilter> filterList = new ArrayList<MassSpecFilter>();


    public CombinedFilter(List<MassSpecFilter> filters){
        this.filterList = filters;
    }

    @Override
    public boolean accept(Map<String, Object> map) {

        for(MassSpecFilter filter : filterList){
            if(filter.accept(map)){
                return true;
            }
        }
        return false;
    }


    @Override
    public DiagnosticsService getDiagnosticsService() {
        return service;
    }

    private DiagnosticsService service = DiagnosticsServiceFactory
            .newInstance().createService();
}
