package cz.kihitomi.cookiemonster.mcp

/**
 * The bridge between the MCP server and Android Accessibility Service (AAS).
 * All MCP tools use this interface to translate agent commands into AAS actions.
 */
interface AgentActionHandler {

    /**
     * Captures the current state of active window as a DOM tree.
     *
     * @return JSON string representation of the DOM, or an error string if
     * the root node is inaccessible.
     */
    fun getScreenContent(): String?

    /**
     * Takes a screenshot of the device screen.
     * Requires Android 11 (API 30) or higher.
     *
     * @return A Base64-encoded string of the screenshot, or an error string if
     * the capture fails or the OS version is unsupported
     */
    suspend fun takeScreenshotBase64(): String

    /**
     * Dispatches a tap gesture to a specific set of coordinates.
     *
     * @param boundsString A string of coordinates, it accepts either:
     * - 4 integers (x1, y1, x2, y2)
     * - 2 integers (x, y)
     * @return A success message confirming the clicked coordinates, or an error string.
     */
    fun clickByBounds(boundsString: String): String

    /**
     * Injects text into the currently focused UI element.
     *
     * @param text The text to be typed.
     * @param enter Whether to press the "Enter" key after typing.
     * @return A success message confirming the text typing, or an error string.
     */
    fun typeText(text: String, enter: Boolean): String

    /**
     * Dispatches a swipe gesture from the center of the screen.
     *
     * @param direction The direction of the swipe. Can be "up", "down", "left", or "right".
     * @return A success message confirming the swipe, or an error string.
     */
    fun scroll(direction: String): String
}