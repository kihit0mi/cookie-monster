package cz.kihitomi.cookiemonster.mcp.tools

import cz.kihitomi.cookiemonster.mcp.AgentActionHandler
import io.mockk.mockk
import io.mockk.verify
import io.modelcontextprotocol.kotlin.sdk.server.Server
import org.junit.Before
import org.junit.Test

class ScrollToolTest {

    private lateinit var actionHandler: AgentActionHandler
    private lateinit var scrollTool: ScrollTool
    private lateinit var server: Server

    @Before
    fun setUp() {
        actionHandler = mockk()
        server = mockk(relaxed = true)
        scrollTool = ScrollTool(actionHandler)
    }

    @Test
    fun register_addsToolToServer() {
        scrollTool.register(server)
        verify(exactly = 1) { server.addTool(any(), any()) }
    }
}
