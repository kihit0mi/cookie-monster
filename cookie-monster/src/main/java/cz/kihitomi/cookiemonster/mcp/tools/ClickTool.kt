package cz.kihitomi.cookiemonster.mcp.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.serialization.json.*
import cz.kihitomi.cookiemonster.mcp.AgentActionHandler

class ClickTool(private val actionHandler: AgentActionHandler) {
    private val definition = Tool(
        name = "tap_coordinates",
        description = "Taps on the screen at the specified bounding box center.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("bounds") {
                    put("type", "string")
                    put("description", "The bounding box string (e.g., '140,500,280,600')")
                }
            }
        )
    )

    fun register(server: Server) {
        server.addTool(definition) { request ->
            val arguments = request.arguments as? Map<String, *>
                ?: throw IllegalArgumentException("Arguments are missing or malformed")

            val boundsRaw = arguments["bounds"]
                ?: throw IllegalArgumentException("Missing 'bounds' parameter")

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