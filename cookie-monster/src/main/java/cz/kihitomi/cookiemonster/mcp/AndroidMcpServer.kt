package cz.kihitomi.cookiemonster.mcp

import android.util.Log
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.modelcontextprotocol.kotlin.sdk.server.mcp

object AndroidMcpServer {
    private const val PORT = 8080

    fun start(port: Int = 8080): EmbeddedServer<*, *> {
        Log.d("MCP_SERVER", "Starting on $port")

        val server = embeddedServer(CIO, host = "0.0.0.0", port = port) {
            install(SSE)
            installCors()

            routing {
                get("/health") { call.respondText("OK") }

                mcp {
                    Log.d("MCP_SERVER", "Connection accepted!")
                    return@mcp CookieMonsterMcpServer.configureServer()
                }
            }
        }.start(wait = false)
        return server
    }


    private fun printBanner(port: Int, path: String = "") {
        if (PORT == 0) {
            println("🎬 Starting SSE server on random port")
        } else {
            println("🎬 Starting SSE server on ${if (PORT > 0) "port $PORT" else "random port"}")
            println("🔍 Use MCP inspector to connect to http://localhost:$PORT$path")
        }
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