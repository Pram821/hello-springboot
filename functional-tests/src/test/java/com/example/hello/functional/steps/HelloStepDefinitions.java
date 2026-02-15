package com.example.hello.functional.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HelloStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions resultActions;

    @When("I call the seller center root endpoint")
    public void i_call_the_seller_center_root_endpoint() throws Exception {
        resultActions = mockMvc.perform(get("/seller-center/"));
    }

    @When("I call the seller center {string} endpoint")
    public void i_call_the_seller_center_endpoint(String path) throws Exception {
        resultActions = mockMvc.perform(get("/seller-center/" + path));
    }

    @When("I call the region endpoint with region {string} and request {string}")
    public void i_call_the_region_endpoint(String region, String request) throws Exception {
        resultActions = mockMvc.perform(get("/seller-center/region")
                .param("region", region)
                .param("request", request));
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int status) throws Exception {
        resultActions.andExpect(status().is(status));
    }

    @Then("the response body should be {string}")
    public void the_response_body_should_be(String body) throws Exception {
        resultActions.andExpect(content().string(body));
    }
}
