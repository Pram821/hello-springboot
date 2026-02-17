package com.example.hello.persona;

import com.example.hello.common.PromoService;
import com.example.hello.common.exception.ResourceNotFoundException;
import com.example.hello.persona.kafka.CampaignEventProducer;
import com.mp.flashpicks.common.dto.WmtDiscountInput;
import com.mp.flashpicks.common.entity.PromoEntity;
import com.mp.flashpicks.common.entity.WmtDiscount;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promos")
public class PromoController {

    private static final Logger log = LoggerFactory.getLogger(PromoController.class);

    @Autowired
    private PromoService promoService;

    @Autowired
    private CampaignEventProducer eventProducer;

    private final Counter promoCreateCounter;
    private final Counter promoStatusUpdateCounter;
    private final Timer promoCreateTimer;
    private final Timer promoStatusUpdateTimer;

    public PromoController(MeterRegistry meterRegistry) {
        this.promoCreateCounter = Counter.builder("promos.create.requests")
                .description("Number of promo creation requests")
                .register(meterRegistry);
        this.promoStatusUpdateCounter = Counter.builder("promos.status.update.requests")
                .description("Number of promo status update requests")
                .register(meterRegistry);
        this.promoCreateTimer = Timer.builder("promos.create.duration")
                .description("Time taken to create promos")
                .register(meterRegistry);
        this.promoStatusUpdateTimer = Timer.builder("promos.status.update.duration")
                .description("Time taken to update promo status")
                .register(meterRegistry);
    }

    @GetMapping
    @Timed(value = "promos.getAll", description = "Time taken to get all promos")
    public ResponseEntity<List<PromoEntity>> getAllPromos() {
        log.info("GET /api/promos called");
        return ResponseEntity.ok(promoService.getAllPromos());
    }

    @GetMapping("/{id}")
    @Timed(value = "promos.getById", description = "Time taken to get promo by ID")
    public ResponseEntity<PromoEntity> getPromoById(@PathVariable String id) {
        log.info("GET /api/promos/{} called", id);
        PromoEntity promo = promoService.getPromoById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo not found: " + id));
        return ResponseEntity.ok(promo);
    }

    @GetMapping("/item/{itemId}")
    @Timed(value = "promos.getByItemId", description = "Time taken to get promos by item ID")
    public ResponseEntity<List<PromoEntity>> getPromosByCampaignPartnerItemId(@PathVariable String itemId) {
        log.info("GET /api/promos/item/{} called", itemId);
        return ResponseEntity.ok(promoService.getPromosByCampaignPartnerItemId(itemId));
    }

    @PostMapping
    @Timed(value = "promos.create", description = "Time taken to create a promo")
    public ResponseEntity<PromoEntity> createPromo(@RequestBody Map<String, String> body) {
        promoCreateCounter.increment();
        log.info("POST /api/promos called");
        return promoCreateTimer.record(() -> {
            PromoEntity created = promoService.createPromo(
                    body.get("campaignPartnerItemId"),
                    body.get("promoId"),
                    body.get("promoStatus"),
                    LocalDateTime.parse(body.get("startDate")),
                    LocalDateTime.parse(body.get("endDate")),
                    body.get("createdBy"));
            eventProducer.publishPromoCreated(created.getPromoEntityIdAsString(), created.getPromoId());
            return ResponseEntity.ok(created);
        });
    }

    @PutMapping("/{id}/status")
    @Timed(value = "promos.status.update", description = "Time taken to update promo status")
    public ResponseEntity<PromoEntity> updatePromoStatus(@PathVariable String id,
                                                         @RequestBody Map<String, String> body) {
        promoStatusUpdateCounter.increment();
        log.info("PUT /api/promos/{}/status called", id);
        return promoStatusUpdateTimer.record(() -> {
            PromoEntity updated = promoService.updatePromoStatus(
                    id,
                    body.get("promoStatus"),
                    body.get("previousStatus"),
                    body.get("eventAction"),
                    body.get("modifiedBy"));
            eventProducer.publishPromoStatusChanged(id, body.get("previousStatus"), body.get("promoStatus"));
            return ResponseEntity.ok(updated);
        });
    }

    @PostMapping("/discounts")
    @Timed(value = "promos.discounts.create", description = "Time taken to create a discount")
    public ResponseEntity<WmtDiscount> createWmtDiscount(@RequestBody WmtDiscountInput input) {
        log.info("POST /api/promos/discounts called");
        return ResponseEntity.ok(promoService.createWmtDiscount(input));
    }

    @GetMapping("/discounts/{itemId}")
    @Timed(value = "promos.discounts.getByItemId", description = "Time taken to get discounts by item ID")
    public ResponseEntity<List<WmtDiscount>> getDiscountsByItemId(@PathVariable String itemId) {
        log.info("GET /api/promos/discounts/{} called", itemId);
        return ResponseEntity.ok(promoService.getDiscountsByItemId(itemId));
    }
}
