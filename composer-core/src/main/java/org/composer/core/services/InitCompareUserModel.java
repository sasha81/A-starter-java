package org.composer.core.services;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.model.Specs;

public class InitCompareUserModel implements Processor {
    private  final ISpecToModel specToModel;

    public InitCompareUserModel(ISpecToModel specToModel) {
        this.specToModel = specToModel;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Specs spec = exchange.getMessage().getBody( Specs.class);
        exchange.getMessage().setHeader("id", spec.getTaskId());
        try{
            CompareUsersModel task = this.specToModel.getModelFromSpecs(spec);

            task.setNextTask();
            exchange.getMessage().setBody(task);
            String executor = task.getCurrentTask().getExecutor();
            exchange.getMessage().setHeader("executor",executor);

        } catch (Exception e){
            exchange.getMessage().setHeader("initError",e.getMessage());
            exchange.getMessage().setHeader("executor","close");
        }


    }
}
