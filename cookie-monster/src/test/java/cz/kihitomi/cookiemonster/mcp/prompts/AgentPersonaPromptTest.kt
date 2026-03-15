package cz.kihitomi.cookiemonster.mcp.prompts

import io.mockk.mockk
import io.mockk.verify
import io.modelcontextprotocol.kotlin.sdk.server.Server
import org.junit.Before
import org.junit.Test

class AgentPersonaPromptTest {

    private lateinit var agentPersonaPrompt: AgentPersonaPrompt
    private lateinit var server: Server

    @Before
    fun setUp() {
        server = mockk(relaxed = true)
        agentPersonaPrompt = AgentPersonaPrompt()
    }

    @Test
    fun register_addsPromptToServer() {
        agentPersonaPrompt.register(server)
        verify(exactly = 1) { server.addPrompt(any(), any()) }
    }
}
