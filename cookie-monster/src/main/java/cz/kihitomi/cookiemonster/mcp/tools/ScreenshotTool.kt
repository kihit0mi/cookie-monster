package cz.kihitomi.cookiemonster.mcp.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.serialization.json.*
import cz.kihitomi.cookiemonster.mcp.AgentActionHandler

class ScreenshotTool(private val actionHandler: AgentActionHandler) {
    private val definition = Tool(
        name = "get_screenshot",
        description = "Captures a screenshot of the current device screen. Use this when the accessibility tree is empty, missing information, or when interacting with graphical apps (games, custom views) that don't expose text nodes.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {},
            required = listOf()
        )
    )

    fun register(server: Server) {
        server.addTool(definition) { _ ->

            val base64Image = try {
                actionHandler.takeScreenshotBase64()
            } catch (e: Exception) {
                "Error: Crash while taking screenshot - ${e.message}"
            }

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
    }
}