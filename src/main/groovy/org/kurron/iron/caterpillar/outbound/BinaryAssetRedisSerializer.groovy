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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import java.nio.ByteBuffer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.SerializationException

/**
 * Custom serializer that stores the asset in technology agnostic way.
 **/
class BinaryAssetRedisSerializer implements RedisSerializer<BinaryAsset> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    byte[] serialize( final BinaryAsset from ) throws SerializationException {
        new ByteArrayOutputStream( from.payload.size() * 2 ).withStream { ByteArrayOutputStream stream ->
            if ( from  ) {
                try {
                    def jsonBytes = objectMapper.writeValueAsBytes( from )
                    stream.write( ByteBuffer.allocate( Integer.BYTES ).putInt( jsonBytes.length ).array() )
                    stream.write( jsonBytes )
                    stream.write( from.payload )
                }
                catch ( Exception e ) {
                    throw new SerializationException( "Could not write JSON: ${e.getMessage()}" , e )
                }
            }
            stream.toByteArray()
        }
    }

    @Override
    BinaryAsset deserialize( final byte[] from ) throws SerializationException {
        BinaryAsset to = null
        if ( from ) {
            try {
                def buffer = ByteBuffer.wrap( from )
                int length = buffer.getInt()
                def jsonBytes = new byte[length]
                buffer.get( jsonBytes, 0, length )
                to = objectMapper.readValue( jsonBytes, TypeFactory.defaultInstance().constructType( BinaryAsset ) )
                int remaining = buffer.remaining()
                def payloadBytes = new byte[remaining]
                buffer.get( payloadBytes )
                to.payload = payloadBytes
            }
            catch ( Exception e ) {
                throw new SerializationException( "Could not read JSON: ${e.getMessage()}", e )
            }
        }
        to
    }
}
