package org.composer.core.services;

import org.apache.camel.Exchange;

public interface IReactorSinkService {

    void notifyAboutRestStep(Exchange exchange);

    void notifyAboutAMQPStep(Exchange exchange);

    void notifyAboutGRPCStep(Exchange exchange);

    void notifyAboutFinished(Exchange exchange);

    void close(Exchange exchange);
}
