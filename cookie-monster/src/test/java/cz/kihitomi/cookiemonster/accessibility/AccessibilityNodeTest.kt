package cz.kihitomi.cookiemonster.accessibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AccessibilityNodeTest {

    @Test
    fun testAccessibilityNodeInstantiation() {
        val childNode = AccessibilityNode(
            className = "android.widget.TextView",
            text = "Child Text",
            resourceId = "test:id/child",
            description = "A child description",
            clickable = false,
            bounds = "0,0,10,10",
            children = emptyList()
        )

        val rootNode = AccessibilityNode(
            className = "android.widget.LinearLayout",
            text = null,
            resourceId = "test:id/root",
            description = null,
            clickable = true,
            bounds = "0,0,100,100",
            children = listOf(childNode)
        )

        assertEquals("android.widget.LinearLayout", rootNode.className)
        assertEquals(true, rootNode.clickable)
        assertEquals("0,0,100,100", rootNode.bounds)
        assertEquals(1, rootNode.children.size)

        val firstChild = rootNode.children[0]
        assertEquals("android.widget.TextView", firstChild.className)
        assertEquals("Child Text", firstChild.text)
        assertEquals("test:id/child", firstChild.resourceId)
        assertFalse(firstChild.clickable)
    }
}
