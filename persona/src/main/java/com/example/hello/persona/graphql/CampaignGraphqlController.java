package com.example.hello.persona.graphql;

import com.example.hello.common.CampaignService;
import com.example.hello.common.exception.ResourceNotFoundException;
import com.mp.flashpicks.common.dto.CampaignRequest;
import com.mp.flashpicks.common.entity.Campaign;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class CampaignGraphqlController {

    private static final Logger log = LoggerFactory.getLogger(CampaignGraphqlController.class);

    private final CampaignService campaignService;
    private final Counter gqlQueryCounter;
    private final Counter gqlMutationCounter;

    public CampaignGraphqlController(CampaignService campaignService, MeterRegistry meterRegistry) {
        this.campaignService = campaignService;
        this.gqlQueryCounter = Counter.builder("graphql.campaign.query.requests")
                .description("GraphQL campaign query requests")
                .register(meterRegistry);
        this.gqlMutationCounter = Counter.builder("graphql.campaign.mutation.requests")
                .description("GraphQL campaign mutation requests")
                .register(meterRegistry);
    }

    @QueryMapping
    public List<Campaign> campaigns() {
        gqlQueryCounter.increment();
        log.info("GraphQL query: campaigns");
        return campaignService.getAllCampaigns();
    }

    @QueryMapping
    public Campaign campaignById(@Argument String id) {
        gqlQueryCounter.increment();
        log.info("GraphQL query: campaignById({})", id);
        Campaign campaign = campaignService.getCampaignById(id);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign not found: " + id);
        }
        return campaign;
    }

    @MutationMapping
    public Campaign createCampaign(@Argument Map<String, String> input) {
        gqlMutationCounter.increment();
        log.info("GraphQL mutation: createCampaign");
        CampaignRequest request = new CampaignRequest();
        request.setName(input.get("name"));
        request.setCampaignType(input.get("campaignType"));
        request.setCampaignStatus(input.get("campaignStatus"));
        request.setCreatedBy(input.get("createdBy"));
        return campaignService.createCampaign(request);
    }
}
