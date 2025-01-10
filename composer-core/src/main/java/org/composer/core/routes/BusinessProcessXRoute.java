package org.composer.core.routes;



import org.composer.core.model.Specs;
import org.composer.core.model.XTaskModel;
import org.composer.core.services.*;
import org.composer.core.utils.ThreadPoolShutdownStrategy;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ThreadPoolBuilder;
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
    private  final ISpecToModel specToModel;
    private final int DELAY = 0;

    public BusinessProcessXRoute(BusinessProcessXService businessProcessXService, IReactorSinkService reactorSinkService, ISpecToModel specToModel) {
        this.businessProcessXService = businessProcessXService;

        this.reactorSinkService = reactorSinkService;
        this.specToModel = specToModel;
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

        from("direct:new_CompareUsers_task")
                .log(LoggingLevel.INFO, "Init a new Compare task for the input: ${body}")
                .log(LoggingLevel.INFO, "Current thread: " + Thread.currentThread().getId())
                .process(exchange -> {
                    Specs spec = exchange.getMessage().getBody( Specs.class);
                    XTaskModel task = this.specToModel.getModelFromSpecs(spec);
                    exchange.getMessage().setHeader("id", spec.getTaskId());
                    task.setNextTask();
                    exchange.getMessage().setBody(task);
                    String executor = task.getCurrentTask().getExecutor();
                    exchange.getMessage().setHeader("executor",executor);
                })

                .log(LoggingLevel.INFO, "executor: ${header.executor}")
                .log(LoggingLevel.INFO, "Current thread: " + Thread.currentThread().getId())
                .toD("direct:${header.executor}");


        from("direct:X_Rest_step").id("X_Rest_step")
                .log(LoggingLevel.INFO, "Current thread: " + Thread.currentThread().getId())
                .setBody(body())
                .process(new RestFutureProcessor(webClient, restUrl)).id("Rest_Async_Processor")
                .bean(reactorSinkService, "notifyAboutRestStep")
                .process(exchange -> {
                    XTaskModel model = exchange.getMessage().getBody( XTaskModel.class);
                    model.setNextTask();
                    exchange.getMessage().setHeader("executor",model.getCurrentTask().getExecutor());
                })
                .log("executor: ${header.executor}")
                .toD("direct:${header.executor}")
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
                .process(exchange -> {
                    XTaskModel model = exchange.getMessage().getBody( XTaskModel.class);
                    model.setNextTask();
                    exchange.getMessage().setHeader("executor",model.getCurrentTask().getExecutor());
                })
                .log("executor: ${header.executor}")
                .toD("direct:${header.executor}")
        ;


        from("direct:X_GRPC_step").id("X_GRPC_step")
                .log(LoggingLevel.INFO, "Current thread: " + Thread.currentThread().getId())
                .process(new GRPCRunnableAsyncProcessor(executorService, nestStub)).id("GRPC_Async_Processor")
                .bean(reactorSinkService, "notifyAboutGRPCStep")
                .process(exchange -> {
                    XTaskModel model = exchange.getMessage().getBody( XTaskModel.class);
                    model.setNextTask();
                    exchange.getMessage().setHeader("executor",model.getCurrentTask().getExecutor());
                })
                .log("ID: ${header.id}")
                .toD("direct:${header.executor}")
        ;

        from("direct:close")
                .bean(reactorSinkService, "close");
    }
}
