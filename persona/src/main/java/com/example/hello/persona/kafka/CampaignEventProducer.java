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

    public void publishPartnerApproved(String partnerId, String sellerName) {
        String message = "{\"event\":\"PARTNER_APPROVED\",\"partnerId\":\"%s\",\"sellerName\":\"%s\"}"
                .formatted(partnerId, sellerName);
        log.info("Publishing partner approved event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, partnerId, message);
    }

    public void publishProspectivePartnerCreated(String partnerId, String sellerName) {
        String message = "{\"event\":\"PROSPECTIVE_PARTNER_CREATED\",\"partnerId\":\"%s\",\"sellerName\":\"%s\"}"
                .formatted(partnerId, sellerName);
        log.info("Publishing prospective partner created event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, partnerId, message);
    }

    public void publishCampaignStatusChanged(String campaignId, String oldStatus, String newStatus) {
        String message = "{\"event\":\"CAMPAIGN_STATUS_CHANGED\",\"campaignId\":\"%s\",\"oldStatus\":\"%s\",\"newStatus\":\"%s\"}"
                .formatted(campaignId, oldStatus, newStatus);
        log.info("Publishing campaign status changed event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, campaignId, message);
    }

    public void publishPromoCreated(String promoEntityId, String promoId) {
        String message = "{\"event\":\"PROMO_CREATED\",\"promoEntityId\":\"%s\",\"promoId\":\"%s\"}"
                .formatted(promoEntityId, promoId);
        log.info("Publishing promo created event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, promoEntityId, message);
    }

    public void publishPromoStatusChanged(String promoEntityId, String oldStatus, String newStatus) {
        String message = "{\"event\":\"PROMO_STATUS_CHANGED\",\"promoEntityId\":\"%s\",\"oldStatus\":\"%s\",\"newStatus\":\"%s\"}"
                .formatted(promoEntityId, oldStatus, newStatus);
        log.info("Publishing promo status changed event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, promoEntityId, message);
    }

    public void publishPlacementWeekCreated(String weekId, String name) {
        String message = "{\"event\":\"PLACEMENT_WEEK_CREATED\",\"weekId\":\"%s\",\"name\":\"%s\"}"
                .formatted(weekId, name);
        log.info("Publishing placement week created event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, weekId, message);
    }

    public void publishPlacementItemAdded(String weekId, String itemId) {
        String message = "{\"event\":\"PLACEMENT_ITEM_ADDED\",\"weekId\":\"%s\",\"itemId\":\"%s\"}"
                .formatted(weekId, itemId);
        log.info("Publishing placement item added event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, weekId, message);
    }

    public void publishItemSubmitted(String itemId, String partnerId, String campaignPk) {
        String message = "{\"event\":\"ITEM_SUBMITTED\",\"itemId\":\"%s\",\"partnerId\":\"%s\",\"campaignPk\":\"%s\"}"
                .formatted(itemId, partnerId, campaignPk);
        log.info("Publishing item submitted event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, itemId, message);
    }

    public void publishItemApproved(String itemId, String approvedBy) {
        String message = "{\"event\":\"ITEM_APPROVED\",\"itemId\":\"%s\",\"approvedBy\":\"%s\"}"
                .formatted(itemId, approvedBy);
        log.info("Publishing item approved event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, itemId, message);
    }

    public void publishItemDeclined(String itemId, String declinedBy, String reasonCodeId) {
        String message = "{\"event\":\"ITEM_DECLINED\",\"itemId\":\"%s\",\"declinedBy\":\"%s\",\"reasonCodeId\":\"%s\"}"
                .formatted(itemId, declinedBy, reasonCodeId);
        log.info("Publishing item declined event: {}", message);
        kafkaTemplate.send(campaignEventsTopic, itemId, message);
    }
}
