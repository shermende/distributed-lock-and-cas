package dev.shermende.mps.processing.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ErrorHandler;

import java.util.Objects;

@Configuration
public class KafkaConfiguration {
    @Bean
    public NewTopic dlt() {
        return new NewTopic("test", 10, (short) 1);
    }

    @Bean
    public NewTopic dltt() {
        return new NewTopic("test.dlt", 10, (short) 1);
    }

    @Bean
    public ErrorHandler errorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate
    ) {
        return (exception, data) -> kafkaTemplate.send(
                new ProducerRecord<>(
                        String.format("%s.dlt", Objects.requireNonNull(data).topic()),
                        data.partition(),
                        data.key(),
                        data.value(),
                        data.headers()
                )
        );
    }

}
