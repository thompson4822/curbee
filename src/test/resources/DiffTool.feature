Feature: DiffTool
  as a system
  I have a tool that can compare two object states of arbitrary complexity
  so that I can see the differences between them

  Background: Set up the object states
    Given I have the following object states
    | name                    | state |
    | simple                  | { "a": 1, "b": {"d": 1, "e": 1}, "c": [] } |
    | multipleChanges         | { "a": 1, "b": {"d": 1, "e": 2}, "c": [1] } |
    | simple2                 | { "a": 5, "b": {"d": 1, "e": 1}, "c": [] } |
    | nestedChange1           | { "a": 1, "b": {"d": 1, "e": 2}, "c": [] } |
    | simpleList1             | { "a": 1, "b": {"d": 1, "e": 1}, "c": [1] } |
    | simpleListAdded         | { "a": 1, "b": {"d": 1, "e": 1}, "c": [1, 2] } |
    | simpleListAddedRemoved  | { "a": 1, "b": {"d": 1, "e": 1}, "c": [2, 3] } |
    | complexList1            | { "a": 1, "b": {"d": 1, "e": 1}, "c": [{"id": "q", "x": 1}, {"id": "r", "x": 1}] } } |
    | complexList2            | { "a": 1, "b": {"d": 1, "e": 1}, "c": [{"id": "q", "x": 2}, {"id": "r", "x": 1}] } }  |
    | complexListInvalid1     | { "a": 1, "b": {"d": 1, "e": 1}, "c": [{"y": "?", "z": "!"}, {"id": "r", "x": 1}] } } |
    | complexListInvalid2     | { "a": 1, "b": {"d": 1, "e": 1}, "c": [{"id": "q", "x": 2}, {"y": "?", "z": "!"}] } }  |
    | complexList3            | { "a": 1, "b": {"d": 1, "e": 1}, "c": [{"id": "q", "x": 2}, {"id": "r", "x": 1}, {"id": "s", "x": 1}] } } |
    | auditList1              | { "a": 1, "b": {"d": 1, "e": 1}, "c": [{"aid": "q", "x": 2}] } |
    | auditList2              | { "a": 1, "b": {"d": 1, "e": 1}, "c": [{"aid": "q", "x": 3}, {"aid": "r", "x": 1}] } |

    #@ignore
    Scenario Outline: If the previous state is not provided, an exception should be shown
      Given I have a <previous> state and a <current> state
      When I compare the two
      Then I should see an exception
      Examples:
      | previous | current  |
      | null     | simple   |
      | simple   | null     |

    #@ignore
    Scenario Outline: Comparing two simple object states should return the expected differences
      Given I have a <previous> state and a <current> state
      When I compare the two
      Then I should see the expected <differences>
      And I should not see an exception
      Examples:
      | previous | current      | differences |
      | simple   | simple       | []          |
      | simple   | simple2      | [{"property": "a", "previous": 1, "current": 5}] |
      | simple   | simpleList1  | [{"property": "c", "added": [1], "removed": []}] |

    #@ignore
    Scenario Outline: Comparing two simple object states that have multiple differences should return the expected results
      Given I have a <previous> state and a <current> state
      When I compare the two
      Then I should see the expected <differences>
      And I should not see an exception
      Examples:
      | previous        | current           | differences |
      | simple          | multipleChanges   | [{"property": "b.e", "previous": 1, "current": 2}, {"property": "c", "added": [1], "removed": []}] |

    #@ignore
    Scenario Outline: Comparing two states with nested objects should return the expected differences
      Given I have a <previous> state and a <current> state
      When I compare the two
      Then I should see the expected <differences>
      And I should not see an exception
      Examples:
      | previous      | current       | differences |
      | nestedChange1 | nestedChange1 | [] |
      | simple        | nestedChange1 | [{"property": "b.e", "previous": 1, "current": 2}] |

    #@ignore
    Scenario Outline: Comparing two states with different lists should return the expected differences
      Given I have a <previous> state and a <current> state
      When I compare the two
      Then I should see the expected <differences>
      And I should not see an exception
      Examples:
      | previous              | current              | differences |
      | simpleList1           | simpleList1          | [] |
      | simpleList1           | simpleListAddedRemoved | [{"property": "c", "added": [2, 3], "removed": [1]}] |
      | simpleList1           | simpleListAdded      | [{"property": "c", "added": [2], "removed": []}] |
      | complexList1          | complexList2         | [{"property": "c[q].x", "previous": 1, "current": 2}] |
      | complexList2          | complexList3         | [{"property": "c", "added": [{"id": "s", "x": 1}], "removed": []}] |
      | complexList1          | complexList3         | [{"property": "c", "added": [{"id": "s", "x": 1}], "removed": []}, {"property": "c[q].x", "previous": 1, "current": 2}] |
      | complexList3          | complexList1         | [{"property": "c", "added": [], "removed": [{"id": "s", "x": 1}]}, {"property": "c[q].x", "previous": 2, "current": 1}] |
      | complexList3          | complexList2         | [{"property": "c", "added": [], "removed": [{"id": "s", "x": 1}]}] |


    Scenario Outline: Comparing two states where the list contains an audit object should return the expected differences
      Given I have a <previous> state and a <current> state
      When I compare the two
      Then I should see the expected <differences>
      And I should not see an exception
      Examples:
      | previous              | current              | differences |
      | auditList1            | auditList2           | [{"property": "c", "added": [{"aid": "r", "x": 1}], "removed": []}, {"property": "c[q].x", "previous": 2, "current": 3}] |


    Scenario Outline: Comparing two states with invalid lists should return the expected differences
      Given I have a <previous> state and a <current> state
      When I compare the two
      Then I should see an exception
      Examples:
      | previous              | current              |
      | simpleList1           | complexListInvalid1  |
      | complexListInvalid1   | complexListInvalid2  |
      | complexListInvalid2   | complexListInvalid1  |

    #@ignore
    Scenario Outline: Figure out pathname when dealing with lists
      Given I have a <previous> state and a <current> state
      When I compare the two
      Then I should see the expected <differences>
      And I should not see an exception
      Examples:
      | previous              | current              | differences |
      | complexList1          | complexList2         | [{"property": "c[q].x", "previous": 1, "current": 2}] |
