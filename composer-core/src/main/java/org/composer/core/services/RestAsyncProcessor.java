package org.composer.core.services;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.composer.core.converters.GetUserModel;
import org.composer.core.converters.RestModelUserDto;
import org.composer.core.model.XTaskModel;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class RestAsyncProcessor implements AsyncProcessor {
    private WebClient webClient;
    public RestAsyncProcessor(WebClient webClient){
        this.webClient=webClient;
    }
    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        CompletableFuture<Exchange> future = this.processAsync(exchange);
        future.whenComplete((res,err)->{
            callback.done(false);
        });
        return false;
    }

    @Override
    public CompletableFuture<Exchange> processAsync(Exchange exchange) {

        return this.webClient.get().uri("/users/all")
                .retrieve()
                .bodyToMono(RestModelUserDto[].class)
                .toFuture().thenApply(message->{
                    XTaskModel body =  exchange.getMessage().getBody(XTaskModel.class);

            body.getRest_step().setOutput(Stream.of(message).map(GetUserModel::fromDto).toList());
            exchange.getIn().setBody(body);
            return exchange;
        }).exceptionally(err-> {
                    exchange.setException(err);
                    return exchange;
                }
        );
    }

    @Override
    public void process(Exchange exchange) throws Exception {

    }
}
