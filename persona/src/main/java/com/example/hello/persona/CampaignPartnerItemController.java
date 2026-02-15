package com.example.hello.persona;

import com.example.hello.common.CampaignPartnerItemService;
import com.example.hello.common.exception.ResourceNotFoundException;
import com.example.hello.persona.kafka.CampaignEventProducer;
import com.mp.flashpicks.common.dto.CampaignPartnerItemInput;
import com.mp.flashpicks.common.entity.CampaignPartnerItem;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaign-partner-items")
public class CampaignPartnerItemController {

    private static final Logger log = LoggerFactory.getLogger(CampaignPartnerItemController.class);

    @Autowired
    private CampaignPartnerItemService campaignPartnerItemService;

    @Autowired
    private CampaignEventProducer eventProducer;

    private final Counter itemCreateCounter;
    private final Counter itemGetCounter;
    private final Timer itemCreateTimer;

    public CampaignPartnerItemController(MeterRegistry meterRegistry) {
        this.itemCreateCounter = Counter.builder("campaign_partner_items.create.requests")
                .description("Number of partner item creation requests")
                .register(meterRegistry);
        this.itemGetCounter = Counter.builder("campaign_partner_items.get.requests")
                .description("Number of partner item get requests")
                .register(meterRegistry);
        this.itemCreateTimer = Timer.builder("campaign_partner_items.create.duration")
                .description("Time taken to create partner items")
                .register(meterRegistry);
    }

    @GetMapping
    public ResponseEntity<List<CampaignPartnerItem>> getAllItems() {
        itemGetCounter.increment();
        log.info("GET /api/campaign-partner-items called");
        return ResponseEntity.ok(campaignPartnerItemService.getAllItems());
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<CampaignPartnerItem> getItemById(@PathVariable String itemId) {
        itemGetCounter.increment();
        log.info("GET /api/campaign-partner-items/{} called", itemId);
        CampaignPartnerItem item = campaignPartnerItemService.getItemById(itemId);
        if (item == null) {
            throw new ResourceNotFoundException("Campaign partner item not found: " + itemId);
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping
    public ResponseEntity<CampaignPartnerItem> createItem(@RequestBody CampaignPartnerItemInput input) {
        itemCreateCounter.increment();
        log.info("POST /api/campaign-partner-items called");
        return itemCreateTimer.record(() -> {
            CampaignPartnerItem created = campaignPartnerItemService.createItem(input);
            eventProducer.publishCampaignPartnerItemCreated(
                    created.getCampaignPartnerItemIdAsString(), created.getPartnerId());
            return ResponseEntity.ok(created);
        });
    }
}
