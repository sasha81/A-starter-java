package org.composer.core.services;


import org.apache.camel.Exchange;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.model.ContainerResults;
import org.composer.core.model.DegreesOfMatching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserProcessService {

    Logger logger = LoggerFactory.getLogger(UserProcessService.class);



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

    public void process_Compare_Result(Exchange exchange){
        CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
        var result = ContainerResults.builder()
                .groupsOfTheSameUserMatch(DegreesOfMatching.CLOSE)
                .numberOfUsersMatch(DegreesOfMatching.DIFFERENT)
                .build();
        body.getFinal_result().setOutput(result);
        logger.info("Result_processed for exchange id "+exchange.getExchangeId());
    }


}
