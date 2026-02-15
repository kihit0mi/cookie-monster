package cz.kihitomi.cookiemonster

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import io.modelcontextprotocol.kotlin.sdk.types.ImageContent

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject


class CookieMonsterMcpServer(private val accessibilityService: MyAccessibilityService) {

    companion object {
        fun configureServer(): Server {

            val server = Server(
                serverInfo = Implementation(
                    name = "CookieMonsterAgent",
                    version = "1.0.0"
                ),
                options = ServerOptions(
                    capabilities = ServerCapabilities(
                        tools = ServerCapabilities.Tools(listChanged = true),
                        resources = ServerCapabilities.Resources(
                            listChanged = false,
                            subscribe = false
                        )
                    )
                )
            )


            val screenTool = Tool(
                name = "get_screen_content",
                description = "Returns the JSON tree of the current screen.",
                inputSchema = ToolSchema(
                    properties = buildJsonObject { }
                )
            )

            server.addTool(screenTool) { _ ->
                val content = MyAccessibilityService.instance?.getScreenContent()
                CallToolResult(
                    content = listOf(TextContent(text = content.toString()))
                )
            }

            val screenshotTool = Tool(
                name = "get_screenshot",
                description = "Captures a screenshot of the current device screen. Use this when the accessibility tree is empty, missing information, or when interacting with graphical apps (games, custom views) that don't expose text nodes.",
                inputSchema = ToolSchema(
                    properties = buildJsonObject {},
                    required = listOf()
                )
            )

            server.addTool(screenshotTool) { _ ->
                val service = MyAccessibilityService.instance

                if (service == null) {
                    return@addTool CallToolResult(
                        content = listOf(TextContent(text = "Error: Accessibility Service not running"))
                    )
                }

                val base64Image = service.takeScreenshotBase64()

                if (base64Image.startsWith("Error")) {
                    CallToolResult(content = listOf(TextContent(text = base64Image)))
                } else {
                    CallToolResult(
                        content = listOf(
                            ImageContent(
                                mimeType = "image/png",
                                data = base64Image
                            )
                        )
                    )
                }
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

            server.addTool(clickTool) { request ->
                val arguments = request.arguments as? Map<String, *>
                    ?: throw IllegalArgumentException("Arguments are missing or malformed")

                val boundsRaw = arguments["bounds"]
                    ?: throw IllegalArgumentException("Missing 'bounds' parameter")

                val bounds = boundsRaw.toString().replace("\"", "").trim()

                if (bounds.isBlank() || bounds == "null") {
                    throw IllegalArgumentException("Missing 'bounds' parameter")
                }

                val service = MyAccessibilityService.instance
                val result = if (service != null) {
                    service.clickByBounds(bounds)
                } else {
                    "Error: Service not running"
                }

                CallToolResult(
                    content = listOf(TextContent(text = result))
                )
            }

            val typeTool = Tool(
                name = "input_text",
                description = "Enters text into currently focused input field. You must click on the input field first, using tap_coordinates.",
                inputSchema = ToolSchema(
                    properties = buildJsonObject {
                        put("text", buildJsonObject {
                            put("type", "string")
                            put("description", "The text to enter")
                        })
                    },
                    required = listOf("text")
                )
            )

            server.addTool(typeTool) { request ->
                val arguments = request.arguments as? Map<String, *>
                    ?: throw IllegalArgumentException("Arguments missing.")

                val text = arguments["text"]?.toString()
                    ?: throw IllegalArgumentException("Missing 'text' parameter.")

                val service = MyAccessibilityService.instance

                val result = if (service != null) {
                    service.typeText(text)
                } else {
                    "Error: Service not running"
                }

                CallToolResult(
                    content = listOf(TextContent(text = result))
                )
            }

         return server
        }
    }
}



