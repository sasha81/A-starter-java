package org.composer.core.interceptors;

import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;


public class ForwardClientCallWithMetadata<ReqT, RespT> extends ForwardingClientCall<ReqT, RespT> {
    private final ClientCall<ReqT, RespT> delegate;
    private final Metadata metadata;
    public ForwardClientCallWithMetadata(Metadata metadata, ClientCall<ReqT, RespT> delegate){
        this.delegate=delegate;
        this.metadata = metadata;
    }

    @Override
    protected ClientCall<ReqT, RespT> delegate() {
        return delegate;
    }

}
