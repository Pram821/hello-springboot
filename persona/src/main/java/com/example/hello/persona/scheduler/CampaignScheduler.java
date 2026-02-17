package com.example.hello.persona.scheduler;

import com.example.hello.common.CampaignLifecycleService;
import com.mp.flashpicks.common.entity.Campaign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CampaignScheduler {

    private static final Logger log = LoggerFactory.getLogger(CampaignScheduler.class);
    private final CampaignLifecycleService lifecycleService;

    public CampaignScheduler(CampaignLifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void autoActivateCampaigns() {
        log.info("Scheduler: Checking for campaigns to auto-activate");
        List<Campaign> openCampaigns = lifecycleService.getCampaignsByStatus("OPEN");
        LocalDateTime now = LocalDateTime.now();
        long activated = openCampaigns.stream()
                .filter(c -> c.getStartDate() != null && c.getStartDate().isBefore(now))
                .peek(c -> {
                    try {
                        lifecycleService.updateCampaignStatus(
                                bytesToUuid(c.getCampaignPk()), "LIVE", "SYSTEM_SCHEDULER");
                        log.info("Auto-activated campaign: {}", c.getCampaignName());
                    } catch (Exception e) {
                        log.warn("Failed to auto-activate campaign {}: {}", c.getCampaignName(), e.getMessage());
                    }
                })
                .count();
        log.info("Scheduler: Auto-activated {} campaigns", activated);
    }

    @Scheduled(cron = "0 0 1 * * ?") // daily at 1 AM
    public void autoCompleteCampaigns() {
        log.info("Scheduler: Checking for campaigns to auto-complete");
        List<Campaign> liveCampaigns = lifecycleService.getCampaignsByStatus("LIVE");
        LocalDateTime now = LocalDateTime.now();
        liveCampaigns.stream()
                .filter(c -> c.getEndDate() != null && c.getEndDate().isBefore(now))
                .forEach(c -> {
                    try {
                        lifecycleService.updateCampaignStatus(
                                bytesToUuid(c.getCampaignPk()), "COMPLETED", "SYSTEM_SCHEDULER");
                        log.info("Auto-completed campaign: {}", c.getCampaignName());
                    } catch (Exception e) {
                        log.warn("Failed to auto-complete campaign {}: {}", c.getCampaignName(), e.getMessage());
                    }
                });
    }

    private String bytesToUuid(byte[] bytes) {
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(bytes);
        java.util.UUID uuid = new java.util.UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }
}
