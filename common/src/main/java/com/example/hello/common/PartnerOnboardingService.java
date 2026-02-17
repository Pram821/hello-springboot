package com.example.hello.common;

import com.mp.flashpicks.common.entity.ApprovedPartner;
import com.mp.flashpicks.common.entity.ProspectivePartner;
import com.mp.flashpicks.common.repository.ApprovedPartnerRepository;
import com.mp.flashpicks.common.repository.ProspectivePartnerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PartnerOnboardingService {

    private final ProspectivePartnerRepository prospectivePartnerRepository;
    private final ApprovedPartnerRepository approvedPartnerRepository;

    public PartnerOnboardingService(ProspectivePartnerRepository prospectivePartnerRepository,
                                     ApprovedPartnerRepository approvedPartnerRepository) {
        this.prospectivePartnerRepository = prospectivePartnerRepository;
        this.approvedPartnerRepository = approvedPartnerRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "partners", key = "'prospective'")
    public List<ProspectivePartner> getAllProspectivePartners() {
        return prospectivePartnerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ProspectivePartner> getProspectivePartnerById(String id) {
        return prospectivePartnerRepository.findById(uuidToBytes(id));
    }

    @Transactional
    @CacheEvict(value = "partners", allEntries = true)
    public ProspectivePartner createProspectivePartner(String partnerId, String sellerName, String buId, String martId, String createdBy) {
        ProspectivePartner partner = new ProspectivePartner();
        partner.setProspectivePartnerId(uuidToBytes(UUID.randomUUID().toString()));
        partner.setPartnerId(partnerId);
        partner.setSellerName(sellerName);
        partner.setBuId(buId);
        partner.setMartId(martId);
        partner.setCreatedBy(createdBy);
        partner.setCreatedDate(LocalDateTime.now());
        partner.setModifiedDate(LocalDateTime.now());
        partner.setEntityVersion(1L);
        return prospectivePartnerRepository.save(partner);
    }

    @Transactional
    @CacheEvict(value = "partners", allEntries = true)
    public ApprovedPartner approvePartner(String prospectivePartnerId, String approvedBy) {
        ProspectivePartner prospective = prospectivePartnerRepository.findById(uuidToBytes(prospectivePartnerId))
                .orElseThrow(() -> new com.example.hello.common.exception.ResourceNotFoundException(
                        "Prospective partner not found: " + prospectivePartnerId));

        ApprovedPartner approved = new ApprovedPartner();
        approved.setPartnerId(prospective.getPartnerId());
        approved.setSellerName(prospective.getSellerName());
        approved.setBuId(prospective.getBuId());
        approved.setMartId(prospective.getMartId());
        approved.setCreatedBy(approvedBy);
        approved.setCreatedDate(LocalDateTime.now());
        approved.setModifiedDate(LocalDateTime.now());
        approved.setEntityVersion(1L);

        ApprovedPartner saved = approvedPartnerRepository.save(approved);
        prospectivePartnerRepository.deleteById(uuidToBytes(prospectivePartnerId));
        return saved;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "partners", key = "'approved'")
    public List<ApprovedPartner> getAllApprovedPartners() {
        return approvedPartnerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ApprovedPartner> getApprovedPartner(String partnerId) {
        return approvedPartnerRepository.findById(partnerId);
    }

    private byte[] uuidToBytes(String uuidStr) {
        UUID uuid = UUID.fromString(uuidStr);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
