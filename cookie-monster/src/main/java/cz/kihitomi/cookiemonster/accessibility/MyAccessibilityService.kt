package cz.kihitomi.cookiemonster.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import cz.kihitomi.cookiemonster.logger.LogManager
import cz.kihitomi.cookiemonster.mcp.AgentActionHandler
import cz.kihitomi.cookiemonster.mcp.CookieMonsterServer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

/**
 * The engine of the app, where we actually do all the things LLM needs to autonymously control the phone.
 * It is decoupled from the server side completely, the functions here are  called by our bridge - AgentActionHandler.
 */
@SuppressLint("AccessibilityPolicy")
class MyAccessibilityService : AccessibilityService(), AgentActionHandler {

    // =========================================================================
    // COMPANION OBJECT & STATE
    // =========================================================================

    companion object {
        private const val TAG = "Cookie_Monster"
        private const val SCROLL_DISTANCE_PX = 500f
        private const val GESTURE_DURATION_MS = 300L
    }

    private var mcpServer: CookieMonsterServer? = null

    private val jsonHandler = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // =========================================================================
    // ANDROID LIFECYCLE OVERRIDES
    // =========================================================================
    /**
     * Here is where the server is actually started, the moment the user allows accessibility permissions.
     * We do not want to manually start it, the app is just the server in the users eyes,
     * so it makes no sense to start one without the other.
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
        mcpServer = CookieMonsterServer(actionHandler = this)
        mcpServer?.start(8080)
        Log.d(TAG, "Cookie Monster woke up.")
        LogManager.addLog(TAG, "Cookie Monster woke up!")
    }

    //not used, but required by AccessibilityService
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
        Log.d(TAG, "Cookie Monster was interrupted.")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Cookie Monster went to sleep.")
        return super.onUnbind(intent)
    }

    // =========================================================================
    // MCP TOOLS FUNCTIONS
    // =========================================================================

    override fun getScreenContent(): String {
        val rootNode = rootInActiveWindow ?: return "Root node is null"

        val dataObject = mapNodeToData(rootNode)

        LogManager.addLog(TAG, "Observe Tool was used.")

        return jsonHandler.encodeToString(dataObject)
    }

    /**
     * Allows two different sets of coordinates, for greater flexibility - 4 integers in case of successful DOM scrape,
     * provided directly by Android, or 2 integers in case of LLM guessing based on a screenshot.
     */
    override fun clickByBounds(boundsString: String): String {
        try {
            // Clean up coordinates we get from LLM, we cannot be sure how they are formatted
            val cleanBounds = boundsString.replace("[", "").replace("]", "").replace(" ", "").replace("(", "").replace(")", "")
            val parts = cleanBounds.split(",").map { it.toInt() }

            val (x, y) = when (parts.size) {
                4 -> Pair((parts[0] + parts[2]) / 2f, (parts[1] + parts[3]) / 2f)
                2 -> Pair(parts[0].toFloat(), parts[1].toFloat())
                else -> return "Error: Bad format. Expected either 4 integers (bounds) or 2 integers (x,y)."
            }

            val path = Path().apply {
                moveTo(x, y)
                lineTo(x, y)
            }

            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, GESTURE_DURATION_MS)) //Android might not register the tap properly if too short or long
                .build()

            val dispatched = dispatchGesture(gesture, null, null)

            if (dispatched) LogManager.addLog(TAG, "Click Tool was used.")


