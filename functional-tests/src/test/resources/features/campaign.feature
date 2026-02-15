Feature: Campaign Management
  As a campaign manager
  I want to create and retrieve campaigns
  So that I can manage flash pick events

  Scenario: List campaigns when none exist
    When I request all campaigns
    Then the campaign response status should be 200
    And the response should be an empty list

  Scenario: Create a new campaign
    When I create a campaign with name "Summer Flash Sale" type "FLASH" status "DRAFT" createdBy "admin"
    Then the campaign response status should be 200
    And the response should contain a campaign primary key
    And the response should contain campaign with name "Summer Flash Sale"
    And the response should contain campaignType "FLASH"
    And the response should contain campaignStatus "DRAFT"

  Scenario: List campaigns after creation
    When I create a campaign with name "Winter Sale" type "SEASONAL" status "ACTIVE" createdBy "manager"
    Then the campaign response status should be 200
    When I request all campaigns
    Then the campaign response status should be 200
    And the campaigns list should have at least 1 entry

  Scenario: Get non-existent campaign returns 404
    When I request campaign with id "00000000-0000-0000-0000-000000000000"
    Then the campaign response status should be 404
