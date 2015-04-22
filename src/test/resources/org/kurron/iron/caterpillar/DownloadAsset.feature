Feature: Download An Asset
  In order to use assets stored on the network
  REST API users should be able to download their previously stored assets
  and access its meta-data

  Background:
    Given an asset has previously been uploaded
    And each request contains an X-Correlation-Id header filled in with a unique value

  @happy
  Scenario: Successful Download
    Given an Accept header filled in with the media-type of the asset and the media-type of the hypermedia control
    And a GET request is made to the URI
    Then a response with a 200 HTTP status code is returned
    And the Content-Type header matches the Accept header
    And the body contains the asset

  @happy
  Scenario: Meta-data Download
    Given an Accept header filled in with the media-type of the hypermedia control
    When a GET request is made to the URI
    Then a response with a 200 HTTP status code is returned
    And the hypermedia control describing the asset is returned

  @sad
  Scenario: Unknown URI
    Given an Accept header filled in with the media-type of the asset and the media-type of the hypermedia control
    And a URI that does not match anything in the system
    When a GET request is made to the URI
    Then a response with a 404 HTTP status code is returned
    And the hypermedia control describing the unknown asset is returned

