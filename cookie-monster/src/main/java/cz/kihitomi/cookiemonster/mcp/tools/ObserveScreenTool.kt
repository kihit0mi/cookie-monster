package cz.kihitomi.cookiemonster.mcp.tools

import cz.kihitomi.cookiemonster.mcp.AgentActionHandler
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.ContentBlock
import io.modelcontextprotocol.kotlin.sdk.types.ImageContent
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.buildJsonObject

/**
 * Tool, as defined by MCP protocol, with all the required properties.
 * Describes what it does to an agent accessing it, and calls for an OS action, separate from the server.
 */
class ObserveScreenTool(private val actionHandler: AgentActionHandler) {

    private val definition = Tool(
        name = "observe_screen",
        // The description AI reads and decides what tool is it going to use.
        // Changing this will drastically impact agent performance and behavior.
        description = "Captures the current state of the device screen. Returns BOTH the structural JSON DOM tree (for exact coordinates and text) and a visual screenshot (for graphical context). ALWAYS use this as your primary way to see.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {},
            required = emptyList()
        )
    )

    /**
     * Mounts the tool onto the active Ktor session.
     * Opts into experimental serialization APIs required by the MCP SDK to properly encode multimodal payloads (like byte arrays for images) into JSON.
     */
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