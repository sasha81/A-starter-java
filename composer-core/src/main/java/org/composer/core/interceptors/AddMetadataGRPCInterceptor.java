package org.composer.core.interceptors;

import io.grpc.*;

public class AddMetadataGRPCInterceptor implements ClientInterceptor {

    private Metadata metadata;

    public AddMetadataGRPCInterceptor(Metadata metadata){
        this.metadata = metadata;
    }
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next){
        return new
                ForwardClientCallWithMetadata<ReqT, RespT>(this.metadata,next.newCall(method, callOptions)) {
                    @Override
                    public void start(Listener<RespT> responseListener, Metadata headers) {
                       // logger.info("Added metadata");
                     //   headers.put(Metadata.Key.of("HOSTNAME", ASCII_STRING_MARSHALLER), "MY_HOST");
                        headers.merge(metadata);
                        super.start(responseListener, headers);
                    }
                };
    }
}