            return if (dispatched) "Clicked on [$x, $y]" else "Error: Click failed."

        } catch (e: Exception) {
            Log.d("clicking", "click failed")
            return "Error while clicking: ${e.message}"
        }
    }

    /**
     * Requires Android 11 (API 30) or higher to use takeScreenshot(). For lower versions, we would need to use
     * MediaProjection API, which pulls out consent form every time it's called, thus slowing the whole process down a lot.
     */
    override suspend fun takeScreenshotBase64(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return "Error: Screenshot requires Android 11 (API 30) or higher."
        }

        return suspendCancellableCoroutine { continuation ->
            val display = Display.DEFAULT_DISPLAY

            takeScreenshot(
                display, applicationContext.mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(result: ScreenshotResult) {
                        try {
                            val hardwareBuffer = result.hardwareBuffer
                            val colorSpace = result.colorSpace

                            val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)

                            if (bitmap == null) {
                                continuation.resume("Error: Failed to create bitmap")
                                return
                            }

                            val outputStream = ByteArrayOutputStream()
                            val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                            softwareBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                            val imageBytes = outputStream.toByteArray()
                            val base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                            // free the the memory explicitly, waiting for garbage collection would cause OOM crashes
                            hardwareBuffer.close()
                            softwareBitmap.recycle()
                            bitmap.recycle()

                            continuation.resume(base64String)
                        } catch (e: Exception) {
                            continuation.resume("Error processing screenshot: ${e.message}")
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        continuation.resume("Error: Screenshot failed with code $errorCode")
                    }
                }
            )
        }
    }

    /**
     * ACTION_IME_ENTER requires Android 11 (API 30) or higher.
     * This functions only work when an input field is selected, that's why we have to ensure it's clicked first.
     */
    @RequiresApi(30)
    override fun typeText(text: String, enter: Boolean): String {

        val root = rootInActiveWindow ?: return "Error: Could not access screen content."

        val focusedNode = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

        if (focusedNode == null) {
            return "Error: No text field is focused. Please use 'tap_coordinates' to click the text box first."
        }

        val arguments = Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        )

        val success = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

        if (enter) {
            focusedNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id)
        }

        if (success) LogManager.addLog(TAG, "Input Tool was used.")

        return if (success && !enter) {
            "Success: Typed '$text'"
        } else if (success) {
            "Success: Typed '$text' and pressed enter."
        } else {
            "Error: Focused node rejected the text. It might not be editable."
        }
    }

    override fun scroll(direction: String): String {
        val displayMetrics = resources.displayMetrics
        // starting the scroll in the middle of the screen for safety - the least chance we run out of space
        val middleHeight = (displayMetrics.heightPixels / 2).toFloat()
        val middleWidth = (displayMetrics.widthPixels / 2).toFloat()

        val gestureBuilder = GestureDescription.Builder()
        val path = Path()

        when (direction) {
            "up" -> {
                path.moveTo(middleWidth, middleHeight)
                path.lineTo(middleWidth, middleHeight + SCROLL_DISTANCE_PX)
            }

            "down" -> {
                path.moveTo(middleWidth, middleHeight)
                path.lineTo(middleWidth, middleHeight - SCROLL_DISTANCE_PX)
            }

            "left" -> {
                path.moveTo(middleWidth, middleHeight)
                path.lineTo(middleWidth + SCROLL_DISTANCE_PX, middleHeight)
            }

            "right" -> {
                path.moveTo(middleWidth, middleHeight)
                path.lineTo(middleWidth - SCROLL_DISTANCE_PX, middleHeight)
            }

            else -> {
                return "Error: Invalid direction. Use 'up', 'down', 'left', or 'right'."
            }

        }

        gestureBuilder.addStroke(
            GestureDescription.StrokeDescription(
                path,
                0L,
                GESTURE_DURATION_MS
            )
        )
        dispatchGesture(gestureBuilder.build(), null, null)

        LogManager.addLog(TAG, "Scroll Tool was used.")

        return "Success: Scrolled $direction"
    }

    // =========================================================================
    // INTERNAL HELPER FUNCTIONS
    // =========================================================================
    /**
     * Strips the native Android nodes of circular references (which break JSON serialization)
     * and filters the DOM down to only the useful properties.
     */
    private fun mapNodeToData(node: AccessibilityNodeInfo): AccessibilityNode {
        val childNodes = mutableListOf<AccessibilityNode>()

        val rect = Rect()
        node.getBoundsInScreen(rect)
        val boundsString = "${rect.left},${rect.top},${rect.right},${rect.bottom}"

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                childNodes.add(mapNodeToData(child))

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

    private fun tryGetRootNode(attempt: Int) {
        if (attempt > MAX_ROOT_NODE_ATTEMPTS) return

        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val dataObject = mapNodeToData(rootNode)
            val jsonString = jsonHandler.encodeToString(dataObject)
            saveJsonToFile(jsonString)

        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                tryGetRootNode(attempt + 1)
            }, ROOT_NODE_RETRY_DELAY_MS)
        }
    }
}