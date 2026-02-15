Feature: Campaign Partner Item Management
  As a partner manager
  I want to create and retrieve campaign partner items
  So that I can manage partner item submissions for campaigns

  Scenario: List campaign partner items when none exist
    When I request all campaign partner items
    Then the partner item response status should be 200
    And the partner item response should be an empty list

  Scenario: Create a new campaign partner item
    When I create a campaign partner item with partnerId "PARTNER001" itemId "ITEM001" retailPrice 29.99 promoPrice 19.99
    Then the partner item response status should be 200
    And the response should contain a campaignPartnerItemId
    And the response should contain partnerId "PARTNER001"
    And the response should contain itemId "ITEM001"
    And the response should contain retailPrice 29.99
    And the response should contain promoPrice 19.99

  Scenario: Get non-existent partner item returns 404
    When I request campaign partner item with id "00000000-0000-0000-0000-000000000000"
    Then the partner item response status should be 404
