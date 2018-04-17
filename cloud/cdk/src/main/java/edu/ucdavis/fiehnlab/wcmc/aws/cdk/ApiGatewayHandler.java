package edu.ucdavis.fiehnlab.wcmc.aws.cdk;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.HashMap;
import java.util.Map;

public abstract class ApiGatewayHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {


    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> request, Context context) {

        assert (request != null);

        Map<String, Object> result = evaluateRequest(request);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(result)
                .setHeaders(headers)
                .build();
    }

    protected abstract Map<String, Object> evaluateRequest(Map<String, Object> request);

}
