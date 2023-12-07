Feature: manipulating categories
  Scenario: add-kat-pos
    Given I'm logged in as admin
    When I attempt to add a category with name "koty domoweA", slug "koty-domoweA" and parent "Other"
    Then the box should appear saying "Category saved!"
    And on the list there should be a category named "koty domoweA"
