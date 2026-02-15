package com.example.hello.functional.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CampaignPartnerItemStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions resultActions;

    @When("I request all campaign partner items")
    public void i_request_all_campaign_partner_items() throws Exception {
        resultActions = mockMvc.perform(get("/api/campaign-partner-items"));
    }

    @When("I create a campaign partner item with partnerId {string} itemId {string} retailPrice {double} promoPrice {double}")
    public void i_create_a_campaign_partner_item(String partnerId, String itemId, double retailPrice, double promoPrice) throws Exception {
        String json = """
                {
                    "campaignPartnerId": "11111111-1111-1111-1111-111111111111",
                    "campaignPk": "22222222-2222-2222-2222-222222222222",
                    "partnerId": "%s",
                    "itemId": "%s",
                    "retailPrice": %s,
                    "promoPrice": %s,
                    "source": "SELLER",
                    "createdBy": "test-user"
                }
                """.formatted(partnerId, itemId, retailPrice, promoPrice);

        resultActions = mockMvc.perform(post("/api/campaign-partner-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @When("I request campaign partner item with id {string}")
    public void i_request_campaign_partner_item_with_id(String id) throws Exception {
        resultActions = mockMvc.perform(get("/api/campaign-partner-items/" + id));
    }

    @Then("the partner item response status should be {int}")
    public void the_partner_item_response_status_should_be(int status) throws Exception {
        resultActions.andExpect(status().is(status));
    }

    @Then("the partner item response should be an empty list")
    public void the_partner_item_response_should_be_an_empty_list() throws Exception {
        resultActions.andExpect(content().json("[]"));
    }

    @Then("the response should contain partnerId {string}")
    public void the_response_should_contain_partner_id(String partnerId) throws Exception {
        resultActions.andExpect(jsonPath("$.partnerId").value(partnerId));
    }

    @Then("the response should contain itemId {string}")
    public void the_response_should_contain_item_id(String itemId) throws Exception {
        resultActions.andExpect(jsonPath("$.itemId").value(itemId));
    }

    @Then("the response should contain a campaignPartnerItemId")
    public void the_response_should_contain_an_id() throws Exception {
        resultActions.andExpect(jsonPath("$.campaignPartnerItemId").isNotEmpty());
    }

    @Then("the response should contain retailPrice {double}")
    public void the_response_should_contain_retail_price(double price) throws Exception {
        resultActions.andExpect(jsonPath("$.retailPrice").value(closeTo(price, 0.01)));
    }

    @Then("the response should contain promoPrice {double}")
    public void the_response_should_contain_promo_price(double price) throws Exception {
        resultActions.andExpect(jsonPath("$.promoPrice").value(closeTo(price, 0.01)));
    }
}
