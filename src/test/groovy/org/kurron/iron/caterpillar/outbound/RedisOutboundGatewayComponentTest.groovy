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

import org.kurron.iron.caterpillar.BaseOutboundIntegrationTest
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration-level testing of the RedisOutboundGateway object.
 */
class RedisOutboundGatewayComponentTest extends BaseOutboundIntegrationTest {

    @Autowired
    PersistenceOutboundGateway sut

    def 'exercise storage and retrieval'() {

        given: 'a valid outbound gateway'
        assert sut

        and: 'an asset to save'
        def asset = new BinaryAssetBuilder().build()

        when: 'the asset is saved to redis'
        def id = sut.store( asset )

        then: 'the asset can be retrieved immediately'
        def result = sut.retrieve( id )

        and: 'the expected bytes are returned'
        result
        result == asset
    }
}
