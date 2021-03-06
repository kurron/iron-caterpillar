/*
 * Copyright (c) 2015. Ronald D. Kurr kurr@jvmguy.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kurron.iron.caterpillar

import com.fasterxml.jackson.databind.ObjectMapper
import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import groovy.util.logging.Slf4j
import org.kurron.iron.caterpillar.feedback.ExampleFeedbackContext
import org.kurron.iron.caterpillar.inbound.CustomHttpHeaders
import org.kurron.iron.caterpillar.inbound.HypermediaControl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.DigestUtils
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder

/**
 * Step definitions geared towards the application's acceptance test but remember, all steps are used
 * by Cucumber unless special care is taken. If you word your features in a consistent manner, then
 * steps will automatically get reused and you won't have to keep writing the same test code.
 **/
@ContextConfiguration( classes = [AcceptanceTestConfiguration], loader = SpringApplicationContextLoader )
@Slf4j
@SuppressWarnings( ['GrMethodMayBeStatic', 'GStringExpressionWithinString', 'MethodCount' ] )
class TestSteps {

    /**
     * The application's configuration settings.
     **/
    @Autowired
    private ApplicationProperties configuration

    @Autowired
    private RestOperations restOperations

    @Autowired
    private ObjectMapper objectMapper

    /**
     * Knows how to determine the service that the application is listening on.
     **/
    @Autowired
    private HttpServiceResolver theServiceResolver

    /**
     * Constant for unknown media type.
     **/
    public static final String ALL = '*'

    /**
     * Default size of uploaded payload.
     **/
    public static final int BUFFER_SIZE = 256

    /**
     * Number of Bytes in a Megabyte.
     */
    @SuppressWarnings( 'DuplicateNumberLiteral' )
    public static final int BYTES_IN_A_MEGABYTE = 1024 * 1024

    /**
     * Generates random data.
     **/
    @SuppressWarnings( 'UnusedPrivateField' )
    @Delegate
    private final Randomizer randomizer = new Randomizer()

    /**
     * This is state shared between steps and can be setup and torn down by the hooks.
     **/
    class MyWorld {
        ResponseEntity<HypermediaControl> uploadEntity
        ResponseEntity<byte[]> downloadEntity
        byte[] bytes = new byte[0]
        def digest = 'not set'
        def headers = new HttpHeaders()
        def mediaType  = new MediaType( ALL, ALL )
        URI uri
        URI location
        HttpStatus statusCode = HttpStatus.I_AM_A_TEAPOT
        def internet = restOperations
        def transformer = objectMapper
    }

    /**
     * Shared between hooks and steps.
     **/
    MyWorld sharedState

    @Before
    void assembleSharedState() {
        log.info( 'Creating shared state' )
        sharedState = new MyWorld()
        sharedState.bytes = randomByteArray( BUFFER_SIZE )
        sharedState.uri = theServiceResolver.resolveURI()
        log.error( 'theServiceResolver is {}', theServiceResolver )
        log.error( 'sharedState.uri is {}', sharedState.uri )
    }

    @After
    void destroySharedState() {
        log.info( 'Destroying shared state' )
        sharedState = null
    }

    @Given( '^each request contains an X-Correlation-Id header filled in with a unique value$' )
    void 'each request contains an X-Correlation-Id header filled in with a unique value'() {
        specifyCorrelationID()
    }

    private specifyCorrelationID( String id = randomHexString() )  {
        sharedState.headers.set( CustomHttpHeaders.X_CORRELATION_ID, id )
    }

    @Given( '^a Content-Type header filled in with the media-type of the uploaded asset$' )
    void 'a Content-Type header filled in with the media-type of the uploaded asset'() {
        sharedState.mediaType = generateMediaType()
        specifyContentType()
    }

    private MediaType generateMediaType() {
        def type = randomHexString()
        def subtype = randomHexString()
        Map<String, String> options = [:]
        options[randomHexString()] = randomHexString()
        new MediaType( type, subtype, options )
    }

    private specifyContentType() {
        sharedState.headers.setContentType( sharedState.mediaType )
    }

    @Given( '^a Content-Length header filled in with the size, in bytes, of the asset being uploaded$' )
    @SuppressWarnings( 'EmptyMethod' )
    void 'a Content-Length header filled in with the size, in bytes, of the asset being uploaded'() {
        // nothing to do because the Spring Template always sets the header with the correct value
    }

    @Given( '^an Accept header filled in with the desired media-type of the returned hypermedia control$' )
    void 'an Accept header filled in with the desired media-type of the returned hypermedia control'() {
        specifyAcceptType()
    }

