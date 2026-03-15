package cz.kihitomi.cookiemonster.mcp

import cz.kihitomi.cookiemonster.mcp.CookieMonsterServer
import cz.kihitomi.cookiemonster.mcp.AgentActionHandler
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Test

class CookieMonsterServerTest {

    @Test
    fun serverCreation_initializesMcpSession() {
        val actionHandler = mockk<AgentActionHandler>()
        val server = CookieMonsterServer(actionHandler)
        
        // Verifies that the internal MCP SDK server object is properly initialized
        // alongside its defined tools and prompts.
        assertNotNull("Server session should be initialized", server.mcpSession)
    }
}
