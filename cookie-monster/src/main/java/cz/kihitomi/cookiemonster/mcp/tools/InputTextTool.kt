package cz.kihitomi.cookiemonster.mcp.tools

import cz.kihitomi.cookiemonster.mcp.AgentActionHandler
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class InputTextTool(private val actionHandler: AgentActionHandler) {
    private val definition = Tool(
        name = "input_text",
        description = "Types text into the currently focused input field. You MUST use the click tool on the text field before calling this. The enter parameter (boolean) determines if the physical 'Enter/Return' key is pressed after typing. Set enter to true when submitting a search or sending a message.",
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