    @Given( '^an Accept header containing an unsupported media-type$' )
    void 'an Accept header containing an unsupported media-type'() {
        specifyAcceptType( [MediaType.parseMediaType( 'unsupported/unsupported' )] )
    }

    private specifyAcceptType( List<MediaType> acceptable = [MediaType.APPLICATION_JSON] ) {
        sharedState.headers.setAccept( acceptable )
    }

    @Given( '^an X-Uploaded-By header filled in with a unique identifier of the entity uploading the asset$' )
    void 'an X-Uploaded-By header filled in with a unique identifier of the entity uploading the asset'() {
        specifyUploadedBy()
    }

    private specifyUploadedBy()  {
        sharedState.headers.set( CustomHttpHeaders.X_UPLOADED_BY, 'acceptance tester' )
    }

    @Given( '^a Content-MD5 header filled in with the digest of the asset being uploaded$' )
    void 'a Content-MD5 header filled in with the digest of the asset being uploaded'() {
        specifyContentMd5()    }

    private specifyContentMd5()  {
        sharedState.digest = Base64.encoder.encodeToString( DigestUtils.md5Digest( sharedState.bytes ) )
        sharedState.headers.set( CustomHttpHeaders.CONTENT_MD5, sharedState.digest )
    }

    @Given( '^a Content-MD5 header filled in with a digest of a different asset$' )
    void 'a Content-MD5 header filled in with a digest of a different asset'() {
        sharedState.digest = Base64.encoder.encodeToString( DigestUtils.md5Digest( randomByteArray( 8 ) ) )
        sharedState.headers.set( CustomHttpHeaders.CONTENT_MD5, sharedState.digest )
    }

    @Given( '^an asset to be uploaded$' )
    void 'an asset to be uploaded'() {
        sharedState.bytes = sharedState.bytes ?: randomByteArray( BUFFER_SIZE )
    }

    @Given( '^an asset that is too large$' )
    void 'an asset that is too large'() {
        int tooBig = (configuration.maxPayloadSize * BYTES_IN_A_MEGABYTE) + 1
        sharedState.bytes = randomByteArray( tooBig )
    }

    @Given( '^an Accept header filled in with the desired media-type of the bits to be downloaded$' )
    void 'an Accept header filled in with the desired media-type of the bits to be downloaded'() {
        specifyAcceptType( [sharedState.mediaType] )
    }

    @Given( '^an asset has previously been uploaded$' )
    void 'an asset has previously been uploaded'() {
        // reusing steps to upload bytes is too difficult to properly manage so we invoke the steps here
        sharedState.headers = new HttpHeaders() // clear out any prior steps' headers
        specifyCorrelationID()
        sharedState.mediaType = generateMediaType()
        specifyContentType()
        specifyAcceptType()
        specifyContentMd5()
        specifyUploadedBy()
        def requestEntity = new HttpEntity( sharedState.bytes, sharedState.headers )
        sharedState.uploadEntity = restOperations.exchange( sharedState.uri, HttpMethod.PUT, requestEntity, HypermediaControl )
        assert sharedState.uploadEntity.statusCode == HttpStatus.CREATED
        sharedState.headers = new HttpHeaders() // reset for the remaining steps
        log.error( 'sharedState.location = {}', sharedState.location )
    }

    @Given( '^Content-Length header with a value larger than what the server will accept$' )
    void 'Content-Length header with a value larger than what the server will accept'() {
        // Spring is smart and sets the Content-Length based on the uploaded payload
        int tooBig = (configuration.maxPayloadSize * BYTES_IN_A_MEGABYTE) + 1
        sharedState.bytes = randomByteArray( tooBig )
    }

    @Given( '^a URI that does not match anything in the system$' )
    void 'a URI that does not match anything in the system'() {
        def builder = UriComponentsBuilder.newInstance()
        sharedState.location.with {
            builder.scheme( it.scheme ).host( it.host ).port( it.port )
        }
        sharedState.location = builder.path( '/' ).path( randomUUID().toString() ).build().toUri()
    }

    @Given( '^a URI of an expired asset$' )
    void 'a URI of an expired asset'() {
        // we will wait long enough to allow the previously uploaded asset to expire
        Thread.sleep( 1000 * 65 )
    }

    @When( '^a PUT request is made with the asset in the body$' )
    void 'a PUT request is made with the asset in the body'() {
        def requestEntity = new HttpEntity( sharedState.bytes, sharedState.headers )
        sharedState.uploadEntity = restOperations.exchange( sharedState.uri, HttpMethod.PUT, requestEntity, HypermediaControl )
        sharedState.statusCode = sharedState.uploadEntity.statusCode
    }

