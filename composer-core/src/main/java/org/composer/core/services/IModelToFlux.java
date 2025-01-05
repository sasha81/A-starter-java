package org.composer.core.services;

import org.composer.core.model.*;

import java.util.List;

public interface IModelToFlux {
    public  FluxMessageContainer<?> getFluxUserGRPCContainer(XTaskModel body);
    public FluxMessageContainer<?> getFluxUserAMQPContainer(XTaskModel body);
    public FluxMessageContainer<?> getFluxUserRestContainer(XTaskModel body);

    public FluxMessageContainer<?> getFluxResults(XTaskModel body);
}
