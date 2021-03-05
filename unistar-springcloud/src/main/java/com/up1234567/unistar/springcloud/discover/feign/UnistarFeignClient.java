package com.up1234567.unistar.springcloud.discover.feign;

import com.up1234567.unistar.springcloud.discover.trace.UnistarTraceContext;
import com.up1234567.unistar.common.util.StringUtil;
import feign.Client;
import feign.Request;
import feign.Response;

import java.io.IOException;

public class UnistarFeignClient implements Client {

    private Client client;

    public UnistarFeignClient(Client client) {
        this.client = client;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        Response response = client.execute(request, options);
        UnistarTraceContext.setFeignTarget(StringUtil.resolveHost(response.request().url()));
        return response;
    }
}
