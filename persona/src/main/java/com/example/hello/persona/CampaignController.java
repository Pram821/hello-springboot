package com.example.hello.persona;

import com.example.hello.common.CampaignService;
import com.example.hello.common.exception.ResourceNotFoundException;
import com.example.hello.persona.kafka.CampaignEventProducer;
import com.mp.flashpicks.common.dto.CampaignRequest;
import com.mp.flashpicks.common.entity.Campaign;
import io.micrometer.core.annotation.Timed;
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
@RequestMapping("/api/campaigns")
public class CampaignController {

    private static final Logger log = LoggerFactory.getLogger(CampaignController.class);

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignEventProducer eventProducer;

    private final Counter campaignCreateCounter;
    private final Counter campaignGetCounter;
    private final Timer campaignCreateTimer;

    public CampaignController(MeterRegistry meterRegistry) {
        this.campaignCreateCounter = Counter.builder("campaigns.create.requests")
                .description("Number of campaign creation requests")
                .register(meterRegistry);
        this.campaignGetCounter = Counter.builder("campaigns.get.requests")
                .description("Number of campaign get requests")
                .register(meterRegistry);
        this.campaignCreateTimer = Timer.builder("campaigns.create.duration")
                .description("Time taken to create campaigns")
                .register(meterRegistry);
    }

    @GetMapping
    @Timed(value = "campaigns.getAll", description = "Time taken to get all campaigns")
    public ResponseEntity<List<Campaign>> getAllCampaigns() {
        campaignGetCounter.increment();
        log.info("GET /api/campaigns called");
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }

    @GetMapping("/{campaignId}")
    @Timed(value = "campaigns.getById", description = "Time taken to get campaign by ID")
    public ResponseEntity<Campaign> getCampaignById(@PathVariable String campaignId) {
        campaignGetCounter.increment();
        log.info("GET /api/campaigns/{} called", campaignId);
        Campaign campaign = campaignService.getCampaignById(campaignId);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign not found: " + campaignId);
        }
        return ResponseEntity.ok(campaign);
    }

    @PostMapping
    @Timed(value = "campaigns.create", description = "Time taken to create a campaign")
    public ResponseEntity<Campaign> createCampaign(@RequestBody CampaignRequest request) {
        campaignCreateCounter.increment();
        log.info("POST /api/campaigns called");
        return campaignCreateTimer.record(() -> {
            Campaign created = campaignService.createCampaign(request);
            eventProducer.publishCampaignCreated(created.getCampaignId(), created.getCampaignName());
            return ResponseEntity.ok(created);
        });
    }
}
