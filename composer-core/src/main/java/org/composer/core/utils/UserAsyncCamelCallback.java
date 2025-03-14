package org.composer.core.utils;


import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.composer.core.converters.GetUserModel;
import org.composer.core.model.ModelUser;
import org.composer.core.model.CompareUsersModel;
import users.Users;

import java.util.List;

public class UserAsyncCamelCallback extends AbstractAsyncCameCallback<Users.UsersWithGroupsDto>{
    public UserAsyncCamelCallback(Exchange exchange, AsyncCallback callback) {
        super(exchange, callback);
    }
    @Override
    public void onNext(Users.UsersWithGroupsDto value) {
        CompareUsersModel body =  this.exchange.getMessage().getBody(CompareUsersModel.class);

        List<ModelUser> modelUserList = value.getUsersWithGroupsList().stream().map(GetUserModel::fromDto).toList();
        var  currentTask = (Task<String, String, List<ModelUser>>)body.getCurrentTask();
        currentTask.setOutput(modelUserList);

        this.exchange.getIn().setBody(body);
    }
    @Override
    public void onError(Throwable t) {
        this.exchange.setException(t);
    }
}
