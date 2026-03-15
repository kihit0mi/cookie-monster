package cz.kihitomi.cookiemonster.mcp.tools

import cz.kihitomi.cookiemonster.mcp.AgentActionHandler
import io.mockk.mockk
import io.mockk.verify
import io.modelcontextprotocol.kotlin.sdk.server.Server
import org.junit.Before
import org.junit.Test

class ObserveScreenToolTest {

    private lateinit var actionHandler: AgentActionHandler
    private lateinit var observeScreenTool: ObserveScreenTool
    private lateinit var server: Server

    @Before
    fun setUp() {
        actionHandler = mockk()
        server = mockk(relaxed = true)
        observeScreenTool = ObserveScreenTool(actionHandler)
    }

    @Test
    fun register_addsToolToServer() {
        observeScreenTool.register(server)
        verify(exactly = 1) { server.addTool(any(), any()) }
    }
}
