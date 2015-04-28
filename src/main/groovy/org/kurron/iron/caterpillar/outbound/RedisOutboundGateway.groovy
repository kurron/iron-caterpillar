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
package org.kurron.iron.caterpillar.outbound

import static org.kurron.iron.caterpillar.feedback.ExampleFeedbackContext.REDIS_RESOURCE_NOT_FOUND
import static org.kurron.iron.caterpillar.feedback.ExampleFeedbackContext.REDIS_RETRIEVE_INFO
import static org.kurron.iron.caterpillar.feedback.ExampleFeedbackContext.REDIS_STORE_INFO
import org.kurron.feedback.AbstractFeedbackAware
import org.kurron.feedback.exceptions.NotFoundError
import org.kurron.iron.caterpillar.feedback.ExampleFeedbackContext
import org.kurron.stereotype.OutboundGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisOperations

/**
 * Implementation of the outbound gateway that talks to Redis.
 */
@OutboundGateway
class RedisOutboundGateway extends AbstractFeedbackAware implements PersistenceOutboundGateway {

    /**
     * Handles Redis interactions.
     */
    private final RedisOperations<String, String> redisOperations

    /**
     * The key to use when storing and retrieving the resource's content type.
     */
    private static final String CONTENT_TYPE_KEY = 'content-type'

    /**
     * The key to use when storing and retrieving the resource's payload.
     */
    private static final String PAYLOAD_KEY = 'payload'

    /**
     * The key to use when storing and retrieving the resource's uploaded by tag.
     */
    private static final String UPLOADED_BY_KEY = 'uploaded-by'

    /**
     * The key to use when storing and retrieving the resource's size.
     */
    private static final String SIZE_KEY = 'size'

    @Autowired
    RedisOutboundGateway( final RedisOperations<String, String> redisTemplate ) {
        redisOperations = redisTemplate // WARNING: bean name must be 'redisTemplate' or injection fails
    }

    @Override
    String store( final BinaryAsset asset ) {
        feedbackProvider.sendFeedback( REDIS_STORE_INFO, asset.size, asset.contentType, asset.uploadedBy, asset.md5 )

        if ( redisOperations.opsForHash().keys( asset.md5 ) ) {
            feedbackProvider.sendFeedback( ExampleFeedbackContext.DUPLICATE_UPLOAD, asset.md5 )
        }
        else {
            Map<String,String> data = [(CONTENT_TYPE_KEY): asset.contentType,
                                       (UPLOADED_BY_KEY): asset.uploadedBy,
                                       (SIZE_KEY): Integer.toString( asset.size ),
                                       (PAYLOAD_KEY): Base64.encoder.encodeToString( asset.payload ) ]
            redisOperations.opsForHash().putAll( asset.md5, data )
        }
        asset.md5
    }

    @Override
    BinaryAsset retrieve( final String id ) {
        feedbackProvider.sendFeedback( REDIS_RETRIEVE_INFO, id )
        def entries = redisOperations.opsForHash().entries( id )
        if ( !entries ) {
            feedbackProvider.sendFeedback( REDIS_RESOURCE_NOT_FOUND, id )
            throw new NotFoundError( REDIS_RESOURCE_NOT_FOUND, id )
        }
        new BinaryAsset( payload: Base64.decoder.decode( entries[PAYLOAD_KEY] as String ),
                         uploadedBy: entries[UPLOADED_BY_KEY] as String,
                         size: entries[SIZE_KEY] as int,
                         contentType: entries[CONTENT_TYPE_KEY] as String,
                         md5: id )
    }
}
