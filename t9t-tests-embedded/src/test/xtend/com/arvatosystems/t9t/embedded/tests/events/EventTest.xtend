package com.arvatosystems.t9t.embedded.tests.events

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.event.GenericEvent
import com.arvatosystems.t9t.base.request.ProcessEventRequest
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import org.junit.BeforeClass
import org.junit.Test

class EventTest {

    static private ITestConnection dlg

    @BeforeClass
    def public static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def public void performEventLogTest() {
        val myEvent   = new GenericEvent => [
            eventID   = "testEvent"
            z         = #{ "hello" -> "world", "pi" -> 3.14 }
        ]
        dlg.okIO(new ProcessEventRequest => [
            eventHandlerQualifier = "logger"
            eventData             = myEvent
        ])
    }
}