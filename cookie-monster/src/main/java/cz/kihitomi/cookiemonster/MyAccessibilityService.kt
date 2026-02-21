package cz.kihitomi.cookiemonster

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.serialization.json.Json
import java.io.File
import android.graphics.Bitmap
import android.view.Display
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import android.util.Base64
import kotlin.coroutines.resume


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
            val cleanBounds = boundsString.replace("[", "")
                .replace("]", "")
                .replace(" ", "")

            val parts = cleanBounds.split(",").map { it.toInt() }

            if (parts.size != 4) return "Error: Bad bounds format. Expected 4 integers."

            val x = (parts[0] + parts[2]) / 2f
            val y = (parts[1] + parts[3]) / 2f

            val path = android.graphics.Path()
            path.moveTo(x, y)
            path.lineTo(x, y)



            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
                .build()

            val dispatched = dispatchGesture(gesture, null, null)

            return if (dispatched) "Clicked on [$x, $y]" else "Error: Click failed."

        } catch (e: Exception) {
            Log.d("clicking", "click failed")
            return "Error while clicking: ${e.message}"
        }
    }

    suspend fun takeScreenshotBase64(): String {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            return "Error: Screenshot requires Android 11 (API 30) or higher."
        }

        return suspendCancellableCoroutine { continuation ->
            val display = Display.DEFAULT_DISPLAY

            takeScreenshot(display, applicationContext.mainExecutor,
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

    fun typeText(text: String): String {
        val root = rootInActiveWindow ?: return "Error: Could not access screen content."

        val focusedNode = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

        if (focusedNode == null) {
            return "Error: No text field is focused. Please use 'tap_coordinates' to click the text box first."
        }

        val arguments = android.os.Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        )

        val success = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)


        return if (success) {
            "Success: Typed '$text'"
        } else {
            "Error: Focused node rejected the text. It might not be editable."
        }
    }

    fun scroll(direction: String): String {
        val displayMetrics = resources.displayMetrics
        val middleHeight = (displayMetrics.heightPixels / 2).toFloat()
        val middleWidth = (displayMetrics.widthPixels / 2).toFloat()



        val gestureBuilder = GestureDescription.Builder()
        val path = android.graphics.Path()

        when (direction) {
            "up" -> {
                path.moveTo(middleWidth, middleHeight)
                path.lineTo(middleWidth, middleHeight + 500f)}
            "down" -> {
                path.moveTo(middleWidth, middleHeight)
                path.lineTo(middleWidth, middleHeight - 500f)}
            "left" -> {
                path.moveTo(middleWidth, middleHeight)
                path.lineTo(middleWidth + 500f, middleHeight)}
            "right" -> {
                path.moveTo(middleWidth, middleHeight)
                path.lineTo(middleWidth - 500f, middleHeight)}

            else -> {
                 return "Error: Invalid direction. Use 'up', 'down', 'left', or 'right'."
                }

        }

        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path as android.graphics.Path, 0L, 300L))
        dispatchGesture(gestureBuilder.build(), null, null)

        return "Success: Scrolled $direction"
    }
}