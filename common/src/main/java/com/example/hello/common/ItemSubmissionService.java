package com.example.hello.common;

import com.mp.flashpicks.common.dto.CampaignPartnerItemInput;
import com.mp.flashpicks.common.entity.CampaignPartnerItem;
import com.mp.flashpicks.common.entity.CampaignPartnerMessage;
import com.mp.flashpicks.common.entity.ItemDetail;
import com.mp.flashpicks.common.repository.CampaignPartnerItemRepository;
import com.mp.flashpicks.common.repository.CampaignPartnerMessageRepository;
import com.mp.flashpicks.common.repository.ItemDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItemSubmissionService {

    private final CampaignPartnerItemRepository campaignPartnerItemRepository;
    private final CampaignPartnerMessageRepository campaignPartnerMessageRepository;
    private final ItemDetailRepository itemDetailRepository;

    public ItemSubmissionService(CampaignPartnerItemRepository campaignPartnerItemRepository,
                                  CampaignPartnerMessageRepository campaignPartnerMessageRepository,
                                  ItemDetailRepository itemDetailRepository) {
        this.campaignPartnerItemRepository = campaignPartnerItemRepository;
        this.campaignPartnerMessageRepository = campaignPartnerMessageRepository;
        this.itemDetailRepository = itemDetailRepository;
    }

    @Transactional
    public CampaignPartnerItem submitItemToCampaign(CampaignPartnerItemInput input) {
        CampaignPartnerItem item = new CampaignPartnerItem();
        item.setCampaignPartnerItemId(uuidToBytes(UUID.randomUUID().toString()));
        item.setCampaignPartnerId(uuidToBytes(input.getCampaignPartnerId()));
        Optional.ofNullable(input.getCampaignPk()).map(this::uuidToBytes).ifPresent(item::setCampaignPk);
        item.setPartnerId(input.getPartnerId());
        item.setItemId(input.getItemId());
        item.setAsin(input.getAsin());
        Optional.ofNullable(input.getStartDate()).map(LocalDateTime::parse).ifPresent(item::setStartDate);
        Optional.ofNullable(input.getEndDate()).map(LocalDateTime::parse).ifPresent(item::setEndDate);
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

    @Transactional(readOnly = true)
    public List<CampaignPartnerItem> getItemsByCampaign(String campaignPk) {
        byte[] campaignPkBytes = uuidToBytes(campaignPk);
        return campaignPartnerItemRepository.findAll().stream()
                .filter(i -> java.util.Arrays.equals(i.getCampaignPk(), campaignPkBytes))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CampaignPartnerItem> getItemsByPartner(String partnerId) {
        return campaignPartnerItemRepository.findAll().stream()
                .filter(i -> partnerId.equals(i.getPartnerId()))
                .collect(Collectors.toList());
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public List<CampaignPartnerMessage> getMessages(String campaignPartnerId) {
        byte[] partnerIdBytes = uuidToBytes(campaignPartnerId);
        return campaignPartnerMessageRepository.findAll().stream()
                .filter(m -> java.util.Arrays.equals(m.getCampaignPartnerId(), partnerIdBytes))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ItemDetail> getItemDetail(String itemId) {
        return itemDetailRepository.findById(itemId);
    }

    private byte[] uuidToBytes(String uuidStr) {
        UUID uuid = UUID.fromString(uuidStr);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
