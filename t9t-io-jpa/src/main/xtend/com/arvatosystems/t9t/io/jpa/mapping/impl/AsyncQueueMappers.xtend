/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.io.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.io.AsyncQueueDTO
import com.arvatosystems.t9t.io.jpa.entities.AsyncQueueEntity
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver

@AutoMap42
class AsyncQueueMappers {
    IAsyncQueueEntityResolver resolver

    @AutoHandler("SC42")
    def void e2dAsyncQueueDTO(AsyncQueueEntity entity, AsyncQueueDTO dto) {}
    def void d2eAsyncQueueDTO(AsyncQueueEntity entity, AsyncQueueDTO dto, boolean onlyActive) {}
}
