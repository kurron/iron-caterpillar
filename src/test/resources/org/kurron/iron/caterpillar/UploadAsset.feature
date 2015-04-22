Feature: Upload An Asset
  In order to permanently store assets on the network
  REST API users should be able to upload Binary Large Objects (BLOBs) and
  store meta-data about the BLOB.

  Background:
    Given each request contains an X-Correlation-Id header filled in with a unique value
    And an X-Uploaded-By header filled in with a unique identifier of the entity uploading the asset
    And a Content-Type header filled in with the media-type of the uploaded asset
    And a Content-Length header filled in with the size, in bytes, of the asset being uploaded
    And a Content-MD5 header filled in with the MD5 digest of the asset being uploaded
    And an Accept header filled in with the desired media-type of the returned hypermedia control

  @happy
  Scenario: Successful Upload
    Given an asset to be uploaded
    When a POST request is made with the asset in the body
    Then a response with a 201 HTTP status code is returned
    And the Location header contains the URI of the uploaded asset
    And the ETag header contains the current value of the asset
    And the hypermedia control contains the URI of the uploaded asset
    And the hypermedia control contains the meta-data of the uploaded asset

  @sad
  Scenario: Asset Too Large
    Given an asset that is too large
    When a POST request is made with the asset in the body
    Then a response with a 413 HTTP status code is returned
    And the hypermedia control describing the size problem is returned

  @sad
  Scenario: Asset Already Exists
    Given an asset has previously been uploaded
    When a POST request is made with the previously uploaded asset in the body
    Then a response with a 409 HTTP status code is returned
    And the hypermedia control describing the conflict is returned

