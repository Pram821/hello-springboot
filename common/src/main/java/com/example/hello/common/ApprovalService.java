package com.example.hello.common;

import com.mp.flashpicks.common.entity.CampaignPartnerItem;
import com.mp.flashpicks.common.entity.ItemReviewReasonCode;
import com.mp.flashpicks.common.entity.ReviewReasonCode;
import com.mp.flashpicks.common.repository.CampaignPartnerItemRepository;
import com.mp.flashpicks.common.repository.ItemReviewReasonCodeRepository;
import com.mp.flashpicks.common.repository.ReviewReasonCodeRepository;
import com.mp.flashpicks.common.repository.RulesDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    @Autowired
    private CampaignPartnerItemRepository campaignPartnerItemRepository;

    @Autowired
    private ReviewReasonCodeRepository reviewReasonCodeRepository;

    @Autowired
    private ItemReviewReasonCodeRepository itemReviewReasonCodeRepository;

    @Autowired
    private RulesDataRepository rulesDataRepository;

    public List<CampaignPartnerItem> getPendingItems() {
        return campaignPartnerItemRepository.findAll().stream()
                .filter(i -> "PENDING".equals(i.getReviewAction()))
                .collect(Collectors.toList());
    }

    public CampaignPartnerItem approveItem(String campaignPartnerItemId, String approvedBy) {
        byte[] pk = uuidToBytes(campaignPartnerItemId);
        CampaignPartnerItem item = campaignPartnerItemRepository.findById(pk).orElse(null);
        if (item == null) {
            return null;
        }
        item.setReviewAction("APPROVED");
        item.setModifiedBy(approvedBy);
        item.setModifiedDate(LocalDateTime.now());
        return campaignPartnerItemRepository.save(item);
    }

    public CampaignPartnerItem declineItem(String campaignPartnerItemId, String declinedBy, String reasonCodeId) {
        byte[] pk = uuidToBytes(campaignPartnerItemId);
        CampaignPartnerItem item = campaignPartnerItemRepository.findById(pk).orElse(null);
        if (item == null) {
            return null;
        }
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

    public List<ReviewReasonCode> getAllReasonCodes() {
        return reviewReasonCodeRepository.findAll();
    }

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

    public List<ItemReviewReasonCode> getItemReviewReasons(String campaignPartnerItemId) {
        byte[] itemIdBytes = uuidToBytes(campaignPartnerItemId);
        return itemReviewReasonCodeRepository.findAll().stream()
                .filter(r -> java.util.Arrays.equals(r.getCampaignPartnerItemId(), itemIdBytes))
                .collect(Collectors.toList());
    }

    // ItemReviewReasonCode is a simple mapping entity (no timestamps/version)

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
