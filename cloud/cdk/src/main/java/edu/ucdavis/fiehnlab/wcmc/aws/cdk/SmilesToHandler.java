package edu.ucdavis.fiehnlab.wcmc.aws.cdk;

import org.openscience.cdk.exception.CDKException;

import java.util.HashMap;
import java.util.Map;

public abstract class SmilesToHandler extends ApiGatewayHandler {
    @Override
    protected final Map<String, Object> evaluateRequest(Map<String, Object> request) {
        try {
            String smiles[] = request.get("body").toString().split("\n");

            Map<String, Object> results = new HashMap<>();

            for (String smile : smiles) {
                results.put(smile, handleSmile(smile));
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected abstract Map<String,Object> handleSmile(String smile) throws CDKException;
}
