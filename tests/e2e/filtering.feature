Feature: Filter (Blacklist) Values


  Scenario: Filtering values
    Given we clear the conditions for "111,222,333" in "filter-condition"
    Given we listen on the following kafka topic "filtered-readings"
    And we publish the following messages to "filter-condition"
      | key                 | value                   |
      | {"reading_id": 111} | {"is_sensitive": true}  |
      | {"reading_id": 333} | {"is_sensitive": false} |
    And we wait 2 seconds for the config to propagate
    When we publish the following messages to "readings"
      | key                 | value                         |
      | {"reading_id": 333} | {"value": 11, "timestamp": 1} |
      | {"reading_id": 222} | {"value": 12, "timestamp": 2} |
      # next rows should be skipped
      | {"reading_id": 111} | {"value": 13, "timestamp": 3} |
      | {"reading_id": 111} | {"value": 14, "timestamp": 4} |
    Then we expect to find the following messages in "filtered-readings"
      | key                 | value                         |
      | {"reading_id": 333} | {"value": 11, "timestamp": 1} |
      | {"reading_id": 222} | {"value": 12, "timestamp": 2} |
    Given we publish the following messages to "filter-condition"
      | key                 | value                   |
      | {"reading_id": 111} | {"is_sensitive": false} |
      | {"reading_id": 222} | {"is_sensitive": true}  |
    And we wait 2 seconds for the config to propagate
    When we publish the following messages to "readings"
      | key                 | value                         |
      | {"reading_id": 111} | {"value": 15, "timestamp": 5} |
      # next rows should be skipped
      | {"reading_id": 222} | {"value": 16, "timestamp": 6} |
    Then we expect to find the following messages in "filtered-readings"
      | key                 | value                         |
      | {"reading_id": 111} | {"value": 15, "timestamp": 5} |
