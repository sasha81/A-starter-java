package org.composer.core.services;

import org.composer.core.converters.GetUserModel;
import org.composer.core.model.ModelUser;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.utils.Task;
import org.example.common.utils.TriConsumer;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import users.Users;
import users.UsersServiceGrpc;
import org.composer.core.utils.CustomCamelAsyncTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class GRPCAsyncProcessor implements AsyncProcessor {

    private final BiConsumer<Exchange, Users.UsersWithGroupsDto> onNextCamelCallback = (exchange, users)->{
        CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
        List<ModelUser> modelUserList = users.getUsersWithGroupsList().stream().map(GetUserModel::fromDto).toList();
        var  currentTask = (Task<String, String, List<ModelUser>>)body.getCurrentTask();
        currentTask.setOutput(modelUserList);
        exchange.getIn().setBody(body);
    };

    private final TriConsumer<Metadata, String, StreamObserver<Users.UsersWithGroupsDto>> callTask =(metadata, input, customTaskCallback)->{
        this.nestStub.findAllUsers(Users.Empty.getDefaultInstance(),customTaskCallback);
    };

    private final Function<Exchange,Metadata> getMetadataFromExchange = (exchange)-> {
        Metadata md = new Metadata();
        md.put(Metadata.Key.of("CorrelationId", ASCII_STRING_MARSHALLER), exchange.getExchangeId());
        return md;
    };

    private final Function<Exchange,String> getInputFromExchange = (exchange)-> {
        CompareUsersModel body =  exchange.getMessage().getBody(CompareUsersModel.class);
        String arg = body.getCurrentTask().getInput();
        return arg;
    };

   private UsersServiceGrpc.UsersServiceStub nestStub;
   private ExecutorService executorService=null;

   public GRPCAsyncProcessor(UsersServiceGrpc.UsersServiceStub nestStub){
       this.nestStub = nestStub;

   }
    public GRPCAsyncProcessor(UsersServiceGrpc.UsersServiceStub nestStub, ExecutorService executorService){
       this(nestStub);
        this.executorService = executorService;

    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback camelCallback) {
        ExecutorService executor;
       if(this.executorService==null){
           executor = exchange.getContext().getExecutorServiceManager().newFixedThreadPool(this,"GRPC",2);
       }else{
           executor=this.executorService;
       }
        executor.submit(getTask(exchange, camelCallback));
        return false;
    }

    @Override
    public CompletableFuture<Exchange> processAsync(Exchange exchange) {
        return null;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

    }


    private Runnable getTask(Exchange exchange, AsyncCallback camelCallback){
        CustomCamelAsyncTask<Metadata,String, Users.UsersWithGroupsDto> task =
                CustomCamelAsyncTask.<Metadata, String, Users.UsersWithGroupsDto>builder()
                        .exchange(exchange)
                        .camelCallback(camelCallback)
                        .getMetadata(getMetadataFromExchange)
                        .getInput(getInputFromExchange)
                        .callTask(callTask)
                        .onNextMethod(onNextCamelCallback)
                .build();
        return  task;
    }
}
