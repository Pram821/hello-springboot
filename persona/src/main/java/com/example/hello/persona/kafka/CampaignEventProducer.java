package com.example.hello.persona.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CampaignEventProducer {

    private static final Logger log = LoggerFactory.getLogger(CampaignEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.campaign-events:campaign-events}")
    private String campaignEventsTopic;

    public CampaignEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCampaignCreated(String campaignId, String campaignName) {
        String message = "{\"event\":\"CAMPAIGN_CREATED\",\"campaignId\":\"%s\",\"campaignName\":\"%s\"}"
                .formatted(campaignId, campaignName);
        log.info("Publishing campaign created event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, campaignId, message);
    }

    public void publishCampaignPartnerItemCreated(String itemId, String partnerId) {
        String message = "{\"event\":\"PARTNER_ITEM_CREATED\",\"itemId\":\"%s\",\"partnerId\":\"%s\"}"
                .formatted(itemId, partnerId);
        log.info("Publishing partner item created event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, itemId, message);
    }
}
