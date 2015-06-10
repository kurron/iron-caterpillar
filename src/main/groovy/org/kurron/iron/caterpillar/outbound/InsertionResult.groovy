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

import groovy.transform.Immutable

/**
 * Represents the result of inserting an asset into the system.
 **/
@Immutable
class InsertionResult {

    /**
     * The key to use when looking pu the asset.
     **/
    String id

    /**
     * If true, the bytes were actually uploaded, otherwise the bytes already existed in the system.
     **/
    boolean inserted
}
