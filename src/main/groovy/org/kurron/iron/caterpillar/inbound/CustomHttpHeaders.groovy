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
package org.kurron.iron.caterpillar.inbound

/**
 * Contains custom HTTP header names.
 */
class CustomHttpHeaders {

    /**
     * Private constructor, to prevent instantiation.
     */
    private CustomHttpHeaders() { }

    /**
     * The correlation id (a.k.a. work-unit) header, useful in stitching together work being done by the server.
     */
    static final String X_CORRELATION_ID = 'X-Correlation-Id'

    /**
     * The number of minutes to wait before expiring a given resource.
     */
    static final String X_EXPIRATION_MINUTES = 'X-Expiration-Minutes'

    /**
     * The identifier of the entity uploading the BLOB.
     */
    static final String X_UPLOADED_BY = 'X-Uploaded-By'

    /**
     * This is a standard header but Spring doesn't seem to have a constant for it.
     */
    static final String CONTENT_MD5 = 'Content-MD5'
}
