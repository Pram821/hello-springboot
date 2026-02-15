package com.example.hello.functional.steps;

import io.cucumber.java.en.Given;
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

public class CampaignStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions resultActions;

    @When("I request all campaigns")
    public void i_request_all_campaigns() throws Exception {
        resultActions = mockMvc.perform(get("/api/campaigns"));
    }

    @When("I create a campaign with name {string} type {string} status {string} createdBy {string}")
    public void i_create_a_campaign(String name, String type, String status, String createdBy) throws Exception {
        String json = """
                {
                    "name": "%s",
                    "campaignType": "%s",
                    "campaignStatus": "%s",
                    "createdBy": "%s"
                }
                """.formatted(name, type, status, createdBy);

        resultActions = mockMvc.perform(post("/api/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @When("I request campaign with id {string}")
    public void i_request_campaign_with_id(String id) throws Exception {
        resultActions = mockMvc.perform(get("/api/campaigns/" + id));
    }

    @Then("the campaign response status should be {int}")
    public void the_campaign_response_status_should_be(int status) throws Exception {
        resultActions.andExpect(status().is(status));
    }

    @Then("the response should contain error message {string}")
    public void the_response_should_contain_error_message(String message) throws Exception {
        resultActions.andExpect(jsonPath("$.message").value(message));
    }

    @Then("the response should be an empty list")
    public void the_response_should_be_an_empty_list() throws Exception {
        resultActions.andExpect(content().json("[]"));
    }

    @Then("the response should contain campaign with name {string}")
    public void the_response_should_contain_campaign_with_name(String name) throws Exception {
        resultActions.andExpect(jsonPath("$.campaignName").value(name));
    }

    @Then("the response should contain a campaign primary key")
    public void the_response_should_contain_a_campaign_pk() throws Exception {
        resultActions.andExpect(jsonPath("$.campaignId").isNotEmpty());
    }

    @Then("the response should contain campaignType {string}")
    public void the_response_should_contain_campaign_type(String type) throws Exception {
        resultActions.andExpect(jsonPath("$.campaignType").value(type));
    }

    @Then("the response should contain campaignStatus {string}")
    public void the_response_should_contain_campaign_status(String status) throws Exception {
        resultActions.andExpect(jsonPath("$.campaignStatus").value(status));
    }

    @Then("the campaigns list should have at least {int} entry")
    public void the_campaigns_list_should_have_at_least_entries(int count) throws Exception {
        resultActions.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(count))));
    }
}
