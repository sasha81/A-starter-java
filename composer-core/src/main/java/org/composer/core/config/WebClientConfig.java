package org.composer.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${rest.host}")
    private String host;
    @Value("${rest.port}")
    private String port;


    @Autowired
    WebClient.Builder webClientBuilder;
    @Bean
    public WebClient getWebClient(){
        return webClientBuilder.baseUrl("http://"+host+":"+port)
                .build();
    }
}
