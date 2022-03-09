package dev.shermende.mps.processing.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
public class HomeController {
    private final DiscoveryClient discoveryClient;

    private final MessageChannel messageChannel;

    public HomeController(
            DiscoveryClient discoveryClient,
            @Qualifier("kafkaChannel") MessageChannel messageChannel
    ) {
        this.discoveryClient = discoveryClient;
        this.messageChannel = messageChannel;
    }

    @GetMapping("/")
    public String string() {
        messageChannel.send(MessageBuilder
                .withPayload(Test.builder().title(UUID.randomUUID().toString()).build())
                .setHeader(KafkaHeaders.TOPIC, "test")
                .build());
        log.info("test {}", discoveryClient.getInstances("mps-processing"));
        return UUID.randomUUID().toString();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Test {
        private String title;
    }

}
