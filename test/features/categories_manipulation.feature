Feature: manipulating categories

  Scenario: add-kat-pos
    Given I'm logged in as admin
    When I attempt to add a category with name "koty domoweA", slug "koty-domoweA"
    Then the box should appear saying "Category saved!"
    And on the list there should be a category named "koty domoweA"

  Scenario: add-kat-neg-whitespace
    Given I'm logged in as admin
    When I attempt to add a category with name "koty domoweB", slug "koty domoweB"
    Then the box should appear saying "Slug cannot contain spaces"
    And on the list there shouldn't be a category named "koty domoweB"

  Scenario: add-kat-neg-duplicate
    Given I'm logged in as admin
    And there exists a category with name "koty domoweC", slug "koty-domoweC"
    When I attempt to add a category with name "jakieś kitku", slug "koty-domoweC"
    Then the box should appear saying "A category already exists with this slug."

  Scenario: mod-kat-pos
    Given I'm logged in as admin
    And there exists a category with name "koty domoweD", slug "koty-domoweD"
    When I attempt to modify category "koty-domoweC"
    * set name to "kibby"
    And save
    Then the box should appear saying "Category saved!"
    And on the list there shouldn't be a category named "kibby"
