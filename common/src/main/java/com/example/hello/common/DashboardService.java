package com.example.hello.common;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class DashboardService {

    private final CampaignService campaignService;
    private final PartnerOnboardingService partnerService;
    private final ApprovalService approvalService;
    private final PromoService promoService;
    private final PlacementWeekService placementWeekService;

    public DashboardService(CampaignService campaignService,
                            PartnerOnboardingService partnerService,
                            ApprovalService approvalService,
                            PromoService promoService,
                            PlacementWeekService placementWeekService) {
        this.campaignService = campaignService;
        this.partnerService = partnerService;
        this.approvalService = approvalService;
        this.promoService = promoService;
        this.placementWeekService = placementWeekService;
    }

    @Async("taskExecutor")
    public CompletableFuture<Integer> getCampaignCountAsync() {
        return CompletableFuture.completedFuture(campaignService.getAllCampaigns().size());
    }

    @Async("taskExecutor")
    public CompletableFuture<Integer> getPartnerCountAsync() {
        return CompletableFuture.completedFuture(partnerService.getAllApprovedPartners().size());
    }

    @Async("taskExecutor")
    public CompletableFuture<Integer> getPendingApprovalsCountAsync() {
        return CompletableFuture.completedFuture(approvalService.getPendingItems().size());
    }

    @Async("taskExecutor")
    public CompletableFuture<Integer> getPromoCountAsync() {
        return CompletableFuture.completedFuture(promoService.getAllPromos().size());
    }

    @Async("taskExecutor")
    public CompletableFuture<Integer> getPlacementWeekCountAsync() {
        return CompletableFuture.completedFuture(placementWeekService.getAllPlacementWeeks().size());
    }

    public CompletableFuture<Map<String, Integer>> getDashboardStats() {
        CompletableFuture<Integer> campaigns = getCampaignCountAsync();
        CompletableFuture<Integer> partners = getPartnerCountAsync();
        CompletableFuture<Integer> pendingApprovals = getPendingApprovalsCountAsync();
        CompletableFuture<Integer> promos = getPromoCountAsync();
        CompletableFuture<Integer> placementWeeks = getPlacementWeekCountAsync();

        return CompletableFuture.allOf(campaigns, partners, pendingApprovals, promos, placementWeeks)
                .thenApply(v -> Map.of(
                        "totalCampaigns", campaigns.join(),
                        "approvedPartners", partners.join(),
                        "pendingApprovals", pendingApprovals.join(),
                        "activePromos", promos.join(),
                        "placementWeeks", placementWeeks.join()
                ));
    }
}
