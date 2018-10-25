package edu.ucdavis.fiehnlab.wcmc.aws.cdk;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * simple method to expose a lambda as a an API Gateway response
 */
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

    /**
     * xontains the reqeuest as input and returns a map of objects as result
     * @param request
     * @return
     */
    protected abstract Map<String, Object> evaluateRequest(Map<String, Object> request);

}
