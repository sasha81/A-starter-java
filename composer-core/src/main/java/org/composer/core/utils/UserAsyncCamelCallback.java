package org.composer.core.utils;


import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.composer.core.converters.GetUserModel;
import org.composer.core.model.XTaskModel;
import users.Users;

import java.util.stream.Collectors;

public class UserAsyncCamelCallback extends AbstractAsyncCameCallback<Users.UsersWithGroupsDto>{
    public UserAsyncCamelCallback(Exchange exchange, AsyncCallback callback) {
        super(exchange, callback);
    }
    @Override
    public void onNext(Users.UsersWithGroupsDto value) {
        XTaskModel body =  this.exchange.getMessage().getBody(XTaskModel.class);
        body.getGrpc_step().setOutput(value.getUsersWithGroupsList().stream().map(GetUserModel::fromDto).collect(Collectors.toList()));
        this.exchange.getIn().setBody(body);
    }
    @Override
    public void onError(Throwable t) {
        this.exchange.setException(t);
    }
}
