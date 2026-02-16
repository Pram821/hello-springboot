package com.example.hello.common;

import com.mp.flashpicks.common.entity.ApprovedPartner;
import com.mp.flashpicks.common.entity.ProspectivePartner;
import com.mp.flashpicks.common.repository.ApprovedPartnerRepository;
import com.mp.flashpicks.common.repository.ProspectivePartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PartnerOnboardingService {

    @Autowired
    private ProspectivePartnerRepository prospectivePartnerRepository;

    @Autowired
    private ApprovedPartnerRepository approvedPartnerRepository;

    public List<ProspectivePartner> getAllProspectivePartners() {
        return prospectivePartnerRepository.findAll();
    }

    public ProspectivePartner getProspectivePartnerById(String id) {
        byte[] pk = uuidToBytes(id);
        return prospectivePartnerRepository.findById(pk).orElse(null);
    }

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

    public ApprovedPartner approvePartner(String prospectivePartnerId, String approvedBy) {
        byte[] pk = uuidToBytes(prospectivePartnerId);
        ProspectivePartner prospective = prospectivePartnerRepository.findById(pk).orElse(null);
        if (prospective == null) {
            return null;
        }

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
        prospectivePartnerRepository.deleteById(pk);
        return saved;
    }

    public List<ApprovedPartner> getAllApprovedPartners() {
        return approvedPartnerRepository.findAll();
    }

    public ApprovedPartner getApprovedPartner(String partnerId) {
        return approvedPartnerRepository.findById(partnerId).orElse(null);
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
