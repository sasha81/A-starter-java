package org.composer.core.stubs;

import org.springframework.amqp.core.AsyncAmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.core.ParameterizedTypeReference;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AsyncAmqpTemplateStub implements AsyncAmqpTemplate {

    private Function<Message,CompletableFuture<Message>> getOutputMessage;
    public AsyncAmqpTemplateStub(Function<Message,CompletableFuture<Message>> getOutputMessage) {
        this.getOutputMessage = getOutputMessage;
    }

    @Override
    public CompletableFuture<Message> sendAndReceive(Message message) {
        return this.getOutputMessage.apply(message);
    }

    @Override
    public CompletableFuture<Message> sendAndReceive(String routingKey, Message message) {
        return sendAndReceive( message);
    }

    @Override
    public CompletableFuture<Message> sendAndReceive(String exchange, String routingKey, Message message) {
        return sendAndReceive( message);
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceive(Object object) {
        return null;
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceive(String routingKey, Object object) {
        return null;
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceive(String exchange, String routingKey, Object object) {
        return null;
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceive(Object object, MessagePostProcessor messagePostProcessor) {
        return null;
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceive(String routingKey, Object object, MessagePostProcessor messagePostProcessor) {
        return null;
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceive(String exchange, String routingKey, Object object, MessagePostProcessor messagePostProcessor) {
        return null;
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceiveAsType(Object object, ParameterizedTypeReference<C> responseType) {
        return null;
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceiveAsType(String routingKey, Object object, ParameterizedTypeReference<C> responseType) {
        return null;
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceiveAsType(String exchange, String routingKey, Object object, ParameterizedTypeReference<C> responseType) {
        return null;
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceiveAsType(Object object, MessagePostProcessor messagePostProcessor, ParameterizedTypeReference<C> responseType) {
        return null;
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceiveAsType(String routingKey, Object object, MessagePostProcessor messagePostProcessor, ParameterizedTypeReference<C> responseType) {
        return null;
    }

    @Override
    public <C> CompletableFuture<C> convertSendAndReceiveAsType(String exchange, String routingKey, Object object, MessagePostProcessor messagePostProcessor, ParameterizedTypeReference<C> responseType) {
        return null;
    }
}
