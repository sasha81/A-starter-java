package org.composer.core.utils;

import org.composer.core.model.CompareUsersModel;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import users.Users;
import users.UsersServiceGrpc;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class UserAsyncGRPCCamelTask extends GRPCAsyncCamelTask<String, Users.UsersWithGroupsDto> {
    private final UsersServiceGrpc.UsersServiceStub grpcStub;
    public UserAsyncGRPCCamelTask(Exchange exchange, AsyncCallback camelCallback, UsersServiceGrpc.UsersServiceStub grpcStub) {
        super(exchange, camelCallback);
        this.grpcStub = grpcStub;
    }
    @Override
    protected void callTask(Metadata metadata, String input, StreamObserver<Users.UsersWithGroupsDto> taskCallback) {
        this.grpcStub.findAllUsers(Users.Empty.getDefaultInstance(),taskCallback);
    }
    @Override
    protected Metadata getMetadataFromExchange(Exchange exchange) {
        Metadata md = new Metadata();
        md.put(Metadata.Key.of("CorrelationId", ASCII_STRING_MARSHALLER), exchange.getExchangeId());
        return md;
    }
    @Override
    protected String getInputFromExchange(Exchange exchange) {
        CompareUsersModel body =  this.exchange.getMessage().getBody(CompareUsersModel.class);
        String arg = body.getCurrentTask().getInput();
        return arg;
    }
    @Override
    protected StreamObserver<Users.UsersWithGroupsDto> getAsyncCamelCallback(Exchange exchange, AsyncCallback callback) {
        return new UserAsyncCamelCallback(exchange, callback);
    }
}
