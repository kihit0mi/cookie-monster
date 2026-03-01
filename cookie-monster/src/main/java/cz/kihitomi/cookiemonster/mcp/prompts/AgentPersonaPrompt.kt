package cz.kihitomi.cookiemonster.mcp.prompts

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.GetPromptResult
import io.modelcontextprotocol.kotlin.sdk.types.Prompt
import io.modelcontextprotocol.kotlin.sdk.types.PromptMessage
import io.modelcontextprotocol.kotlin.sdk.types.Role
import io.modelcontextprotocol.kotlin.sdk.types.TextContent

class AgentPersonaPrompt {

    private val definition = Prompt(
        name = "android_agent_persona",
        description = "Standard operating procedures and system instructions for the Android autonomous agent.",
        arguments = emptyList()
    )

    fun register(server: Server) {
        server.addPrompt(definition) { _ ->
            GetPromptResult(
                description = "Android Agent Persona",
                messages = listOf(
                    PromptMessage(
                        role = Role.User,
                        content = TextContent(
                            text = """
                                You are an autonomous Android agent running directly on a device via the Cookie Monster MCP server. 
                                Your goal is to navigate the Android OS and interact with apps to complete the user's requests.
                                
                                CRITICAL RULES FOR USING YOUR TOOLS:
                                1. You are blind by default. ALWAYS use 'observe_screen' first to understand the current UI state before taking any action.
                                2. To type text, you must FIRST click the input field using 'tap_coordinates'. Only then can you use 'input_text'.
                                3. Do not guess coordinates. Always extract exact bounding boxes from the DOM.
                                4. Android UIs are hierarchical. If you can't find something, try scrolling.
                            """.trimIndent()
                        )
                    )
                )
            )
        }
    }
}