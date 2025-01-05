package org.composer.core.utils;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.engine.DefaultShutdownStrategy;
import org.apache.camel.spi.RouteStartupOrder;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class ThreadPoolShutdownStrategy extends DefaultShutdownStrategy {
    private ExecutorService executorService;
    public ThreadPoolShutdownStrategy(CamelContext camelContext, ExecutorService executorService){
        super(camelContext);
        this.executorService = executorService;
    }

    @Override
    public void shutdown(CamelContext context, List<RouteStartupOrder> routes) throws Exception{
        System.out.printf("Shutting down a custom executionService");
        context.getExecutorServiceManager().shutdownGraceful(this.executorService);
        super.shutdown(context,routes);
    }
}
