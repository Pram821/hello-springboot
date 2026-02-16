package com.example.hello.persona.perf;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class SellerCenterSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(System.getProperty("gatling.baseUrl", "http://localhost:8080"))
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private final ScenarioBuilder helloScenario = scenario("Hello Endpoints")
            .exec(
                    http("GET /seller-center/")
                            .get("/seller-center/")
                            .check(status().is(200))
            )
            .pause(1)
            .exec(
                    http("GET /seller-center/hello2")
                            .get("/seller-center/hello2")
                            .check(status().is(200))
            )
            .pause(1)
            .exec(
                    http("GET /seller-center/hello3")
                            .get("/seller-center/hello3")
                            .check(status().is(200))
            );

    private final ScenarioBuilder campaignScenario = scenario("Campaign Endpoints")
            .exec(
                    http("GET /api/campaigns")
                            .get("/api/campaigns")
                            .check(status().in(200, 500))
            )
            .pause(1)
            .exec(
                    http("POST /api/campaigns")
                            .post("/api/campaigns")
                            .body(StringBody("""
                                    {
                                        "name": "Perf Test Campaign",
                                        "campaignType": "FLASH",
                                        "campaignStatus": "DRAFT",
                                        "createdBy": "perf-test"
                                    }
                                    """))
                            .check(status().in(200, 500))
                            .check(jsonPath("$.campaignId").optional().saveAs("campaignId"))
            )
            .pause(1)
            .doIf(session -> session.contains("campaignId")).then(
                    exec(
                            http("GET /api/campaigns/{id}")
                                    .get("/api/campaigns/#{campaignId}")
                                    .check(status().in(200, 500))
                    )
            );

    private final ScenarioBuilder regionScenario = scenario("Region Endpoints")
            .exec(
                    http("GET /seller-center/region?region=us")
                            .get("/seller-center/region")
                            .queryParam("region", "us")
                            .queryParam("request", "perf-test")
                            .check(status().is(200))
            )
            .pause(1)
            .exec(
                    http("GET /seller-center/region?region=mx")
                            .get("/seller-center/region")
                            .queryParam("region", "mx")
                            .queryParam("request", "perf-test")
                            .check(status().is(200))
            );

    {
        setUp(
                helloScenario.injectOpen(
                        rampUsers(10).during(10),
                        constantUsersPerSec(5).during(30)
                ),
                campaignScenario.injectOpen(
                        rampUsers(5).during(10),
                        constantUsersPerSec(2).during(30)
                ),
                regionScenario.injectOpen(
                        rampUsers(5).during(10),
                        constantUsersPerSec(3).during(30)
                )
        ).protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile(95.0).lt(5000),
                        global().successfulRequests().percent().gt(70.0)
                );
    }
}
