package cz.kihitomi.cookiemonster

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class MyAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "Cookie_Monster"
        var instance: MyAccessibilityService? = null
    }


    private val jsonHandler = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Cookie Monster woke up.")
        LogManager.addLog(TAG, "Cookie Monster woke up!")
        AndroidMcpServer.start(8080)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
        Log.d(TAG, "Cookie Monster was interrupted.")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        AndroidMcpServer.stop()
        Log.d(TAG, "Cookie Monster went to sleep.")
        return super.onUnbind(intent)
    }

    fun captureCurrentWindow() {
        Log.d(TAG, "Manual scrape initiated.")
        tryGetRootNode(0)
    }

    fun getScreenContent(): String {
        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            return "Root node is null"
        }
        val dataObject = mapNodeToData(rootNode)
        return jsonHandler.encodeToString(dataObject)
    }

    private fun tryGetRootNode(attempt: Int) {
        if (attempt > 5) return

        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val dataObject = mapNodeToData(rootNode)
            val jsonString = jsonHandler.encodeToString(dataObject)
            saveJsonToFile(jsonString)

            rootNode.recycle()
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                tryGetRootNode(attempt + 1)
            }, 200)
        }
    }

    private fun mapNodeToData(node: AccessibilityNodeInfo): AccessibilityNode {
        val childNodes = mutableListOf<AccessibilityNode>()

        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        val boundsString = "${rect.left},${rect.top},${rect.right},${rect.bottom}"

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                childNodes.add(mapNodeToData(child))

                child.recycle()
            }
        }

        return AccessibilityNode(
            className = node.className?.toString() ?: "unknown",
            text = node.text?.toString(),
            resourceId = node.viewIdResourceName,
            description = node.contentDescription?.toString(),
            clickable = node.isClickable,
            bounds = boundsString,
            children = childNodes
        )
    }

    private fun saveJsonToFile(jsonString: String) {
        val fileName = "dom_dump_${System.currentTimeMillis()}.json"
        val file = File(getExternalFilesDir(null), fileName)

        try {
            // zapíšeme to do souboru
            file.writeText(jsonString)
            Log.d(TAG, "DOM saved: ${file.absolutePath}")
            LogManager.addLog(TAG, "Saved DOM: $fileName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write JSON", e)
        }
    }

    fun clickByBounds(boundsString: String): String {
        try {
            val parts = boundsString.split(",").map { it.toInt() }
            if (parts.size != 4) return "Error: Bad bounds format."

            val x = (parts[0] + parts[2]) / 2f
            val y = (parts[1] + parts[3]) / 2f

            val path = android.graphics.Path()
            path.moveTo(x,y)

            val gesture = android.accessibilityservice.GestureDescription.Builder()
                .addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, 100))
                .build()

            val dispatched = dispatchGesture(gesture, null, null)
            return if (dispatched) "Clicked on [$x, $y]" else "Error: Click failed."

        } catch (e: Exception) {
            return "Error while clicking: ${e.message}"
        }
    }
}