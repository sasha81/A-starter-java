package org.composer.core.services;

import org.composer.core.model.*;

public interface IModelToFlux {
    public  FluxMessageContainer<?> getFluxUserGRPCContainer(CompareUsersModel body);
    public FluxMessageContainer<?> getFluxUserAMQPContainer(CompareUsersModel body);
    public FluxMessageContainer<?> getFluxUserRestContainer(CompareUsersModel body);

    public FluxMessageContainer<?> getFluxResults(CompareUsersModel body);
}
