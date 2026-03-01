package cz.kihitomi.cookiemonster.mcp.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.serialization.json.*
import cz.kihitomi.cookiemonster.mcp.AgentActionHandler

class ScrollTool(private val actionHandler: AgentActionHandler) {
    private val definition = Tool(
        name = "scroll_in_direction",
        description = "Scrolls the screen by 500 pixels. Valid directions are 'up', 'down', 'left', or 'right'. CRITICAL: After scrolling, the UI changes. You MUST call 'observe_screen' again to get the updated coordinates before trying to click anything.",
        inputSchema = ToolSchema(
            required = listOf("direction"),
            properties = buildJsonObject {
                putJsonObject("direction") {
                    put("type", "string")
                    put(
                        "description",
                        "The direction where you want to scroll - 'up', 'down', 'left' or 'right'"
                    )
                }
            }
        )
    )

    fun register(server: Server) {
        server.addTool(definition) { request ->

            val arguments = request.arguments as? Map<String, *>
                ?: throw IllegalArgumentException("Arguments missing.")

            val direction = arguments["direction"]?.toString()?.trim('"')
                ?: throw IllegalArgumentException("Missing 'direction' parameter.")

            val result = try {
                actionHandler.scroll(direction)
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