package cz.kihitomi.cookiemonster

import kotlinx.serialization.Serializable

@Serializable
data class AccessibilityNode(
    val className: String,
    val text: String?,
    val resourceId: String?,
    val description: String?,
    val clickable: Boolean,
    val bounds: String,
    val children: List<AccessibilityNode>
)