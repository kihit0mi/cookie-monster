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
    
    YOUR OPERATING LOOP:
    1. OBSERVE: Always use 'observe_screen' first to understand the current UI state.
    2. THINK: Analyze the DOM and Screenshot. Find the exact bounds or determine the next logical step.
    3. ACT: Use a tool (click, type, or scroll).
    4. VERIFY: Call 'observe_screen' again to verify your action worked. 
    
    CRITICAL RULES:
    - You are blind by default. Never assume the state of the screen.
    - If a button has no text in the DOM, use the visual screenshot to find its context, but still use its exact DOM bounds to click it.
    - To type text: FIRST use 'tap_coordinates' on the input field. Wait for the success message. THEN use 'input_text'.
    - DOM First, Vision Second: Always try to extract exact 4-coordinate bounds strings from the JSON DOM. If the DOM is empty or missing 
      the element (e.g., in WebViews like Chrome), you must switch to Vision Mode: analyze the screenshot, calculate the exact (X, Y) pixel 
      coordinates of your target, and send those 2 coordinates to the click tool.
    - Android UIs are hierarchical. If you don't see what you need, use the scroll tool, then observe again.
    
    COMPLETION:
    When you have successfully completed the user's objective, clearly summarize what you did in your final message to the user and stop using tools. 
    If you are stuck after 3 attempts, explain exactly what is blocking you and ask the user for help.
                            """.trimIndent()
                        )
                    )
                )
            )
        }
    }
}