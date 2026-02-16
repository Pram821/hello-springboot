package com.example.hello.common;

import com.mp.flashpicks.common.dto.PlacementWeekInput;
import com.mp.flashpicks.common.entity.PlacementWeek;
import com.mp.flashpicks.common.entity.PlacementWeekItem;
import com.mp.flashpicks.common.repository.PlacementWeekItemRepository;
import com.mp.flashpicks.common.repository.PlacementWeekRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlacementWeekService {

    @Autowired
    private PlacementWeekRepository placementWeekRepository;

    @Autowired
    private PlacementWeekItemRepository placementWeekItemRepository;

    public List<PlacementWeek> getAllPlacementWeeks() {
        return placementWeekRepository.findAll();
    }

    public PlacementWeek getPlacementWeekById(String id) {
        byte[] pk = uuidToBytes(id);
        return placementWeekRepository.findById(pk).orElse(null);
    }

    // PlacementWeekInput dates are Strings; PlacementWeek dates are LocalDateTime

    public PlacementWeek createPlacementWeek(PlacementWeekInput input) {
        PlacementWeek week = new PlacementWeek();
        week.setWeekId(uuidToBytes(UUID.randomUUID().toString()));
        week.setName(input.getName());
        week.setCampaignType(input.getCampaignType());
        if (input.getStartDate() != null) {
            week.setStartDate(LocalDateTime.parse(input.getStartDate()));
        }
        if (input.getEndDate() != null) {
            week.setEndDate(LocalDateTime.parse(input.getEndDate()));
        }
        week.setStatus(input.getStatus());
        week.setRecommendationVersion(input.getRecommendationVersion());
        week.setBuId(input.getBuId());
        week.setMartId(input.getMartId());
        week.setCreatedBy(input.getCreatedBy());
        week.setCreatedDate(LocalDateTime.now());
        week.setModifiedBy(input.getModifiedBy());
        week.setModifiedDate(LocalDateTime.now());
        week.setEntityVersion(1L);
        return placementWeekRepository.save(week);
    }

    public PlacementWeek updatePlacementWeekStatus(String weekId, String status, String modifiedBy) {
        byte[] pk = uuidToBytes(weekId);
        PlacementWeek week = placementWeekRepository.findById(pk).orElse(null);
        if (week == null) {
            return null;
        }
        week.setStatus(status);
        week.setModifiedBy(modifiedBy);
        week.setModifiedDate(LocalDateTime.now());
        return placementWeekRepository.save(week);
    }

    public List<PlacementWeekItem> getItemsByWeekId(String weekId) {
        byte[] weekIdBytes = uuidToBytes(weekId);
        return placementWeekItemRepository.findAll().stream()
                .filter(i -> java.util.Arrays.equals(i.getWeekId(), weekIdBytes))
                .collect(Collectors.toList());
    }

    public PlacementWeekItem addItemToWeek(String weekId, String itemId, String partnerId,
                                           Double price, Integer stackRank, String buId,
                                           String martId, String createdBy) {
        PlacementWeekItem item = new PlacementWeekItem();
        item.setWeekItemId(uuidToBytes(UUID.randomUUID().toString()));
        item.setWeekId(uuidToBytes(weekId));
        item.setItemId(itemId);
        item.setPartnerId(partnerId);
        item.setPrice(price);
        item.setStackRank(stackRank);
        item.setBuId(buId);
        item.setMartId(martId);
        item.setCreatedBy(createdBy);
        item.setCreatedDate(LocalDateTime.now());
        item.setModifiedDate(LocalDateTime.now());
        item.setEntityVersion(1L);
        return placementWeekItemRepository.save(item);
    }

    public PlacementWeekItem updateItemRank(String weekItemId, Integer newRank, String modifiedBy) {
        byte[] pk = uuidToBytes(weekItemId);
        PlacementWeekItem item = placementWeekItemRepository.findById(pk).orElse(null);
        if (item == null) {
            return null;
        }
        item.setStackRank(newRank);
        item.setModifiedBy(modifiedBy);
        item.setModifiedDate(LocalDateTime.now());
        return placementWeekItemRepository.save(item);
    }

    private byte[] uuidToBytes(String uuidStr) {
        UUID uuid = UUID.fromString(uuidStr);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private String bytesToUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }
}
