package dev.shermende.mps.processing.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageChannel;

@Configuration
public class IntegrationConfiguration {

    @Bean
    public MessageChannel kafkaChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public IntegrationFlow integrationFlow(
            KafkaTemplate<Object, Object> kafkaTemplate
    ) {
        return IntegrationFlows.from(kafkaChannel())
                .handle(message -> kafkaTemplate.send(message)).get();
    }

}
