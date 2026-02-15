package com.example.hello.persona.graphql;

import com.example.hello.common.CampaignPartnerItemService;
import com.example.hello.common.exception.ResourceNotFoundException;
import com.mp.flashpicks.common.dto.CampaignPartnerItemInput;
import com.mp.flashpicks.common.entity.CampaignPartnerItem;
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
public class CampaignPartnerItemGraphqlController {

    private static final Logger log = LoggerFactory.getLogger(CampaignPartnerItemGraphqlController.class);

    private final CampaignPartnerItemService itemService;
    private final Counter gqlQueryCounter;
    private final Counter gqlMutationCounter;

    public CampaignPartnerItemGraphqlController(CampaignPartnerItemService itemService, MeterRegistry meterRegistry) {
        this.itemService = itemService;
        this.gqlQueryCounter = Counter.builder("graphql.partner_item.query.requests")
                .description("GraphQL partner item query requests")
                .register(meterRegistry);
        this.gqlMutationCounter = Counter.builder("graphql.partner_item.mutation.requests")
                .description("GraphQL partner item mutation requests")
                .register(meterRegistry);
    }

    @QueryMapping
    public List<CampaignPartnerItem> campaignPartnerItems() {
        gqlQueryCounter.increment();
        log.info("GraphQL query: campaignPartnerItems");
        return itemService.getAllItems();
    }

    @QueryMapping
    public CampaignPartnerItem campaignPartnerItemById(@Argument String id) {
        gqlQueryCounter.increment();
        log.info("GraphQL query: campaignPartnerItemById({})", id);
        CampaignPartnerItem item = itemService.getItemById(id);
        if (item == null) {
            throw new ResourceNotFoundException("Campaign partner item not found: " + id);
        }
        return item;
    }

    @MutationMapping
    public CampaignPartnerItem createCampaignPartnerItem(@Argument Map<String, Object> input) {
        gqlMutationCounter.increment();
        log.info("GraphQL mutation: createCampaignPartnerItem");
        CampaignPartnerItemInput itemInput = new CampaignPartnerItemInput();
        itemInput.setCampaignPartnerId((String) input.get("campaignPartnerId"));
        itemInput.setCampaignPk((String) input.get("campaignPk"));
        itemInput.setPartnerId((String) input.get("partnerId"));
        itemInput.setItemId((String) input.get("itemId"));
        if (input.get("retailPrice") != null) {
            itemInput.setRetailPrice(((Number) input.get("retailPrice")).floatValue());
        }
        if (input.get("promoPrice") != null) {
            itemInput.setPromoPrice(((Number) input.get("promoPrice")).floatValue());
        }
        itemInput.setSource((String) input.get("source"));
        itemInput.setCreatedBy((String) input.get("createdBy"));
        return itemService.createItem(itemInput);
    }
}
