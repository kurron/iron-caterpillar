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

import org.kurron.iron.caterpillar.BaseUnitTest

/**
 * Unit-level test of the BinaryAssetRedisSerializer class.
 **/
class BinaryAssetRedisSerializerUnitTest extends BaseUnitTest {

    def sut = new BinaryAssetRedisSerializer()

    def 'exercise serialization'() {

        given: 'asset to serialize'
        def asset = new BinaryAssetBuilder().build()

        when: 'the asset is serialized'
        def bytes = sut.serialize( asset )

        and: 'then deserialized'
        def rehydrated = sut.deserialize( bytes )

        then: 'the two instances match'
        asset == rehydrated
    }
}
