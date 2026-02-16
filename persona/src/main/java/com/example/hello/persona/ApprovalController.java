package com.example.hello.persona;

import com.example.hello.common.ApprovalService;
import com.example.hello.common.exception.ResourceNotFoundException;
import com.example.hello.persona.kafka.CampaignEventProducer;
import com.mp.flashpicks.common.entity.CampaignPartnerItem;
import com.mp.flashpicks.common.entity.ItemReviewReasonCode;
import com.mp.flashpicks.common.entity.ReviewReasonCode;
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
@RequestMapping("/api/approvals")
public class ApprovalController {

    private static final Logger log = LoggerFactory.getLogger(ApprovalController.class);

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private CampaignEventProducer eventProducer;

    private final Counter approveCounter;
    private final Counter declineCounter;
    private final Timer approveTimer;
    private final Timer declineTimer;

    public ApprovalController(MeterRegistry meterRegistry) {
        this.approveCounter = Counter.builder("approvals.approve.requests")
                .description("Number of item approval requests")
                .register(meterRegistry);
        this.declineCounter = Counter.builder("approvals.decline.requests")
                .description("Number of item decline requests")
                .register(meterRegistry);
        this.approveTimer = Timer.builder("approvals.approve.duration")
                .description("Time taken to approve items")
                .register(meterRegistry);
        this.declineTimer = Timer.builder("approvals.decline.duration")
                .description("Time taken to decline items")
                .register(meterRegistry);
    }

    @GetMapping("/pending")
    @Timed(value = "approvals.pending.getAll", description = "Time taken to get pending items")
    public ResponseEntity<List<CampaignPartnerItem>> getPendingItems() {
        log.info("GET /api/approvals/pending called");
        return ResponseEntity.ok(approvalService.getPendingItems());
    }

    @PostMapping("/{itemId}/approve")
    @Timed(value = "approvals.approve", description = "Time taken to approve an item")
    public ResponseEntity<CampaignPartnerItem> approveItem(@PathVariable String itemId,
                                                           @RequestParam String approvedBy) {
        approveCounter.increment();
        log.info("POST /api/approvals/{}/approve called", itemId);
        return approveTimer.record(() -> {
            CampaignPartnerItem approved = approvalService.approveItem(itemId, approvedBy);
            eventProducer.publishItemApproved(itemId, approvedBy);
            return ResponseEntity.ok(approved);
        });
    }

    @PostMapping("/{itemId}/decline")
    @Timed(value = "approvals.decline", description = "Time taken to decline an item")
    public ResponseEntity<CampaignPartnerItem> declineItem(@PathVariable String itemId,
                                                           @RequestBody Map<String, String> body) {
        declineCounter.increment();
        log.info("POST /api/approvals/{}/decline called", itemId);
        return declineTimer.record(() -> {
            CampaignPartnerItem declined = approvalService.declineItem(itemId, body.get("declinedBy"), body.get("reasonCodeId"));
            eventProducer.publishItemDeclined(itemId, body.get("declinedBy"), body.get("reasonCodeId"));
            return ResponseEntity.ok(declined);
        });
    }

    @GetMapping("/reason-codes")
    @Timed(value = "approvals.reasonCodes.getAll", description = "Time taken to get all reason codes")
    public ResponseEntity<List<ReviewReasonCode>> getAllReasonCodes() {
        log.info("GET /api/approvals/reason-codes called");
        return ResponseEntity.ok(approvalService.getAllReasonCodes());
    }

    @PostMapping("/reason-codes")
    @Timed(value = "approvals.reasonCodes.create", description = "Time taken to create a reason code")
    public ResponseEntity<ReviewReasonCode> createReasonCode(@RequestBody Map<String, String> body) {
        log.info("POST /api/approvals/reason-codes called");
        ReviewReasonCode created = approvalService.createReasonCode(
                body.get("code"),
                body.get("shortDescription"),
                body.get("longDescription"),
                body.get("type"),
                body.get("scope"),
                body.get("buId"),
                body.get("martId"),
                body.get("createdBy"));
        return ResponseEntity.ok(created);
    }

    @GetMapping("/item-reasons/{itemId}")
    @Timed(value = "approvals.itemReasons.get", description = "Time taken to get item review reasons")
    public ResponseEntity<List<ItemReviewReasonCode>> getItemReviewReasons(@PathVariable String itemId) {
        log.info("GET /api/approvals/item-reasons/{} called", itemId);
        return ResponseEntity.ok(approvalService.getItemReviewReasons(itemId));
    }
}
