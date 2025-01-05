package org.composer.adapter.services;

import java.util.concurrent.CompletableFuture;

public interface ISendToCamelService {
    CompletableFuture<Object> sendBodyToCamel(String endpointUri, Object body);
}
