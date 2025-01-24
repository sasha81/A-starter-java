package org.composer.adapter.services;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SendToCamelService implements ISendToCamelService{
    private final ProducerTemplate producerTemplate;
    public SendToCamelService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }
    public CompletableFuture<Object> sendBodyToCamel(String endpointUri, Object body){
        return this.producerTemplate.asyncSendBody(endpointUri,body);
    }
}
