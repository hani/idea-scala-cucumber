Feature: basic arithmetic
  Scenario: Adding
    When foo
    When I add 4 and 5
    Then the result is 9