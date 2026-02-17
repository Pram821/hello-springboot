package com.example.hello.common;

import com.mp.flashpicks.common.entity.CampaignPartnerItem;
import com.mp.flashpicks.common.entity.ItemReviewReasonCode;
import com.mp.flashpicks.common.entity.ReviewReasonCode;
import com.mp.flashpicks.common.repository.CampaignPartnerItemRepository;
import com.mp.flashpicks.common.repository.ItemReviewReasonCodeRepository;
import com.mp.flashpicks.common.repository.ReviewReasonCodeRepository;
import com.mp.flashpicks.common.repository.RulesDataRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    private final CampaignPartnerItemRepository campaignPartnerItemRepository;
    private final ReviewReasonCodeRepository reviewReasonCodeRepository;
    private final ItemReviewReasonCodeRepository itemReviewReasonCodeRepository;
    private final RulesDataRepository rulesDataRepository;

    public ApprovalService(CampaignPartnerItemRepository campaignPartnerItemRepository,
                           ReviewReasonCodeRepository reviewReasonCodeRepository,
                           ItemReviewReasonCodeRepository itemReviewReasonCodeRepository,
                           RulesDataRepository rulesDataRepository) {
        this.campaignPartnerItemRepository = campaignPartnerItemRepository;
        this.reviewReasonCodeRepository = reviewReasonCodeRepository;
        this.itemReviewReasonCodeRepository = itemReviewReasonCodeRepository;
        this.rulesDataRepository = rulesDataRepository;
    }

    @Transactional(readOnly = true)
    public List<CampaignPartnerItem> getPendingItems() {
        return campaignPartnerItemRepository.findAll().stream()
                .filter(i -> "PENDING".equals(i.getReviewAction()))
                .collect(Collectors.toList());
    }

    @Transactional
    public CampaignPartnerItem approveItem(String campaignPartnerItemId, String approvedBy) {
        byte[] pk = uuidToBytes(campaignPartnerItemId);
        CampaignPartnerItem item = campaignPartnerItemRepository.findById(pk)
                .orElseThrow(() -> new com.example.hello.common.exception.ResourceNotFoundException(
                        "Item not found: " + campaignPartnerItemId));
        item.setReviewAction("APPROVED");
        item.setModifiedBy(approvedBy);
        item.setModifiedDate(LocalDateTime.now());
        return campaignPartnerItemRepository.save(item);
    }

    @Transactional
    public CampaignPartnerItem declineItem(String campaignPartnerItemId, String declinedBy, String reasonCodeId) {
        byte[] pk = uuidToBytes(campaignPartnerItemId);
        CampaignPartnerItem item = campaignPartnerItemRepository.findById(pk)
                .orElseThrow(() -> new com.example.hello.common.exception.ResourceNotFoundException(
                        "Item not found: " + campaignPartnerItemId));
        item.setReviewAction("DECLINED");
        item.setModifiedBy(declinedBy);
        item.setModifiedDate(LocalDateTime.now());
        CampaignPartnerItem saved = campaignPartnerItemRepository.save(item);

        ItemReviewReasonCode mapping = new ItemReviewReasonCode();
        mapping.setMappingId(uuidToBytes(UUID.randomUUID().toString()));
        mapping.setCampaignPartnerItemId(pk);
        mapping.setReasonCodeId(uuidToBytes(reasonCodeId));
        itemReviewReasonCodeRepository.save(mapping);

        return saved;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "reasonCodes")
    public List<ReviewReasonCode> getAllReasonCodes() {
        return reviewReasonCodeRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "reasonCodes", allEntries = true)
    public ReviewReasonCode createReasonCode(String code, String shortDesc, String longDesc,
                                             String type, String scope, String buId,
                                             String martId, String createdBy) {
        ReviewReasonCode reasonCode = new ReviewReasonCode();
        reasonCode.setReasonCodeId(uuidToBytes(UUID.randomUUID().toString()));
        reasonCode.setReviewReasonCode(code);
        reasonCode.setShortDescription(shortDesc);
        reasonCode.setLongDescription(longDesc);
        reasonCode.setType(type);
        reasonCode.setScope(scope);
        reasonCode.setBuId(buId);
        reasonCode.setMartId(martId);
        reasonCode.setCreatedBy(createdBy);
        reasonCode.setCreatedDate(LocalDateTime.now());
        reasonCode.setModifiedDate(LocalDateTime.now());
        reasonCode.setEntityVersion(1L);
        return reviewReasonCodeRepository.save(reasonCode);
    }

    @Transactional(readOnly = true)
    public List<ItemReviewReasonCode> getItemReviewReasons(String campaignPartnerItemId) {
        byte[] itemIdBytes = uuidToBytes(campaignPartnerItemId);
        return itemReviewReasonCodeRepository.findAll().stream()
                .filter(r -> java.util.Arrays.equals(r.getCampaignPartnerItemId(), itemIdBytes))
                .collect(Collectors.toList());
    }

    private byte[] uuidToBytes(String uuidStr) {
        UUID uuid = UUID.fromString(uuidStr);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
