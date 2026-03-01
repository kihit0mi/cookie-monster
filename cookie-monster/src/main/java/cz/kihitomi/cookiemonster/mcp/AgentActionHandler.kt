package cz.kihitomi.cookiemonster.mcp

interface AgentActionHandler {
    fun getScreenContent(): String?
    suspend fun takeScreenshotBase64(): String
    fun clickByBounds(boundsString: String): String
    fun typeText(text: String, enter: Boolean): String
    fun scroll(direction: String): String
}