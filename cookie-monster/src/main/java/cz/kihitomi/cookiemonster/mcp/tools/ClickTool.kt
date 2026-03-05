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
class ClickTool(private val actionHandler: AgentActionHandler) {
    private val definition = Tool(
        name = "tap_coordinates",
        // The description AI reads and decides what tool is it going to use.
        // Changing this will drastically impact agent performance and behavior.
        description = """
                Taps a specific element on the screen. Primary Method: Pass the exact 4-number 
                bounds string from the JSON DOM (e.g., '100,200,300,400'). 
                Fallback Method: If the element is missing from the DOM (like in a web browser), 
                look at the screenshot, estimate the X,Y center point of the target, 
                and pass those 2 numbers (e.g., '150,250').
                """,
        inputSchema = ToolSchema(
            required = listOf("bounds"),
            properties = buildJsonObject {
                putJsonObject("bounds") {
                    put("type", "string")
                    put(
                        "description",
                        "The bounding box string (e.g., '140,500,280,600'), or the X,Y estimate from screenshot."
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
                ?: throw IllegalArgumentException("Arguments are missing or malformed")

            val boundsRaw = arguments["bounds"]
                ?: throw IllegalArgumentException("Missing 'bounds' parameter")

            // Removes the JSON artifacts.
            val bounds = boundsRaw.toString().replace("\"", "").trim()

            if (bounds.isBlank() || bounds == "null") {
                throw IllegalArgumentException("Missing 'bounds' parameter")
            }

            val result = try {
                actionHandler.clickByBounds(bounds)
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