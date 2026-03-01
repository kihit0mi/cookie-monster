package cz.kihitomi.cookiemonster.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import android.util.Log
import cz.kihitomi.cookiemonster.mcp.tools.ClickTool
import cz.kihitomi.cookiemonster.mcp.tools.InputTextTool
import cz.kihitomi.cookiemonster.mcp.tools.ScreenContentTool
import cz.kihitomi.cookiemonster.mcp.tools.ScreenshotTool
import cz.kihitomi.cookiemonster.mcp.tools.ScrollTool
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.modelcontextprotocol.kotlin.sdk.server.mcp

class CookieMonsterServer(private val actionHandler: AgentActionHandler) {

    val mcpSession = Server(
        serverInfo = Implementation(
            name = "CookieMonsterAgent",
            version = "1.0.0"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
                prompts = ServerCapabilities.Prompts(listChanged = false),
                resources = ServerCapabilities.Resources(
                    listChanged = false,
                    subscribe = false
                )
            )
        )
    ).apply {
        ScreenContentTool(actionHandler).register(this)

        ScreenshotTool(actionHandler).register(this)

        ClickTool(actionHandler).register(this)

        InputTextTool(actionHandler).register(this)

        ScrollTool(actionHandler).register(this)

        AgentPersonaPrompt().register(this)
    }

    fun start(port: Int = 8080) {
        Log.d("MCP_SERVER", "Starting Ktor on port $port")

        embeddedServer(CIO, host = "0.0.0.0", port = port) {
            install(SSE)
            installCors()

            routing {
                get("/health") { call.respondText("OK") }

                mcp {
                    Log.d("MCP_SERVER", "Connection accepted!")
                    return@mcp mcpSession
                }
            }
        }.start(wait = false)
    }

    private fun Application.installCors() {
        install(CORS) {
            allowMethod(HttpMethod.Companion.Options)
            allowMethod(HttpMethod.Companion.Get)
            allowMethod(HttpMethod.Companion.Post)
            allowMethod(HttpMethod.Companion.Delete)
            allowMethod(HttpMethod.Companion.Put)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            allowNonSimpleContentTypes = true
            maxAgeInSeconds = 3600
            anyHost()
        }
    }
}