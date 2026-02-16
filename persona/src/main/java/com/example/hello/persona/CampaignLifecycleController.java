package com.example.hello.persona;

import com.example.hello.common.CampaignLifecycleService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/campaign-lifecycle")
public class CampaignLifecycleController {

    private static final Logger log = LoggerFactory.getLogger(CampaignLifecycleController.class);

    @Autowired
    private CampaignLifecycleService campaignLifecycleService;

    @Autowired
    private CampaignEventProducer eventProducer;

    private final Counter statusUpdateCounter;
    private final Timer statusUpdateTimer;

    public CampaignLifecycleController(MeterRegistry meterRegistry) {
        this.statusUpdateCounter = Counter.builder("campaigns.status.update.requests")
                .description("Number of campaign status update requests")
                .register(meterRegistry);
        this.statusUpdateTimer = Timer.builder("campaigns.status.update.duration")
                .description("Time taken to update campaign status")
                .register(meterRegistry);
    }

    @PutMapping("/{id}/status")
    @Timed(value = "campaigns.status.update", description = "Time taken to update campaign status")
    public ResponseEntity<Campaign> updateCampaignStatus(@PathVariable String id,
                                                         @RequestBody Map<String, String> body) {
        statusUpdateCounter.increment();
        log.info("PUT /api/campaign-lifecycle/{}/status called", id);
        return statusUpdateTimer.record(() -> {
            String oldStatus = campaignLifecycleService.getCampaignsByStatus(body.get("status")).isEmpty() ? "UNKNOWN" : body.get("status");
            Campaign updated = campaignLifecycleService.updateCampaignStatus(id, body.get("status"), body.get("modifiedBy"));
            eventProducer.publishCampaignStatusChanged(id, oldStatus, body.get("status"));
            return ResponseEntity.ok(updated);
        });
    }

    @PutMapping("/{id}")
    @Timed(value = "campaigns.update", description = "Time taken to update a campaign")
    public ResponseEntity<Campaign> updateCampaign(@PathVariable String id,
                                                   @RequestBody CampaignRequest request) {
        log.info("PUT /api/campaign-lifecycle/{} called", id);
        Campaign updated = campaignLifecycleService.updateCampaign(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/status/{status}")
    @Timed(value = "campaigns.getByStatus", description = "Time taken to get campaigns by status")
    public ResponseEntity<List<Campaign>> getCampaignsByStatus(@PathVariable String status) {
        log.info("GET /api/campaign-lifecycle/status/{} called", status);
        return ResponseEntity.ok(campaignLifecycleService.getCampaignsByStatus(status));
    }
}
