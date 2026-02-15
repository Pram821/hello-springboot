package com.example.hello.persona.gcs;

import com.example.hello.common.CampaignPartnerItemService;
import com.example.hello.common.exception.BadRequestException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.mp.flashpicks.common.dto.CampaignPartnerItemInput;
import com.mp.flashpicks.common.entity.CampaignPartnerItem;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gcs")
public class GcsBucketController {

    private static final Logger log = LoggerFactory.getLogger(GcsBucketController.class);

    private final CampaignPartnerItemService itemService;
    private final Counter gcsBulkInsertCounter;

    public GcsBucketController(CampaignPartnerItemService itemService, MeterRegistry meterRegistry) {
        this.itemService = itemService;
        this.gcsBulkInsertCounter = Counter.builder("gcs.bulk_insert.requests")
                .description("Number of GCS bulk insert requests")
                .register(meterRegistry);
    }

    @PostMapping("/import-items")
    public ResponseEntity<Map<String, Object>> importItemsFromBucket(
            @RequestParam String bucketName,
            @RequestParam String fileName) {
        gcsBulkInsertCounter.increment();
        log.info("Importing campaign partner items from gs://{}/{}", bucketName, fileName);

        try {
            Storage storage = StorageOptions.getDefaultInstance().getService();
            Blob blob = storage.get(bucketName, fileName);
            if (blob == null) {
                throw new BadRequestException("File not found: gs://" + bucketName + "/" + fileName);
            }

            String content = new String(blob.getContent(), StandardCharsets.UTF_8);
            List<CampaignPartnerItemInput> items = parseCsv(content);

            List<CampaignPartnerItem> created = new ArrayList<>();
            for (CampaignPartnerItemInput input : items) {
                created.add(itemService.createItem(input));
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("source", "gs://" + bucketName + "/" + fileName);
            response.put("itemsImported", created.size());
            response.put("status", "SUCCESS");

            log.info("Successfully imported {} items from GCS", created.size());
            return ResponseEntity.ok(response);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to import items from GCS", e);
            throw new BadRequestException("Failed to import items: " + e.getMessage());
        }
    }

    private List<CampaignPartnerItemInput> parseCsv(String csvContent) throws Exception {
        List<CampaignPartnerItemInput> items = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(csvContent));

        String header = reader.readLine();
        if (header == null) {
            throw new BadRequestException("CSV file is empty");
        }
        String[] columns = header.split(",");
        Map<String, Integer> colIndex = new LinkedHashMap<>();
        for (int i = 0; i < columns.length; i++) {
            colIndex.put(columns[i].trim(), i);
        }

        String line;
        int lineNum = 1;
        while ((line = reader.readLine()) != null) {
            lineNum++;
            if (line.trim().isEmpty()) continue;
            String[] values = line.split(",", -1);

            CampaignPartnerItemInput input = new CampaignPartnerItemInput();
            input.setCampaignPartnerId(getCol(values, colIndex, "campaignPartnerId"));
            input.setCampaignPk(getCol(values, colIndex, "campaignPk"));
            input.setPartnerId(getCol(values, colIndex, "partnerId"));
            input.setItemId(getCol(values, colIndex, "itemId"));

            String retailPrice = getCol(values, colIndex, "retailPrice");
            if (retailPrice != null && !retailPrice.isEmpty()) {
                input.setRetailPrice(Float.parseFloat(retailPrice));
            }
            String promoPrice = getCol(values, colIndex, "promoPrice");
            if (promoPrice != null && !promoPrice.isEmpty()) {
                input.setPromoPrice(Float.parseFloat(promoPrice));
            }

            input.setSource(getCol(values, colIndex, "source"));
            input.setCreatedBy(getCol(values, colIndex, "createdBy"));
            items.add(input);
        }

        log.info("Parsed {} items from CSV", items.size());
        return items;
    }

    private String getCol(String[] values, Map<String, Integer> colIndex, String colName) {
        Integer idx = colIndex.get(colName);
        if (idx == null || idx >= values.length) return null;
        String val = values[idx].trim();
        return val.isEmpty() ? null : val;
    }
}
