package com.example.hello.persona.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CampaignEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CampaignEventConsumer.class);

    @KafkaListener(topics = "${kafka.topic.campaign-events:campaign-events}", groupId = "hello-springboot-group")
    public void consume(String message) {
        log.info("Received campaign event: {}", message);
    }
}
