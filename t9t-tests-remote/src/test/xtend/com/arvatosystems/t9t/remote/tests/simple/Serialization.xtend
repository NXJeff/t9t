package com.arvatosystems.t9t.remote.tests.simple

import com.arvatosystems.t9t.base.MessagingUtil
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.request.BatchRequest
import com.arvatosystems.t9t.base.request.LogMessageRequest
import com.arvatosystems.t9t.base.request.PauseRequest
import com.arvatosystems.t9t.core.CannedRequestDTO
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.CompactByteArrayComposer
import de.jpaw.bonaparte.core.CompactByteArrayParser
import de.jpaw.bonaparte.core.StaticMeta
import de.jpaw.util.ByteUtil
import org.junit.Test

@AddLogger
class Serialization {
    @Test
    def public void serializationTest() {
        MessagingUtil.initializeBonaparteParsers

        val batchedTask = new BatchRequest => [
            commands = #[
                new LogMessageRequest("Hello BEFORE pause"),
                new PauseRequest => [
                    delayInMillis   = 4500
                ],
                new LogMessageRequest("Hello AFTER pause")
            ]
        ]

        val compact = CompactByteArrayComposer.marshal(StaticMeta.OUTER_BONAPORTABLE , batchedTask)
        LOGGER.info("Serialized object has {} bytes length", compact.length)
        LOGGER.info("Serialized object is\n{}", ByteUtil.dump(compact, 1000))

        val result = CompactByteArrayParser.unmarshal(compact, StaticMeta.OUTER_BONAPORTABLE, BatchRequest)
    }


    @Test
    def public void serialization2Test() {
        MessagingUtil.initializeBonaparteParsers

        val batchedTask = new BatchRequest => [
            commands = #[
                new LogMessageRequest("Hello BEFORE pause"),
                new PauseRequest => [
                    delayInMillis   = 4500
                ],
                new LogMessageRequest("Hello AFTER pause")
            ]
        ]

        val compact = CompactByteArrayComposer.marshal(CannedRequestDTO.meta$$request, batchedTask)
        LOGGER.info("Serialized object has {} bytes length", compact.length)
        LOGGER.info("Serialized object is\n{}", ByteUtil.dump(compact, 1000))

        val result = CompactByteArrayParser.unmarshal(compact, CannedRequestDTO.meta$$request, RequestParameters)
    }


}