package cz.kihitomi.cookiemonster.logger

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LogManagerTest {

    @Before
    fun setup() {
        LogManager.clearLogs()
    }

    @Test
    fun addLog_addsNewItemSuccessfully() {
        val testTag = "TEST"
        val testMsg = "Hello World"

        LogManager.addLog(testTag, testMsg)

        val logs = LogManager.getAllLogs()
        assertEquals(1, logs.size)
        assertEquals(testTag, logs[0].tag)
        assertEquals(testMsg, logs[0].message)
    }

    @Test
    fun addLog_respectsMaxCapacity() {
        for (i in 1..1005) {
            LogManager.addLog("TEST", "Message $i")
        }

        val logs = LogManager.getAllLogs()
        assertEquals(1000, logs.size)
    }
}
