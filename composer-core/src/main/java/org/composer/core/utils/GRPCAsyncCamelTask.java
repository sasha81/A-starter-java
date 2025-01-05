package org.composer.core.utils;

import io.grpc.Metadata;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;

public abstract class GRPCAsyncCamelTask<I,O> extends AbstractCamelAsyncTask<Metadata,I,O> {
    public GRPCAsyncCamelTask(Exchange exchange, AsyncCallback camelCallback) {
        super(exchange, camelCallback);

    }
}
