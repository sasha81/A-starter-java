package org.composer.core.services;

import org.composer.core.converters.GetUserModel;
import org.composer.core.model.ModelUser;
import org.composer.core.model.CompareUsersModel;
import org.example.common.utils.TriConsumer;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import org.apache.camel.Exchange;
import users.Users;
import users.UsersServiceGrpc;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class GRPCRunnableAsyncProcessor extends RunnableCamelAsyncProcessor<Metadata, String, Users.UsersWithGroupsDto> {
    private final UsersServiceGrpc.UsersServiceStub nestStub;

    public GRPCRunnableAsyncProcessor(ExecutorService executorService, UsersServiceGrpc.UsersServiceStub nestStub) {
        super(executorService);
        this.nestStub = nestStub;
    }

    public GRPCRunnableAsyncProcessor(UsersServiceGrpc.UsersServiceStub nestStub) {
        super();
        this.nestStub = nestStub;
    }

    @Override
    protected BiConsumer<Exchange, Users.UsersWithGroupsDto> getOnNextCamelCallback() {
        return GRPCRunnableAsyncProcessor::addGrpcDataToExchange;
    }

    @Override
    protected TriConsumer<Metadata, String, StreamObserver<Users.UsersWithGroupsDto>> getCallTask() {
        return (metadata, input, customTaskCallback) -> {
            this.nestStub.findAllUsers(Users.Empty.getDefaultInstance(), customTaskCallback);
        };
    }

    @Override
    protected Function<Exchange, Metadata> getMetadataFromExchange() {
        return (exchange) -> {
            Metadata md = new Metadata();
            md.put(Metadata.Key.of("CorrelationId", ASCII_STRING_MARSHALLER), exchange.getExchangeId());
            return md;
        };
    }

    @Override
    protected Function<Exchange, String> getInputFromExchange() {
        return (exchange) -> {
            CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
            String arg = body.getGrpc_step().getInput();
            return arg;
        };
    }

    @Override
    protected BiConsumer<Exchange, Throwable> getOnErrorCamelCallback() {
        return (exchange, t) -> {
            CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
            body.getGrpc_step().setErrorMessage(getGRPCErrorMessage(t));
            exchange.getMessage().setBody(body);
            exchange.setException(t);
        };
    }

    public static String stringifyUsers(Users.UsersWithGroupsDto users) {
        List<Users.UserViewDto> userList = users.getUsersWithGroupsList();
        return userList.stream().map(user ->
                "{userId: " + user.getUserId() + ", username: " + user.getUsername() + ", groups:" + stringifyGroup(user.getGroupsList()) + "}").collect(Collectors.joining(",", "[", "]"));
    }

    public static String stringifyGroup(List<Users.Group> groups) {
        return groups.stream().map(group -> "{ groupId:" + group.getGroupId() + ", groupName: " + group.getGroupname() + "}").collect(Collectors.joining(",", "[", "]"));
    }


    public static Exchange addGrpcDataToExchange(Exchange exchange, Users.UsersWithGroupsDto grpcResult) {
        CompareUsersModel body = setBody(exchange, grpcResult);
        exchange.getMessage().setBody(body);
        return exchange;
    }


    public static CompareUsersModel setBody(Exchange exchange, Users.UsersWithGroupsDto grpcResult) {
        CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
        List<ModelUser> modelUserList = grpcResult.getUsersWithGroupsList().stream().map(GetUserModel::fromDto).toList();
        body.getGrpc_step().setOutput(modelUserList);
        return body;
    }

    public static String getGRPCErrorMessage(Throwable t) {
        return t.getMessage();


    }


}
