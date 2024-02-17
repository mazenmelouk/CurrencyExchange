Feature: Currency Exchange

  Background: Currency exchange uses data from external API to fetch rates and answer exchange queries
    Given the exchange rates for USD are
      | currency | rate |
      | EUR      | 0.9  |
      | JPY      | 150  |

  Scenario Outline: Given a valid currency exchange query, result is successful
    When I request to exchange 100 USD to <target>
    Then the request succeeds with status 200
    And response is <expected> <target>

    Examples:
      | target | expected |
      | EUR    | 90       |
      | JPY    | 15000    |

  Scenario: Given a query to convert to an unknown currency, result is not found
    When I request to exchange 100 USD to NONE
    Then the request succeeds with status 404

  Scenario: Given a query to convert from an unknown currency, result is not found
    When I request to exchange 100 NONE to EUR
    # Todo fix it should return 400 not 500, we need to add a handler in the client.
    Then the request succeeds with status 500

    #Todo fix https://github.com/mazenmelouk/CurrencyExchange/issues/10
  Scenario: Given the external rates change, the old rates are still used
  We first do a request at old rates, then we change the rates.
  We do another request and its results are still using the old rates
    When I request to exchange 100 USD to EUR
    Then the request succeeds with status 200
    And response is 90 EUR
    Given the exchange rates for USD are
      | currency | rate |
      | EUR      | 0.85 |
    When I request to exchange 100 USD to EUR
    Then the request succeeds with status 200
    And response is 90 EUR