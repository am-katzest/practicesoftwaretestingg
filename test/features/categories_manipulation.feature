Feature: manipulating categories

  Scenario: add-kat-pos
    Given I'm logged in as admin
    When I attempt to add a category with name "koty domowe-a", slug "koty-domowe-a"
    Then the box should appear saying "Category saved!"
    And on the list there should be a category named "koty domowe-a"

  Scenario: add-kat-neg-whitespace
    Given I'm logged in as admin
    When I attempt to add a category with name "koty domowe-b", slug "koty domowe-b"
    Then the box should appear saying "Slug cannot contain spaces"
    And on the list there shouldn't be a category named "koty domowe-b"

  Scenario: add-kat-neg-duplicate
    Given I'm logged in as admin
    And there exists a category with name "koty domowe-c", slug "koty-domowe-c"
    When I attempt to add a category with name "jakieś kitku", slug "koty-domowe-c"
    Then the box should appear saying "A category already exists with this slug."

  Scenario: mod-kat-pos
    Given I'm logged in as admin
    And there exists a category with name "koty domowe-d", slug "koty-domowe-d"
    When I attempt to modify category "koty-domowe-d"
    And set name to "kibby"
    And save
    Then the box should appear saying "Category saved!"
    And on the list there should be a category named "kibby"

  Scenario: mod-kat-neg
    Given I'm logged in as admin
    And there exists a category with name "koty domowe-e", slug "koty-domowe-e"
    When I attempt to modify category "koty-domowe-e"
    And set slug to "meow meow"
    And save
    Then the box should appear saying "Slug cannot contain spaces"
    And on the list there shouldn't be a category named "meow meow"
