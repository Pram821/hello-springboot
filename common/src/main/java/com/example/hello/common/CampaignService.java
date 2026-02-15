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

@Service
public class CampaignService {

    @Autowired
    private CampaignRepository campaignRepository;

    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    public Campaign getCampaignById(String campaignId) {
        byte[] pk = uuidToBytes(campaignId);
        return campaignRepository.findById(pk).orElse(null);
    }

    public Campaign createCampaign(CampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.setCampaignPk(uuidToBytes(UUID.randomUUID().toString()));
        campaign.setCampaignName(request.getName());
        campaign.setCampaignType(request.getCampaignType());
        campaign.setCampaignStatus(request.getCampaignStatus());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setCreatedDate(LocalDateTime.now());
        campaign.setModifiedDate(LocalDateTime.now());
        campaign.setCreatedBy(request.getCreatedBy());
        campaign.setModifiedBy(request.getModifiedBy());
        campaign.setEntityVersion(1L);
        return campaignRepository.save(campaign);
    }

    private byte[] uuidToBytes(String uuidStr) {
        UUID uuid = UUID.fromString(uuidStr);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
