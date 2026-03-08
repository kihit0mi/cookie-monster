package cz.kihitomi.cookiemonster.mcp

import android.util.Log
import cz.kihitomi.cookiemonster.mcp.prompts.AgentPersonaPrompt
import cz.kihitomi.cookiemonster.mcp.tools.ClickTool
import cz.kihitomi.cookiemonster.mcp.tools.InputTextTool
import cz.kihitomi.cookiemonster.mcp.tools.ObserveScreenTool
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
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities

/**
 * This is the where the server configuration and the MCP protocols lie. They are translated into native Kotlin
 * interface calls, thus separating the network logic completely from the OS logic.
 */
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
        ObserveScreenTool(actionHandler).register(this)

        ClickTool(actionHandler).register(this)

        InputTextTool(actionHandler).register(this)

        ScrollTool(actionHandler).register(this)

        AgentPersonaPrompt().register(this)
    }

    fun start(port: Int = 8080) {
        Log.d("MCP_SERVER", "Starting Ktor on port $port")

        //using 0.0.0.0 instead of localhost to allow connections from other devices on the same WiFi network
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
        }.start(wait = false) //
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
            anyHost() // This is just a prototype tool, otherwise we would need stricter CORS policies.
        }
    }
}