package org.composer.core.routes;


import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BusinessProcessXService {

    Logger logger = LoggerFactory.getLogger(BusinessProcessXService.class);



    public void preprocess_X_step_1(Exchange exchange){
        logger.info("X_step_1_preprocessed for exchange id "+exchange.getExchangeId());
    }

    public void postprocess_X_step_1(Exchange exchange){
        logger.info("X_step_1_postprocessed for exchange id "+exchange.getExchangeId());
    }

    public void preprocess_X_step_2(Exchange exchange){
        logger.info("X_step_2_preprocessed for exchange id "+exchange.getExchangeId());
    }

    public void postprocess_X_step_2(Exchange exchange){
        logger.info("X_step_2_postprocessed for exchange id "+exchange.getExchangeId());
    }

    public void preprocess_X_step_3(Exchange exchange){
        logger.info("X_step_3_preprocessed for exchange id "+exchange.getExchangeId());
    }

    public void postprocess_X_step_3(Exchange exchange){
        logger.info("X_step_3_postprocessed for exchange id "+exchange.getExchangeId());
    }

    public void process_X_final(Exchange exchange){
        logger.info("Final_preprocessed for exchange id "+exchange.getExchangeId());
    }


}
