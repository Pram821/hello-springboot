package com.example.hello.common;

import com.mp.flashpicks.common.dto.WmtDiscountInput;
import com.mp.flashpicks.common.entity.PromoEntity;
import com.mp.flashpicks.common.entity.WmtDiscount;
import com.mp.flashpicks.common.repository.PromoEntityRepository;
import com.mp.flashpicks.common.repository.WmtDiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PromoService {

    @Autowired
    private PromoEntityRepository promoEntityRepository;

    @Autowired
    private WmtDiscountRepository wmtDiscountRepository;

    public List<PromoEntity> getAllPromos() {
        return promoEntityRepository.findAll();
    }

    public PromoEntity getPromoById(String id) {
        byte[] pk = uuidToBytes(id);
        return promoEntityRepository.findById(pk).orElse(null);
    }

    public List<PromoEntity> getPromosByCampaignPartnerItemId(String itemId) {
        byte[] itemIdBytes = uuidToBytes(itemId);
        return promoEntityRepository.findAll().stream()
                .filter(p -> java.util.Arrays.equals(p.getCampaignPartnerItemId(), itemIdBytes))
                .collect(Collectors.toList());
    }

    public PromoEntity createPromo(String campaignPartnerItemId, String promoId, String promoStatus,
                                   LocalDateTime startDate, LocalDateTime endDate, String createdBy) {
        PromoEntity promo = new PromoEntity();
        promo.setPromoEntityId(uuidToBytes(UUID.randomUUID().toString()));
        promo.setCampaignPartnerItemId(uuidToBytes(campaignPartnerItemId));
        promo.setPromoId(promoId);
        promo.setPromoStatus(promoStatus);
        promo.setStartDate(startDate);
        promo.setEndDate(endDate);
        promo.setCreatedBy(createdBy);
        promo.setCreatedDate(LocalDateTime.now());
        promo.setModifiedDate(LocalDateTime.now());
        promo.setEntityVersion(1L);
        return promoEntityRepository.save(promo);
    }

    public PromoEntity updatePromoStatus(String promoEntityId, String newStatus, String previousStatus,
                                         String eventAction, String modifiedBy) {
        byte[] pk = uuidToBytes(promoEntityId);
        PromoEntity promo = promoEntityRepository.findById(pk).orElse(null);
        if (promo == null) {
            return null;
        }
        promo.setPromoStatus(newStatus);
        promo.setPreviousPromoStatus(previousStatus);
        promo.setEventAction(eventAction);
        promo.setModifiedBy(modifiedBy);
        promo.setModifiedDate(LocalDateTime.now());
        return promoEntityRepository.save(promo);
    }

    public WmtDiscount createWmtDiscount(WmtDiscountInput input) {
        WmtDiscount discount = new WmtDiscount();
        discount.setWmtDiscountId(uuidToBytes(UUID.randomUUID().toString()));
        discount.setCampaignPartnerItemId(uuidToBytes(input.getCampaignPartnerItemId()));
        discount.setWmtDiscount(input.getWmtDiscount());
        discount.setWmtDiscountAcceptance(input.getWmtDiscountAcceptance());
        discount.setOfferedBy(input.getOfferedBy());
        if (input.getOfferedOn() != null) {
            discount.setOfferedOn(LocalDateTime.parse(input.getOfferedOn()));
        }
        discount.setAcceptedBy(input.getAcceptedBy());
        if (input.getAcceptedOn() != null) {
            discount.setAcceptedOn(LocalDateTime.parse(input.getAcceptedOn()));
        }
        discount.setCreatedBy(input.getCreatedBy());
        discount.setCreatedDate(LocalDateTime.now());
        discount.setModifiedBy(input.getModifiedBy());
        discount.setModifiedDate(LocalDateTime.now());
        discount.setEntityVersion(1L);
        return wmtDiscountRepository.save(discount);
    }

    public List<WmtDiscount> getDiscountsByItemId(String itemId) {
        byte[] itemIdBytes = uuidToBytes(itemId);
        return wmtDiscountRepository.findAll().stream()
                .filter(d -> java.util.Arrays.equals(d.getCampaignPartnerItemId(), itemIdBytes))
                .collect(Collectors.toList());
    }

    public WmtDiscount getDiscountById(String wmtDiscountId) {
        byte[] pk = uuidToBytes(wmtDiscountId);
        return wmtDiscountRepository.findById(pk).orElse(null);
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
