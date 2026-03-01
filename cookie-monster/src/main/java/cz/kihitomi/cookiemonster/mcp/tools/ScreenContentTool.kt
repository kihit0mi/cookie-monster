package cz.kihitomi.cookiemonster.mcp.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.serialization.json.*
import cz.kihitomi.cookiemonster.mcp.AgentActionHandler

class ScreenContentTool(private val actionHandler: AgentActionHandler) {
    private val definition = Tool(
        name = "get_screen_content",
        description = "Returns the JSON tree of the current screen.",
        inputSchema = ToolSchema(
            properties = buildJsonObject { }
        )
    )

    fun register(server: Server) {

        server.addTool(definition) { _ ->

            val result = try {
                actionHandler.getScreenContent()
            } catch (e: Exception) {
                e.printStackTrace()
                "Crash Error: ${e.message}"
            }

            CallToolResult(
                content = listOf(TextContent(text = result.toString()))
            )
        }

    }
}