package com.example.hello.persona;

import com.example.hello.common.ItemSubmissionService;
import com.example.hello.common.exception.ResourceNotFoundException;
import com.example.hello.persona.kafka.CampaignEventProducer;
import com.mp.flashpicks.common.dto.CampaignPartnerItemInput;
import com.mp.flashpicks.common.entity.CampaignPartnerItem;
import com.mp.flashpicks.common.entity.CampaignPartnerMessage;
import com.mp.flashpicks.common.entity.ItemDetail;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/item-submissions")
public class ItemSubmissionController {

    private static final Logger log = LoggerFactory.getLogger(ItemSubmissionController.class);

    @Autowired
    private ItemSubmissionService itemSubmissionService;

    @Autowired
    private CampaignEventProducer eventProducer;

    private final Counter itemSubmitCounter;
    private final Counter messageAddCounter;
    private final Timer itemSubmitTimer;
    private final Timer messageAddTimer;

    public ItemSubmissionController(MeterRegistry meterRegistry) {
        this.itemSubmitCounter = Counter.builder("items.submit.requests")
                .description("Number of item submission requests")
                .register(meterRegistry);
        this.messageAddCounter = Counter.builder("items.messages.add.requests")
                .description("Number of message add requests")
                .register(meterRegistry);
        this.itemSubmitTimer = Timer.builder("items.submit.duration")
                .description("Time taken to submit items")
                .register(meterRegistry);
        this.messageAddTimer = Timer.builder("items.messages.add.duration")
                .description("Time taken to add messages")
                .register(meterRegistry);
    }

    @PostMapping
    @Timed(value = "items.submit", description = "Time taken to submit an item to campaign")
    public ResponseEntity<CampaignPartnerItem> submitItemToCampaign(@RequestBody CampaignPartnerItemInput input) {
        itemSubmitCounter.increment();
        log.info("POST /api/item-submissions called");
        return itemSubmitTimer.record(() -> {
            CampaignPartnerItem created = itemSubmissionService.submitItemToCampaign(input);
            eventProducer.publishItemSubmitted(
                    created.getCampaignPartnerItemIdAsString(),
                    created.getPartnerId(),
                    bytesToUuid(created.getCampaignPk()));
            return ResponseEntity.ok(created);
        });
    }

    @GetMapping("/campaign/{campaignPk}")
    @Timed(value = "items.getByCampaign", description = "Time taken to get items by campaign")
    public ResponseEntity<List<CampaignPartnerItem>> getItemsByCampaign(@PathVariable String campaignPk) {
        log.info("GET /api/item-submissions/campaign/{} called", campaignPk);
        return ResponseEntity.ok(itemSubmissionService.getItemsByCampaign(campaignPk));
    }

    @GetMapping("/partner/{partnerId}")
    @Timed(value = "items.getByPartner", description = "Time taken to get items by partner")
    public ResponseEntity<List<CampaignPartnerItem>> getItemsByPartner(@PathVariable String partnerId) {
        log.info("GET /api/item-submissions/partner/{} called", partnerId);
        return ResponseEntity.ok(itemSubmissionService.getItemsByPartner(partnerId));
    }

    @PostMapping("/messages")
    @Timed(value = "items.messages.add", description = "Time taken to add a message")
    public ResponseEntity<CampaignPartnerMessage> addMessage(@RequestBody Map<String, String> body) {
        messageAddCounter.increment();
        log.info("POST /api/item-submissions/messages called");
        return messageAddTimer.record(() -> {
            CampaignPartnerMessage message = itemSubmissionService.addMessage(
                    body.get("campaignPartnerId"),
                    body.get("comment"),
                    body.get("postedBy"),
                    body.get("source"));
            return ResponseEntity.ok(message);
        });
    }

    @GetMapping("/messages/{campaignPartnerId}")
    @Timed(value = "items.messages.get", description = "Time taken to get messages")
    public ResponseEntity<List<CampaignPartnerMessage>> getMessages(@PathVariable String campaignPartnerId) {
        log.info("GET /api/item-submissions/messages/{} called", campaignPartnerId);
        return ResponseEntity.ok(itemSubmissionService.getMessages(campaignPartnerId));
    }

    @GetMapping("/item-detail/{itemId}")
    @Timed(value = "items.detail.get", description = "Time taken to get item detail")
    public ResponseEntity<ItemDetail> getItemDetail(@PathVariable String itemId) {
        log.info("GET /api/item-submissions/item-detail/{} called", itemId);
        ItemDetail detail = itemSubmissionService.getItemDetail(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item detail not found: " + itemId));
        return ResponseEntity.ok(detail);
    }

    private String bytesToUuid(byte[] bytes) {
        if (bytes == null) return null;
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }
}
