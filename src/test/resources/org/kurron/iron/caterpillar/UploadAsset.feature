Feature: Upload An Asset
  In order to permanently store assets on the network
  REST API users should be able to upload Binary Large Objects (BLOBs) and
  store its meta-data.

  Background:
    Given each request contains an X-Correlation-Id header filled in with a unique value
    And an X-Uploaded-By header filled in with a unique identifier of the entity uploading the asset
    And a Content-Type header filled in with the media-type of the uploaded asset
    And an Accept header filled in with the desired media-type of the returned hypermedia control
    And a Content-Length header filled in with the size, in bytes, of the asset being uploaded
    And a Content-MD5 header filled in with the digest of the asset being uploaded

  @happy
  @required
  Scenario: Successful Upload
    Given an asset to be uploaded
    When a PUT request is made with the asset in the body
    Then a response with a 201 HTTP status code is returned
    And the Location header contains the URI of the uploaded asset
    And the hypermedia control contains the URI of the uploaded asset
    And the hypermedia control contains the meta-data of the uploaded asset

  @sad
  @required
  Scenario: Digest Does Not Match
    Given an asset to be uploaded
    And a Content-MD5 header filled in with a digest of a different asset
    When a PUT request is made with the asset in the body
    Then a response with a 412 HTTP status code is returned
    And the hypermedia control describing the precondition failure is returned

  @sad
  @required
  Scenario: Asset Too Large
    Given an asset that is too large
    When a PUT request is made with the asset in the body
    Then a response with a 413 HTTP status code is returned
    And the hypermedia control describing the size problem is returned

  @happy
  @required
  Scenario: Asset Already Exists
    Given an asset has previously been uploaded
    When a PUT request is made with the previously uploaded asset in the body
    Then a response with a 201 HTTP status code is returned
    And the Location header contains the URI of the uploaded asset
    And the hypermedia control contains the URI of the uploaded asset
    And the hypermedia control contains the meta-data of the uploaded asset

  @sad
  @optional
  Scenario: Unsupported Accept Format
    Given an asset to be uploaded
    And an Accept header containing an unsupported media-type
    When a PUT request is made with the asset in the body
    Then a response with a 406 HTTP status code is returned
    And the hypermedia control describing the formatting problem is returned
