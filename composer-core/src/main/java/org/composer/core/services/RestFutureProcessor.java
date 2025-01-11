package org.composer.core.services;

import org.composer.core.converters.GetUserModel;
import org.composer.core.model.ModelUser;
import org.composer.core.converters.RestModelUserDto;
import org.composer.core.model.CompareUsersModel;
import org.apache.camel.Exchange;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class RestFutureProcessor extends AbstractFutureAsyncProcessor<Consumer<HttpHeaders>,String,RestModelUserDto[]>{
    private final WebClient webClient;
    private final String uri;
    public RestFutureProcessor(WebClient webClient, String uri) {
        this.webClient = webClient;
        this.uri = uri;
    }

    @Override
    protected CompletableFuture<RestModelUserDto[]> getOutputFutureMethod(Exchange exchange, String input, Consumer<HttpHeaders> metadata) {
        return this.webClient.get().uri(uri).headers(metadata)
                .retrieve()
                .bodyToMono(RestModelUserDto[].class)
                .toFuture();
    }

    @Override
    protected String getInputFromExchange(Exchange exchange) {
        return exchange.getMessage().getBody(CompareUsersModel.class).getRest_step().getInput();
    }

    @Override
    protected Consumer<HttpHeaders> getMetadataFromExchange(Exchange exchange) {
        return httpHeaders -> {
            httpHeaders.add("correlationId", exchange.getMessage().getBody(CompareUsersModel.class).getTask_id());
        };

    }

    @Override
    protected Function<RestModelUserDto[], Exchange> getExchangeFutureMethod(Exchange exchange) {
        return message->{

            CompareUsersModel body = setBody(exchange,List.of(message).stream().map(GetUserModel::fromDto).toList() );
            exchange.getMessage().setBody(body);
            return exchange;
        };
    }

    @Override
    protected Function<Throwable, Exchange> getErrorHandler(Exchange exchange){
        return err->{

            CompareUsersModel body = setError(exchange, getErrorMessage(err));
            exchange.getMessage().setBody(body);
            exchange.setException(err);
            return exchange;
        };
    }

    public static String getErrorMessage(Throwable t){
        return t.getMessage();
    }

    public static CompareUsersModel setBody(Exchange exchange, List<ModelUser> restResult){
        CompareUsersModel body =  exchange.getMessage().getBody(CompareUsersModel.class);
        body.getRest_step().setOutput(restResult);
        return body;
    }

    public static CompareUsersModel setError(Exchange exchange, String errorMsg){
        CompareUsersModel body =  exchange.getMessage().getBody(CompareUsersModel.class);
        body.getRest_step().setErrorMessage(errorMsg);
        return body;
    }
}
