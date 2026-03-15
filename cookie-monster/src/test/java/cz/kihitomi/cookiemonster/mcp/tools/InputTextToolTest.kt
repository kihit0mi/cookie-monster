package cz.kihitomi.cookiemonster.mcp.tools

import cz.kihitomi.cookiemonster.mcp.AgentActionHandler
import io.mockk.mockk
import io.mockk.verify
import io.modelcontextprotocol.kotlin.sdk.server.Server
import org.junit.Before
import org.junit.Test

class InputTextToolTest {

    private lateinit var actionHandler: AgentActionHandler
    private lateinit var inputTextTool: InputTextTool
    private lateinit var server: Server

    @Before
    fun setUp() {
        actionHandler = mockk()
        server = mockk(relaxed = true)
        inputTextTool = InputTextTool(actionHandler)
    }

    @Test
    fun register_addsToolToServer() {
        inputTextTool.register(server)
        verify(exactly = 1) { server.addTool(any(), any()) }
    }
}
