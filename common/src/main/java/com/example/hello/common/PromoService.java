package com.example.hello.common;

import com.mp.flashpicks.common.dto.WmtDiscountInput;
import com.mp.flashpicks.common.entity.PromoEntity;
import com.mp.flashpicks.common.entity.WmtDiscount;
import com.mp.flashpicks.common.repository.PromoEntityRepository;
import com.mp.flashpicks.common.repository.WmtDiscountRepository;
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
public class PromoService {

    private final PromoEntityRepository promoEntityRepository;
    private final WmtDiscountRepository wmtDiscountRepository;

    public PromoService(PromoEntityRepository promoEntityRepository,
                        WmtDiscountRepository wmtDiscountRepository) {
        this.promoEntityRepository = promoEntityRepository;
        this.wmtDiscountRepository = wmtDiscountRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "promos")
    public List<PromoEntity> getAllPromos() {
        return promoEntityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PromoEntity> getPromoById(String id) {
        return promoEntityRepository.findById(uuidToBytes(id));
    }

    @Transactional(readOnly = true)
    public List<PromoEntity> getPromosByCampaignPartnerItemId(String itemId) {
        byte[] itemIdBytes = uuidToBytes(itemId);
        return promoEntityRepository.findAll().stream()
                .filter(p -> java.util.Arrays.equals(p.getCampaignPartnerItemId(), itemIdBytes))
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "promos", allEntries = true)
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

    @Transactional
    @CacheEvict(value = "promos", allEntries = true)
    public PromoEntity updatePromoStatus(String promoEntityId, String newStatus, String previousStatus,
                                         String eventAction, String modifiedBy) {
        PromoEntity promo = promoEntityRepository.findById(uuidToBytes(promoEntityId))
                .orElseThrow(() -> new com.example.hello.common.exception.ResourceNotFoundException(
                        "Promo not found: " + promoEntityId));
        promo.setPromoStatus(newStatus);
        promo.setPreviousPromoStatus(previousStatus);
        promo.setEventAction(eventAction);
        promo.setModifiedBy(modifiedBy);
        promo.setModifiedDate(LocalDateTime.now());
        return promoEntityRepository.save(promo);
    }

    @Transactional
    public WmtDiscount createWmtDiscount(WmtDiscountInput input) {
        WmtDiscount discount = new WmtDiscount();
        discount.setWmtDiscountId(uuidToBytes(UUID.randomUUID().toString()));
        discount.setCampaignPartnerItemId(uuidToBytes(input.getCampaignPartnerItemId()));
        discount.setWmtDiscount(input.getWmtDiscount());
        discount.setWmtDiscountAcceptance(input.getWmtDiscountAcceptance());
        discount.setOfferedBy(input.getOfferedBy());
        Optional.ofNullable(input.getOfferedOn()).map(LocalDateTime::parse).ifPresent(discount::setOfferedOn);
        discount.setAcceptedBy(input.getAcceptedBy());
        Optional.ofNullable(input.getAcceptedOn()).map(LocalDateTime::parse).ifPresent(discount::setAcceptedOn);
        discount.setCreatedBy(input.getCreatedBy());
        discount.setCreatedDate(LocalDateTime.now());
        discount.setModifiedBy(input.getModifiedBy());
        discount.setModifiedDate(LocalDateTime.now());
        discount.setEntityVersion(1L);
        return wmtDiscountRepository.save(discount);
    }

    @Transactional(readOnly = true)
    public List<WmtDiscount> getDiscountsByItemId(String itemId) {
        byte[] itemIdBytes = uuidToBytes(itemId);
        return wmtDiscountRepository.findAll().stream()
                .filter(d -> java.util.Arrays.equals(d.getCampaignPartnerItemId(), itemIdBytes))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<WmtDiscount> getDiscountById(String wmtDiscountId) {
        return wmtDiscountRepository.findById(uuidToBytes(wmtDiscountId));
    }

    private byte[] uuidToBytes(String uuidStr) {
        UUID uuid = UUID.fromString(uuidStr);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
