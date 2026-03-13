package cz.kihitomi.cookiemonster.accessibility

import kotlinx.serialization.Serializable

/**
 * A serializable representation of an Android UI element.
 * This class simplifies the UI node to the properties required by the LLM
 * to understand and interact with the screen.
 */
@Serializable
data class AccessibilityNode(
    /** The Android widget type ("android.widget.Button", etc.) */
    val className: String,
    /** The text content of the widget, if any. */
    val text: String?,
    /** The resource ID of the widget, assigned by developer.*/
    val resourceId: String?,
    /** A description of the widget, if any. */
    val description: String?,
    /** Whether the widget is clickable. */
    val clickable: Boolean,
    /**
     * The bounds of the widget in screen coordinates.
     * Format: "left,top,right,bottom"
     * The AI agent uses this string to target the ClickTool.
     */
    val bounds: String,
    /** The nested list of child nodes, forming the DOM tree. */
    val children: List<AccessibilityNode>
)