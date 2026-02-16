package com.example.hello.common;

import com.mp.flashpicks.common.dto.CampaignRequest;
import com.mp.flashpicks.common.entity.Campaign;
import com.mp.flashpicks.common.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CampaignLifecycleService {

    @Autowired
    private CampaignRepository campaignRepository;

    public Campaign updateCampaignStatus(String campaignId, String newStatus, String modifiedBy) {
        byte[] pk = uuidToBytes(campaignId);
        Campaign campaign = campaignRepository.findById(pk).orElse(null);
        if (campaign == null) {
            return null;
        }

        String currentStatus = campaign.getCampaignStatus();
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        campaign.setCampaignStatus(newStatus);
        campaign.setModifiedBy(modifiedBy);
        campaign.setModifiedDate(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }

    public Campaign updateCampaign(String campaignId, CampaignRequest request) {
        byte[] pk = uuidToBytes(campaignId);
        Campaign campaign = campaignRepository.findById(pk).orElse(null);
        if (campaign == null) {
            return null;
        }

        if (request.getName() != null) {
            campaign.setCampaignName(request.getName());
        }
        if (request.getCampaignType() != null) {
            campaign.setCampaignType(request.getCampaignType());
        }
        if (request.getStartDate() != null) {
            campaign.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            campaign.setEndDate(request.getEndDate());
        }
        campaign.setModifiedBy(request.getModifiedBy());
        campaign.setModifiedDate(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }

    public List<Campaign> getCampaignsByStatus(String status) {
        return campaignRepository.findAll().stream()
                .filter(c -> status.equals(c.getCampaignStatus()))
                .collect(Collectors.toList());
    }

    private boolean isValidTransition(String currentStatus, String newStatus) {
        if ("CANCELLED".equals(newStatus)) {
            return true;
        }
        if ("DRAFT".equals(currentStatus) && "OPEN".equals(newStatus)) {
            return true;
        }
        if ("OPEN".equals(currentStatus) && "LIVE".equals(newStatus)) {
            return true;
        }
        if ("LIVE".equals(currentStatus) && "COMPLETED".equals(newStatus)) {
            return true;
        }
        return false;
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
