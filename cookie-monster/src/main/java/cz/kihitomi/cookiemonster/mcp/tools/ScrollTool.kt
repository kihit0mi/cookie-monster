package cz.kihitomi.cookiemonster.mcp.tools

import cz.kihitomi.cookiemonster.mcp.AgentActionHandler
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Tool, as defined by MCP protocol, with all the required properties.
 * Describes what it does to an agent accessing it, and calls for an OS action, separate from the server.
 */
class ScrollTool(private val actionHandler: AgentActionHandler) {
    private val definition = Tool(
        name = "scroll_in_direction",
        // The description AI reads and decides what tool is it going to use.
        // Changing this will drastically impact agent performance and behavior.
        description = """
            Scrolls the screen by 500 pixels. Valid directions are 'up', 'down', 'left', or 'right'. 
            CRITICAL: After scrolling, the UI changes. You MUST call 'observe_screen' again 
            to get the updated coordinates before trying to click anything.
            """,
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

    /**
     * Mounts the tool onto the active Ktor session.
     */
    fun register(server: Server) {
        server.addTool(definition) { request ->

            val arguments = request.arguments as? Map<String, *>
                ?: throw IllegalArgumentException("Arguments missing.")

            val direction = arguments["direction"]?.toString()?.trim('"')?.lowercase()
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