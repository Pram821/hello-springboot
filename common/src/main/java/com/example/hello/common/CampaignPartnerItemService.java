package com.example.hello.common;

import com.mp.flashpicks.common.dto.CampaignPartnerItemInput;
import com.mp.flashpicks.common.entity.CampaignPartnerItem;
import com.mp.flashpicks.common.repository.CampaignPartnerItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CampaignPartnerItemService {

    @Autowired
    private CampaignPartnerItemRepository campaignPartnerItemRepository;

    public List<CampaignPartnerItem> getAllItems() {
        return campaignPartnerItemRepository.findAll();
    }

    public CampaignPartnerItem getItemById(String id) {
        byte[] pk = uuidToBytes(id);
        return campaignPartnerItemRepository.findById(pk).orElse(null);
    }

    public CampaignPartnerItem createItem(CampaignPartnerItemInput input) {
        CampaignPartnerItem item = new CampaignPartnerItem();
        item.setCampaignPartnerItemId(uuidToBytes(UUID.randomUUID().toString()));
        item.setCampaignPartnerId(uuidToBytes(input.getCampaignPartnerId()));
        if (input.getCampaignPk() != null) {
            item.setCampaignPk(uuidToBytes(input.getCampaignPk()));
        }
        item.setPartnerId(input.getPartnerId());
        item.setItemId(input.getItemId());
        item.setAsin(input.getAsin());
        if (input.getStartDate() != null) {
            item.setStartDate(LocalDateTime.parse(input.getStartDate()));
        }
        if (input.getEndDate() != null) {
            item.setEndDate(LocalDateTime.parse(input.getEndDate()));
        }
        item.setAvailableInventory(input.getAvailableInventory());
        item.setForecastQuantity(input.getForecastQuantity());
        item.setRetailPrice(input.getRetailPrice());
        item.setPromoPrice(input.getPromoPrice());
        item.setPromotionalFlag(input.getPromotionalFlag());
        item.setOptWfs(input.getOptWfs());
        item.setReviewAction(input.getReviewAction());
        item.setAdditionalNotes(input.getAdditionalNotes());
        item.setCommissionRate(input.getCommissionRate());
        item.setCurrency(input.getCurrency());
        item.setDealStrength(input.getDealStrength());
        item.setDealStrengthScore(input.getDealStrengthScore());
        item.setIsDealWmtExclusive(input.getIsDealWmtExclusive());
        item.setIsExpeditedShipping(input.getIsExpeditedShipping());
        item.setIsItemOos(input.getIsItemOos());
        item.setItemStatus(input.getItemStatus());
        item.setIsItemWmtExclusive(input.getIsItemWmtExclusive());
        item.setSource(input.getSource());
        item.setUpc(input.getUpc());
        item.setCreatedBy(input.getCreatedBy());
        item.setCreatedDate(LocalDateTime.now());
        item.setModifiedBy(input.getModifiedBy());
        item.setModifiedDate(LocalDateTime.now());
        item.setEntityVersion(1L);
        item.setSkuId(input.getSkuId());
        item.setOfferId(input.getOfferId());
        item.setSpecialTags(input.getSpecialTags());
        return campaignPartnerItemRepository.save(item);
    }

    private byte[] uuidToBytes(String uuidStr) {
        UUID uuid = UUID.fromString(uuidStr);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
