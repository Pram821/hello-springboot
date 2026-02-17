package com.example.hello.common;

import com.mp.flashpicks.common.dto.CampaignRequest;
import com.mp.flashpicks.common.entity.Campaign;
import com.mp.flashpicks.common.repository.CampaignRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CampaignLifecycleService {

    private final CampaignRepository campaignRepository;

    public CampaignLifecycleService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Transactional
    @CacheEvict(value = "campaigns", allEntries = true)
    public Campaign updateCampaignStatus(String campaignId, String newStatus, String modifiedBy) {
        byte[] pk = uuidToBytes(campaignId);
        Campaign campaign = campaignRepository.findById(pk)
                .orElseThrow(() -> new com.example.hello.common.exception.ResourceNotFoundException(
                        "Campaign not found: " + campaignId));

        String currentStatus = campaign.getCampaignStatus();
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        campaign.setCampaignStatus(newStatus);
        campaign.setModifiedBy(modifiedBy);
        campaign.setModifiedDate(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }

    @Transactional
    @CacheEvict(value = "campaigns", allEntries = true)
    public Campaign updateCampaign(String campaignId, CampaignRequest request) {
        byte[] pk = uuidToBytes(campaignId);
        Campaign campaign = campaignRepository.findById(pk)
                .orElseThrow(() -> new com.example.hello.common.exception.ResourceNotFoundException(
                        "Campaign not found: " + campaignId));

        java.util.Optional.ofNullable(request.getName()).ifPresent(campaign::setCampaignName);
        java.util.Optional.ofNullable(request.getCampaignType()).ifPresent(campaign::setCampaignType);
        java.util.Optional.ofNullable(request.getStartDate()).ifPresent(campaign::setStartDate);
        java.util.Optional.ofNullable(request.getEndDate()).ifPresent(campaign::setEndDate);
        campaign.setModifiedBy(request.getModifiedBy());
        campaign.setModifiedDate(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }

    @Transactional(readOnly = true)
    public List<Campaign> getCampaignsByStatus(String status) {
        return campaignRepository.findAll().stream()
                .filter(c -> status.equals(c.getCampaignStatus()))
                .collect(Collectors.toList());
    }

    private boolean isValidTransition(String currentStatus, String newStatus) {
        return "CANCELLED".equals(newStatus)
                || ("DRAFT".equals(currentStatus) && "OPEN".equals(newStatus))
                || ("OPEN".equals(currentStatus) && "LIVE".equals(newStatus))
                || ("LIVE".equals(currentStatus) && "COMPLETED".equals(newStatus));
    }

    private byte[] uuidToBytes(String uuidStr) {
        UUID uuid = UUID.fromString(uuidStr);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
