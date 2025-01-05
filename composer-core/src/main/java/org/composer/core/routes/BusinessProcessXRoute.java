package org.composer.core.routes;



import org.composer.core.model.XTaskModel;
import org.composer.core.services.AMQPFutureProcessor;
import org.composer.core.services.IReactorSinkService;
import org.composer.core.services.RestFutureProcessor;
import org.composer.core.utils.ThreadPoolShutdownStrategy;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ThreadPoolBuilder;
import org.composer.core.services.GRPCRunnableAsyncProcessor;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import users.UsersServiceGrpc;
import java.util.concurrent.ExecutorService;

@Component
public class BusinessProcessXRoute extends RouteBuilder {

    @GrpcClient("nest-grpc")
    private UsersServiceGrpc.UsersServiceStub nestStub;

    @Autowired
    private AsyncRabbitTemplate rabbitTemplate;
    @Autowired
    private WebClient webClient;

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rest.url}")
    private String restUrl;

    @Value("${rabbitmq.nest-routingkey}")
    private String nestRoutingkey;

    private static final String DESTINATION_FOLDER
            = "src/test/exceptions";
    private final BusinessProcessXService businessProcessXService;

    private final IReactorSinkService reactorSinkService;

    private final int DELAY = 0;

    public BusinessProcessXRoute(BusinessProcessXService businessProcessXService, IReactorSinkService reactorSinkService) {
        this.businessProcessXService = businessProcessXService;

        this.reactorSinkService = reactorSinkService;
    }


    @Override
    public void configure() throws Exception {
        onException(Exception.class)
                .process(exchange -> {}).id("X_exception_processor")
                .log(LoggingLevel.ERROR,"Composer Core Exception received:  ${exception}")
                .continued(true)
        ;

        CamelContext context = getContext();
        ExecutorService executorService = new ThreadPoolBuilder(context)
                .poolSize(10).maxPoolSize(15).maxQueueSize(100).build("CustomThreadPool");

        context.setShutdownStrategy(new ThreadPoolShutdownStrategy(context, executorService));

        from("direct:new_X_task")
                .log(LoggingLevel.INFO, "Init a new X task for the input: ${body}")
                .log(LoggingLevel.INFO, "Current thread: " + Thread.currentThread().getId())
                .process(exchange -> {
                    XTaskModel xTask = exchange.getMessage().getBody(XTaskModel.class);
                    exchange.getMessage().setHeader("id", xTask.getTask_id());
                })

                .log(LoggingLevel.INFO, "Id: ${header.id}")
                .log(LoggingLevel.INFO, "Current thread: " + Thread.currentThread().getId())
                .to("direct:X_GRPC_step");


        from("direct:X_Rest_step").id("X_Rest_step")
                .log(LoggingLevel.INFO, "Current thread: " + Thread.currentThread().getId())
                .setBody(body())
                .process(new RestFutureProcessor(webClient, restUrl)).id("Rest_Async_Processor")
                .bean(reactorSinkService, "notifyAboutRestStep")
                .log("ID: ${header.id}")
                .to("direct:X_AMQP_step")
        ;



        from("direct:X_finish")
                .bean(businessProcessXService, "process_X_final")
                .bean(reactorSinkService, "notifyAboutFinished")
                .log("ID: ${header.id}")
                .to("direct:close")
        ;

        from("direct:X_AMQP_step").id("X_AMQP_step")
                .process(new AMQPFutureProcessor(rabbitTemplate,exchangeName,nestRoutingkey)).id("AMQP_Async_Processor")
                .bean(reactorSinkService, "notifyAboutAMQPStep")
                .to("direct:X_finish")
        ;


        from("direct:X_GRPC_step").id("X_GRPC_step")
                .log(LoggingLevel.INFO, "Current thread: " + Thread.currentThread().getId())
                .process(new GRPCRunnableAsyncProcessor(executorService, nestStub)).id("GRPC_Async_Processor")
                .bean(reactorSinkService, "notifyAboutGRPCStep")
                .to("direct:X_Rest_step")
        ;

        from("direct:close")
                .bean(reactorSinkService, "close");
    }
}
