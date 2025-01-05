package org.composer.adapter.utils;

import org.composer.adapter.services.ISendToCamelService;

import java.util.concurrent.CompletableFuture;

public class SendToCamelServiceStub implements ISendToCamelService {
    @Override
    public CompletableFuture<Object> sendBodyToCamel(String endpointUri, Object body) {
        return new CompletableFuture<>();
    }
}
