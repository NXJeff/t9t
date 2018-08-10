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
package com.arvatosystems.t9t.out.services;

import de.jpaw.bonaparte.core.BonaPortable;

/**
 * Defines methods for an async queue implementation.
 * This interface is an intermediate step in the data flow, it directs to a selected implementation of the internal queuing (JMS, LTQ or noop).
 *
 * Business logic usually does not see this interface.
 *  */
public interface IAsyncQueue {
    /** Queues a messages. Returns null, or a QueueRef. */
    Long sendAsync(String asyncChannelId, BonaPortable payload, Long objectRef);
    default void open() {};
    default void close() {};
    default void clearQueue(Long queueRef) {};  // removes any items from the queue, required after removing dead items
}
