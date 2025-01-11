package org.composer.core.services;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.composer.core.model.CompareUsersModel;

public class SetNextTaskProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        CompareUsersModel model = exchange.getMessage().getBody( CompareUsersModel.class);
        model.setNextTask();
        exchange.getMessage().setHeader("executor",model.getCurrentTask().getExecutor());
    }
}