    @When( '^a PUT request is made with the previously uploaded asset in the body$' )
    void '^a PUT request is made with the previously uploaded asset in the body$'() {
        // reusing steps to upload bytes is too difficult to properly manage so we invoke the steps here
        sharedState.headers = new HttpHeaders() // clear out any prior steps' headers
        specifyCorrelationID()
        specifyContentType()
        specifyAcceptType()
        specifyContentMd5()
        specifyUploadedBy()
        def requestEntity = new HttpEntity( sharedState.bytes, sharedState.headers )
        sharedState.uploadEntity = restOperations.exchange( sharedState.uri, HttpMethod.PUT, requestEntity, HypermediaControl )
        sharedState.statusCode = sharedState.uploadEntity.statusCode
    }

    @When( '^a GET request is made to the URI$' )
    void 'a GET request is made to the URI'() {
        def requestEntity = new HttpEntity( new byte[0], sharedState.headers )
        sharedState.downloadEntity = sharedState.internet.exchange( sharedState.location, HttpMethod.GET, requestEntity, byte[] )
        sharedState.statusCode = sharedState.downloadEntity.statusCode
    }

    @Then( '^a response with a (\\d+) HTTP status code is returned$' )
    void 'a response with an HTTP status code is returned'( int statusCode ) {
        assert statusCode == sharedState.statusCode.value()
    }

    @Then( '^the Content-Type header matches the Accept header$' )
    @SuppressWarnings( 'UnnecessaryGetter' )
    void 'the Content-Type header matches the Accept header'() {
        String acceptType = sharedState.headers.accept.first()
        def types = MediaType.parseMediaTypes( acceptType )
        assert types.any { it.isCompatibleWith( sharedState.downloadEntity.headers.getContentType() ) }
    }

    @Then( '^the body contains the asset$' )
    void 'the body contains the asset'() {
        assert sharedState.downloadEntity.body == sharedState.bytes
    }

    @Then( '^the Location header contains the URI of the uploaded asset$' )
    void 'the Location header contains the URI of the uploaded asset'() {
        assert sharedState.uploadEntity.headers.location
    }

    @Then( '^the hypermedia control contains the URI of the uploaded asset$' )
    void 'the hypermedia control contains the URI of the uploaded asset'() {
        assert sharedState.uploadEntity.body.links.find { it.rel == 'self' }
    }

    @Then( '^the hypermedia control contains the meta-data of the uploaded asset$' )
    void 'the hypermedia control contains the meta-data of the uploaded asset'() {
        assert sharedState.uploadEntity.body.metaDataBlock.mimeType == sharedState.mediaType.toString()
        assert sharedState.uploadEntity.body.metaDataBlock.contentLength == sharedState.bytes.length
    }

    @Then( '^the hypermedia control describing the size problem is returned$' )
    void 'the hypermedia control describing the size problem is returned'() {
        assert sharedState.uploadEntity.body.httpCode == HttpStatus.PAYLOAD_TOO_LARGE.value()
        assert sharedState.uploadEntity.body.errorBlock.code == ExampleFeedbackContext.PAYLOAD_TOO_LARGE.code
        assert sharedState.uploadEntity.body.errorBlock.message
        assert sharedState.uploadEntity.body.errorBlock.developerMessage
    }

    @Then( '^the hypermedia control describing the unknown asset is returned$' )
    @SuppressWarnings( 'UnnecessaryGetter' )
    void 'the hypermedia control describing the unknown asset is returned'() {
        assert sharedState.downloadEntity.headers.getContentType().isCompatibleWith( HypermediaControl.MEDIA_TYPE )
        HypermediaControl control = sharedState.transformer.readValue( sharedState.downloadEntity.body, HypermediaControl )
        assert control.errorBlock.code
        assert control.errorBlock.message
        assert control.errorBlock.developerMessage
    }

    @SuppressWarnings( 'UnnecessaryGetter' )
    @Then( '^the hypermedia control describing the precondition failure is returned$' )
    void 'the hypermedia control describing the precondition failure is returned'() {
        assert sharedState.uploadEntity.headers.getContentType().isCompatibleWith( HypermediaControl.MEDIA_TYPE )
        assert sharedState.uploadEntity.body.errorBlock.code
        assert sharedState.uploadEntity.body.errorBlock.message
        assert sharedState.uploadEntity.body.errorBlock.developerMessage
    }

    @Then( '^the hypermedia control describing the formatting problem is returned$' )
    void 'the hypermedia control describing the formatting problem is returned'() {
        assert sharedState.uploadEntity.headers.getContentType().isCompatibleWith( HypermediaControl.MEDIA_TYPE )
        assert sharedState.uploadEntity.body.errorBlock.code
        assert sharedState.uploadEntity.body.errorBlock.message
        assert sharedState.uploadEntity.body.errorBlock.developerMessage
    }
}
