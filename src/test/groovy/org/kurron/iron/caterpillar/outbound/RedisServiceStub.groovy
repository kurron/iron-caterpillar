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

import org.kurron.feedback.AbstractFeedbackAware
import org.kurron.stereotype.ServiceStub

/**
 * A Service Stub of the PersistenceOutboundGateway useful in testing.
 */
@ServiceStub
class RedisServiceStub extends AbstractFeedbackAware implements PersistenceOutboundGateway {

    /**
     * A fake version of redis.
     **/
    private final Map<String,BinaryAsset> redis = [:]

    @Override
    InsertionResult store( final BinaryAsset resource ) {
        redis[resource.md5] = resource
        new InsertionResult( id: resource.md5, inserted: true )
    }

    @Override
    BinaryAsset retrieve( final String id ) {
        redis[id]
    }
}
