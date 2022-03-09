package dev.shermende.mps.processing.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumer {
    @KafkaListener(topics = "test", groupId = "${spring.application.name}", concurrency = "10")
    public void handle(Message<?> message) {
        throw new IllegalArgumentException(message.toString());
    }

}