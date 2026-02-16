package com.example.hello.common;

import com.mp.flashpicks.common.dto.CampaignPartnerItemInput;
import com.mp.flashpicks.common.entity.CampaignPartnerItem;
import com.mp.flashpicks.common.entity.CampaignPartnerMessage;
import com.mp.flashpicks.common.entity.ItemDetail;
import com.mp.flashpicks.common.repository.CampaignPartnerItemRepository;
import com.mp.flashpicks.common.repository.CampaignPartnerMessageRepository;
import com.mp.flashpicks.common.repository.ItemDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItemSubmissionService {

    @Autowired
    private CampaignPartnerItemRepository campaignPartnerItemRepository;

    @Autowired
    private CampaignPartnerMessageRepository campaignPartnerMessageRepository;

    @Autowired
    private ItemDetailRepository itemDetailRepository;

    public CampaignPartnerItem submitItemToCampaign(CampaignPartnerItemInput input) {
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
        item.setReviewAction("PENDING");
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

    public List<CampaignPartnerItem> getItemsByCampaign(String campaignPk) {
        byte[] campaignPkBytes = uuidToBytes(campaignPk);
        return campaignPartnerItemRepository.findAll().stream()
                .filter(i -> java.util.Arrays.equals(i.getCampaignPk(), campaignPkBytes))
                .collect(Collectors.toList());
    }

    public List<CampaignPartnerItem> getItemsByPartner(String partnerId) {
        return campaignPartnerItemRepository.findAll().stream()
                .filter(i -> partnerId.equals(i.getPartnerId()))
                .collect(Collectors.toList());
    }

    public CampaignPartnerMessage addMessage(String campaignPartnerId, String comment,
                                             String postedBy, String source) {
        CampaignPartnerMessage message = new CampaignPartnerMessage();
        message.setCampaignPartnerMessageId(uuidToBytes(UUID.randomUUID().toString()));
        message.setCampaignPartnerId(uuidToBytes(campaignPartnerId));
        message.setComment(comment);
        message.setPostedBy(postedBy);
        message.setSource(source);
        message.setPostedOn(LocalDateTime.now());
        message.setModifiedDate(LocalDateTime.now());
        message.setEntityVersion(1L);
        return campaignPartnerMessageRepository.save(message);
    }

    public List<CampaignPartnerMessage> getMessages(String campaignPartnerId) {
        byte[] partnerIdBytes = uuidToBytes(campaignPartnerId);
        return campaignPartnerMessageRepository.findAll().stream()
                .filter(m -> java.util.Arrays.equals(m.getCampaignPartnerId(), partnerIdBytes))
                .collect(Collectors.toList());
    }

    public ItemDetail getItemDetail(String itemId) {
        return itemDetailRepository.findById(itemId).orElse(null);
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
