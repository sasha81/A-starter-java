package org.composer.core.services;

import org.apache.camel.Exchange;

public interface IReactorSinkService {

    void notifyAboutRestStep(Exchange exchange);

    void notifyAboutAMQPStep(Exchange exchange);

    void notifyAboutGRPCStep(Exchange exchange);

    void notifyAboutResult(Exchange exchange);

    void close(Exchange exchange);
}
