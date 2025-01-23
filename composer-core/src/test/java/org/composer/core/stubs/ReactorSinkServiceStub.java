package org.composer.core.stubs;

import org.apache.camel.Exchange;
import org.composer.core.services.IReactorSinkService;


import java.util.function.Consumer;

import static java.util.Objects.isNull;


public class ReactorSinkServiceStub implements IReactorSinkService {

    private Consumer<Exchange> notifyAboutAMQPStep;

    public ReactorSinkServiceStub() {
    }


    @Override
    public void notifyAboutCreateStep(Exchange exchange) {

    }

    @Override
    public void notifyAboutRestStep(Exchange exchange) {

    }

    @Override
    public void notifyAboutAMQPStep(Exchange exchange) {
        int a=1;
        if(isNull(this.notifyAboutAMQPStep)){return;}
        this.notifyAboutAMQPStep.accept(exchange);

    }

    @Override
    public void notifyAboutGRPCStep(Exchange exchange) {

    }

    @Override
    public void notifyAboutResultStep(Exchange exchange) {

    }

    @Override
    public void close(Exchange exchange) {

    }

    public void setNotifyAboutAMQPStep(Consumer<Exchange> notifyAboutAMQPStep) {
        this.notifyAboutAMQPStep = notifyAboutAMQPStep;
    }
}
