package cz.kihitomi.cookiemonster.mcp.tools

import cz.kihitomi.cookiemonster.mcp.AgentActionHandler
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.ExperimentalSerializationApi

class ObserveScreenTool(private val actionHandler: AgentActionHandler) {

    private val definition = Tool(
        name = "observe_screen",
        description = "Captures the current state of the device screen. Returns BOTH the structural JSON DOM tree (for exact coordinates and text) and a visual screenshot (for graphical context). ALWAYS use this as your primary way to see.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {},
            required = emptyList()
        )
    )
    @OptIn(ExperimentalSerializationApi::class)
    fun register(server: Server) {
        server.addTool(definition) { _ ->

            val domText = actionHandler.getScreenContent() ?: "Error: Could not retrieve DOM"

            val base64Image = try {
                actionHandler.takeScreenshotBase64()
            } catch (e: Exception) {
                "Error: Crash while taking screenshot - ${e.message}"
            }

            val contentList = mutableListOf<ContentBlock>()

            contentList.add(TextContent(text = "DOM TREE:\n$domText"))

            if (base64Image.startsWith("Error")) {
                contentList.add(TextContent(text = "SCREENSHOT FAILED: $base64Image"))
            } else {
                contentList.add(ImageContent(mimeType = "image/png", data = base64Image))
            }

            CallToolResult(content = contentList)
        }
    }
}