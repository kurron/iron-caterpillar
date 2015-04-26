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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.Canonical

/**
 * Represents a resource that is saved to Redis as a hash data type.
 */
@Canonical
class BinaryAsset
{

    /**
     * The content type associated with the resource.
     */
    @JsonProperty( 'content-type' )
    String contentType

    /**
     * The entity that uploaded the asset.
     */
    @JsonProperty( 'uploaded-by' )
    String uploadedBy

    /**
     * The number of bytes the payload is.
     **/
    @JsonProperty( 'size' )
    int size

    /**
     * The hex encoding the of 128 bit MD5 digest of the asset.
     */
    @JsonProperty( 'md5' )
    String md5

    /**
     * The resource bytes.
     */
    @JsonIgnore
    byte[] payload
}
