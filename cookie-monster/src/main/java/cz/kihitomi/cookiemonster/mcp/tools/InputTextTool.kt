package cz.kihitomi.cookiemonster.mcp.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.serialization.json.*
import cz.kihitomi.cookiemonster.mcp.AgentActionHandler

class InputTextTool(private val actionHandler: AgentActionHandler) {
    private val definition = Tool(
        name = "input_text",
        description = "Enters text into currently focused input field. You must click on the input field first, using tap_coordinates. You have also OPTIONAL power to press enter after typing in the text (use when typing in a search field for example).",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("text", buildJsonObject {
                    put("type", "string")
                    put("description", "The text to enter")
                })
                put("enter", buildJsonObject {
                    put("type", "boolean")
                    put("description", "Whether to press enter after typing in the text")
                })
            },
            required = listOf("text")
        )
    )

    fun register(server: Server) {
        server.addTool(definition) { request ->
            val arguments = request.arguments as? Map<String, *>
                ?: throw IllegalArgumentException("Arguments missing.")

            val text = arguments["text"]?.toString()?.trim('"')
                ?: throw IllegalArgumentException("Missing 'text' parameter.")

            val enter = arguments["enter"]?.toString()?.toBoolean() ?: false

            val result = try {
                actionHandler.typeText(text, enter)
            } catch (e: Exception) {
                e.printStackTrace()
                "Crash Error: ${e.message}"
            }

            CallToolResult(
                content = listOf(TextContent(text = result))
            )
        }
    }
}