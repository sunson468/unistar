package com.up1234567.unistar.springcloud.discover.rest;

import com.up1234567.unistar.springcloud.discover.trace.UnistarTraceContext;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.*;

import java.net.URI;

public class UnistarRestTemplate extends RestTemplate {

    @Override
    protected <T> T doExecute(URI uri, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
        try {
            UnistarTraceContext.addRestTrace(uri);
            T ret = super.doExecute(uri, method, requestCallback, responseExtractor);
            UnistarTraceContext.endTrace(true, null);
            return ret;
        } catch (ResourceAccessException ex) {
            UnistarTraceContext.endTrace(false, ex.getMessage());
            throw ex;
        }
    }
}
