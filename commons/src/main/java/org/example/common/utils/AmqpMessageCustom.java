package org.example.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

public class AmqpMessageCustom {
    public static Message getNestMessage(Object data, String pattern, String correlationId)  {
        ObjectMapper objMapper = new ObjectMapper();
        MessageProperties properties = setMessageProperties( correlationId);
//        MessageProperties properties = new MessageProperties();
//        properties.setCorrelationId(correlationId);
//        properties.setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
//        properties.setContentType("application/json");
        try {
            String dataString = objMapper.writeValueAsString(data);
            String fullString = getFullString(dataString, pattern, correlationId); //"{\"pattern\":"+pattern+",\"data\":"+dataString+",\"id\":\""+correlationId+"\"}";
            return new Message(fullString.getBytes(StandardCharsets.UTF_8),properties);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }

    public static String getFullString(String dataString, String pattern, String correlationId){
        return "{\"pattern\":"+pattern+",\"data\":"+dataString+",\"id\":\""+correlationId+"\"}";
    }

    public static MessageProperties setMessageProperties(String correlationId){
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(correlationId);
        properties.setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
        properties.setContentType("application/json");
        return properties;
    }
}
