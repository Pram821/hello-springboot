package com.example.hello.common;

import com.mp.flashpicks.common.dto.PlacementWeekInput;
import com.mp.flashpicks.common.entity.PlacementWeek;
import com.mp.flashpicks.common.entity.PlacementWeekItem;
import com.mp.flashpicks.common.repository.PlacementWeekItemRepository;
import com.mp.flashpicks.common.repository.PlacementWeekRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlacementWeekService {

    private final PlacementWeekRepository placementWeekRepository;
    private final PlacementWeekItemRepository placementWeekItemRepository;

    public PlacementWeekService(PlacementWeekRepository placementWeekRepository,
                                 PlacementWeekItemRepository placementWeekItemRepository) {
        this.placementWeekRepository = placementWeekRepository;
        this.placementWeekItemRepository = placementWeekItemRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "placementWeeks")
    public List<PlacementWeek> getAllPlacementWeeks() {
        return placementWeekRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PlacementWeek> getPlacementWeekById(String id) {
        return placementWeekRepository.findById(uuidToBytes(id));
    }

    @Transactional
    @CacheEvict(value = "placementWeeks", allEntries = true)
    public PlacementWeek createPlacementWeek(PlacementWeekInput input) {
        PlacementWeek week = new PlacementWeek();
        week.setWeekId(uuidToBytes(UUID.randomUUID().toString()));
        week.setName(input.getName());
        week.setCampaignType(input.getCampaignType());
        Optional.ofNullable(input.getStartDate()).map(LocalDateTime::parse).ifPresent(week::setStartDate);
        Optional.ofNullable(input.getEndDate()).map(LocalDateTime::parse).ifPresent(week::setEndDate);
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

    @Transactional
    @CacheEvict(value = "placementWeeks", allEntries = true)
    public PlacementWeek updatePlacementWeekStatus(String weekId, String status, String modifiedBy) {
        PlacementWeek week = placementWeekRepository.findById(uuidToBytes(weekId))
                .orElseThrow(() -> new com.example.hello.common.exception.ResourceNotFoundException(
                        "Placement week not found: " + weekId));
        week.setStatus(status);
        week.setModifiedBy(modifiedBy);
        week.setModifiedDate(LocalDateTime.now());
        return placementWeekRepository.save(week);
    }

    @Transactional(readOnly = true)
    public List<PlacementWeekItem> getItemsByWeekId(String weekId) {
        byte[] weekIdBytes = uuidToBytes(weekId);
        return placementWeekItemRepository.findAll().stream()
                .filter(i -> java.util.Arrays.equals(i.getWeekId(), weekIdBytes))
                .collect(Collectors.toList());
    }

    @Transactional
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

    @Transactional
    public PlacementWeekItem updateItemRank(String weekItemId, Integer newRank, String modifiedBy) {
        PlacementWeekItem item = placementWeekItemRepository.findById(uuidToBytes(weekItemId))
                .orElseThrow(() -> new com.example.hello.common.exception.ResourceNotFoundException(
                        "Placement week item not found: " + weekItemId));
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
}
