package org.composer.adapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"org.composer.adapter.*","org.composer.core.*"})
public class GraphqlAdapter {
    public static void main(String[] args) {
        SpringApplication.run(GraphqlAdapter.class,args);
    }
}