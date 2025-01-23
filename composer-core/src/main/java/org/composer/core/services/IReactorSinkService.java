package org.composer.core.services;

import org.apache.camel.Exchange;

public interface IReactorSinkService {

    void notifyAboutCreateStep(Exchange exchange);

    void notifyAboutRestStep(Exchange exchange);

    void notifyAboutAMQPStep(Exchange exchange);

    void notifyAboutGRPCStep(Exchange exchange);

    void notifyAboutResultStep(Exchange exchange);

    void close(Exchange exchange);
}
