package com.example.hello.persona;

import com.example.hello.common.PlacementWeekService;
import com.example.hello.common.exception.ResourceNotFoundException;
import com.example.hello.persona.kafka.CampaignEventProducer;
import com.mp.flashpicks.common.dto.PlacementWeekInput;
import com.mp.flashpicks.common.entity.PlacementWeek;
import com.mp.flashpicks.common.entity.PlacementWeekItem;
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
@RequestMapping("/api/placement-weeks")
public class PlacementWeekController {

    private static final Logger log = LoggerFactory.getLogger(PlacementWeekController.class);

    @Autowired
    private PlacementWeekService placementWeekService;

    @Autowired
    private CampaignEventProducer eventProducer;

    private final Counter weekCreateCounter;
    private final Counter itemAddCounter;
    private final Timer weekCreateTimer;
    private final Timer itemAddTimer;

    public PlacementWeekController(MeterRegistry meterRegistry) {
        this.weekCreateCounter = Counter.builder("placement.weeks.create.requests")
                .description("Number of placement week creation requests")
                .register(meterRegistry);
        this.itemAddCounter = Counter.builder("placement.items.add.requests")
                .description("Number of placement item add requests")
                .register(meterRegistry);
        this.weekCreateTimer = Timer.builder("placement.weeks.create.duration")
                .description("Time taken to create placement weeks")
                .register(meterRegistry);
        this.itemAddTimer = Timer.builder("placement.items.add.duration")
                .description("Time taken to add items to placement weeks")
                .register(meterRegistry);
    }

    @GetMapping
    @Timed(value = "placement.weeks.getAll", description = "Time taken to get all placement weeks")
    public ResponseEntity<List<PlacementWeek>> getAllPlacementWeeks() {
        log.info("GET /api/placement-weeks called");
        return ResponseEntity.ok(placementWeekService.getAllPlacementWeeks());
    }

    @GetMapping("/{id}")
    @Timed(value = "placement.weeks.getById", description = "Time taken to get placement week by ID")
    public ResponseEntity<PlacementWeek> getPlacementWeekById(@PathVariable String id) {
        log.info("GET /api/placement-weeks/{} called", id);
        PlacementWeek week = placementWeekService.getPlacementWeekById(id);
        if (week == null) {
            throw new ResourceNotFoundException("Placement week not found: " + id);
        }
        return ResponseEntity.ok(week);
    }

    @PostMapping
    @Timed(value = "placement.weeks.create", description = "Time taken to create a placement week")
    public ResponseEntity<PlacementWeek> createPlacementWeek(@RequestBody PlacementWeekInput input) {
        weekCreateCounter.increment();
        log.info("POST /api/placement-weeks called");
        return weekCreateTimer.record(() -> {
            PlacementWeek created = placementWeekService.createPlacementWeek(input);
            eventProducer.publishPlacementWeekCreated(bytesToUuid(created.getWeekId()), created.getName());
            return ResponseEntity.ok(created);
        });
    }

    @PutMapping("/{id}/status")
    @Timed(value = "placement.weeks.status.update", description = "Time taken to update placement week status")
    public ResponseEntity<PlacementWeek> updatePlacementWeekStatus(@PathVariable String id,
                                                                   @RequestBody Map<String, String> body) {
        log.info("PUT /api/placement-weeks/{}/status called", id);
        PlacementWeek updated = placementWeekService.updatePlacementWeekStatus(id, body.get("status"), body.get("modifiedBy"));
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{weekId}/items")
    @Timed(value = "placement.weeks.items.getAll", description = "Time taken to get items by week ID")
    public ResponseEntity<List<PlacementWeekItem>> getItemsByWeekId(@PathVariable String weekId) {
        log.info("GET /api/placement-weeks/{}/items called", weekId);
        return ResponseEntity.ok(placementWeekService.getItemsByWeekId(weekId));
    }

    @PostMapping("/{weekId}/items")
    @Timed(value = "placement.items.add", description = "Time taken to add item to week")
    public ResponseEntity<PlacementWeekItem> addItemToWeek(@PathVariable String weekId,
                                                           @RequestBody Map<String, String> body) {
        itemAddCounter.increment();
        log.info("POST /api/placement-weeks/{}/items called", weekId);
        return itemAddTimer.record(() -> {
            PlacementWeekItem added = placementWeekService.addItemToWeek(
                    weekId,
                    body.get("itemId"),
                    body.get("partnerId"),
                    Double.valueOf(body.get("price")),
                    Integer.valueOf(body.get("stackRank")),
                    body.get("buId"),
                    body.get("martId"),
                    body.get("createdBy"));
            eventProducer.publishPlacementItemAdded(weekId, body.get("itemId"));
            return ResponseEntity.ok(added);
        });
    }

    @PutMapping("/items/{weekItemId}/rank")
    @Timed(value = "placement.items.rank.update", description = "Time taken to update item rank")
    public ResponseEntity<PlacementWeekItem> updateItemRank(@PathVariable String weekItemId,
                                                            @RequestBody Map<String, String> body) {
        log.info("PUT /api/placement-weeks/items/{}/rank called", weekItemId);
        PlacementWeekItem updated = placementWeekService.updateItemRank(
                weekItemId,
                Integer.valueOf(body.get("stackRank")),
                body.get("modifiedBy"));
        return ResponseEntity.ok(updated);
    }

    private String bytesToUuid(byte[] bytes) {
        if (bytes == null) return null;
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }
}
