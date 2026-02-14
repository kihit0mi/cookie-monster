package cz.kihitomi.cookiemonster

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class CookieMonsterMcpServer(private val accessibilityService: MyAccessibilityService) {

    val server = Server(
        serverInfo = Implementation(
            name = "CookieMonsterAgent",
            version = "1.0.0"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
                resources = ServerCapabilities.Resources(listChanged = false, subscribe = false)
            )
        )
    )

    init {

        val screenTool = Tool(
            name = "get_screen_content",
            description = "Returns the JSON tree of the current screen.",
            inputSchema = ToolSchema(
                properties = buildJsonObject { }
            )
        )

        server.addTool(screenTool) { _ ->
            val content = accessibilityService.getScreenContent()
            CallToolResult(
                content = listOf(TextContent(text = content))
            )
        }

        val clickTool = Tool(
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

        server.addTool(clickTool) { args ->
            val arguments = args as Map<String, *>
            val boundsRaw = arguments["bounds"]
            val bounds = boundsRaw.toString().replace("\"", "").trim()

            if (bounds.isBlank() || bounds == "null") {
                throw IllegalArgumentException("Missing 'bounds' parameter")
            }

            val result = accessibilityService.clickByBounds(bounds)

            CallToolResult(
                content = listOf(TextContent(text = result))
            )
        }
    }
}