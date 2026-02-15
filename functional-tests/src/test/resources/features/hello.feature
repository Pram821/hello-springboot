Feature: Seller Center Hello Endpoints
  As a user of the seller center
  I want to access the hello endpoints
  So that I can verify the service is running

  Scenario: Get root hello message
    When I call the seller center root endpoint
    Then the response status should be 200
    And the response body should be "Hello, Spring Boot!"

  Scenario: Get hello2 endpoint
    When I call the seller center "hello2" endpoint
    Then the response status should be 200
    And the response body should be "Hello 2nd"

  Scenario: Get hello3 endpoint
    When I call the seller center "hello3" endpoint
    Then the response status should be 200
    And the response body should be "Hello 3rd"

  Scenario: Get hello4 endpoint
    When I call the seller center "hello4" endpoint
    Then the response status should be 200
    And the response body should be "Hello 4th"

  Scenario: Get hello5 endpoint
    When I call the seller center "hello5" endpoint
    Then the response status should be 200
    And the response body should be "Hello 5th"

  Scenario: Process MX region request
    When I call the region endpoint with region "mx" and request "test-data"
    Then the response status should be 200
    And the response body should be "Processing request for MX region: test-data"

  Scenario: Process US region request
    When I call the region endpoint with region "us" and request "hello"
    Then the response status should be 200
    And the response body should be "Processing request for US region: hello"

  Scenario: Return unknown for invalid region
    When I call the region endpoint with region "invalid" and request "test"
    Then the response status should be 200
    And the response body should be "Unknown region: invalid"
