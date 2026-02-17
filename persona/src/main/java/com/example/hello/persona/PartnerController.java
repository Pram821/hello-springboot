package com.example.hello.persona;

import com.example.hello.common.PartnerOnboardingService;
import com.example.hello.common.exception.ResourceNotFoundException;
import com.example.hello.persona.kafka.CampaignEventProducer;
import com.mp.flashpicks.common.entity.ApprovedPartner;
import com.mp.flashpicks.common.entity.ProspectivePartner;
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
@RequestMapping("/api/partners")
public class PartnerController {

    private static final Logger log = LoggerFactory.getLogger(PartnerController.class);

    @Autowired
    private PartnerOnboardingService partnerOnboardingService;

    @Autowired
    private CampaignEventProducer eventProducer;

    private final Counter partnerCreateCounter;
    private final Counter partnerApproveCounter;
    private final Timer partnerCreateTimer;
    private final Timer partnerApproveTimer;

    public PartnerController(MeterRegistry meterRegistry) {
        this.partnerCreateCounter = Counter.builder("partners.prospective.create.requests")
                .description("Number of prospective partner creation requests")
                .register(meterRegistry);
        this.partnerApproveCounter = Counter.builder("partners.approve.requests")
                .description("Number of partner approval requests")
                .register(meterRegistry);
        this.partnerCreateTimer = Timer.builder("partners.prospective.create.duration")
                .description("Time taken to create prospective partners")
                .register(meterRegistry);
        this.partnerApproveTimer = Timer.builder("partners.approve.duration")
                .description("Time taken to approve partners")
                .register(meterRegistry);
    }

    @GetMapping("/prospective")
    @Timed(value = "partners.prospective.getAll", description = "Time taken to get all prospective partners")
    public ResponseEntity<List<ProspectivePartner>> getAllProspectivePartners() {
        log.info("GET /api/partners/prospective called");
        return ResponseEntity.ok(partnerOnboardingService.getAllProspectivePartners());
    }

    @GetMapping("/prospective/{id}")
    @Timed(value = "partners.prospective.getById", description = "Time taken to get prospective partner by ID")
    public ResponseEntity<ProspectivePartner> getProspectivePartnerById(@PathVariable String id) {
        log.info("GET /api/partners/prospective/{} called", id);
        ProspectivePartner partner = partnerOnboardingService.getProspectivePartnerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prospective partner not found: " + id));
        return ResponseEntity.ok(partner);
    }

    @PostMapping("/prospective")
    @Timed(value = "partners.prospective.create", description = "Time taken to create a prospective partner")
    public ResponseEntity<ProspectivePartner> createProspectivePartner(@RequestBody Map<String, String> body) {
        partnerCreateCounter.increment();
        log.info("POST /api/partners/prospective called");
        return partnerCreateTimer.record(() -> {
            ProspectivePartner created = partnerOnboardingService.createProspectivePartner(
                    body.get("partnerId"),
                    body.get("sellerName"),
                    body.get("buId"),
                    body.get("martId"),
                    body.get("createdBy"));
            eventProducer.publishProspectivePartnerCreated(body.get("partnerId"), body.get("sellerName"));
            return ResponseEntity.ok(created);
        });
    }

    @PostMapping("/prospective/{id}/approve")
    @Timed(value = "partners.approve", description = "Time taken to approve a partner")
    public ResponseEntity<ApprovedPartner> approvePartner(@PathVariable String id,
                                                          @RequestParam String approvedBy) {
        partnerApproveCounter.increment();
        log.info("POST /api/partners/prospective/{}/approve called", id);
        return partnerApproveTimer.record(() -> {
            ApprovedPartner approved = partnerOnboardingService.approvePartner(id, approvedBy);
            eventProducer.publishPartnerApproved(approved.getPartnerId(), approved.getSellerName());
            return ResponseEntity.ok(approved);
        });
    }

    @GetMapping("/approved")
    @Timed(value = "partners.approved.getAll", description = "Time taken to get all approved partners")
    public ResponseEntity<List<ApprovedPartner>> getAllApprovedPartners() {
        log.info("GET /api/partners/approved called");
        return ResponseEntity.ok(partnerOnboardingService.getAllApprovedPartners());
    }

    @GetMapping("/approved/{partnerId}")
    @Timed(value = "partners.approved.getById", description = "Time taken to get approved partner by ID")
    public ResponseEntity<ApprovedPartner> getApprovedPartner(@PathVariable String partnerId) {
        log.info("GET /api/partners/approved/{} called", partnerId);
        ApprovedPartner partner = partnerOnboardingService.getApprovedPartner(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Approved partner not found: " + partnerId));
        return ResponseEntity.ok(partner);
    }
}
