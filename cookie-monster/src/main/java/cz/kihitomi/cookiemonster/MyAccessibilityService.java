package cz.kihitomi.cookiemonster;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



@SuppressLint("AccessibilityPolicy")
public class MyAccessibilityService extends AccessibilityService {

    // Static reference for testing purposes
    public static MyAccessibilityService instance = null;

    private static final String TAG = "Cookie_Monster";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this; // Set the static reference
        Log.d(TAG, "Cookie Monster woke up.");
        LogManager.INSTANCE.addLog(TAG, "Cookie Monster woke up!");
    }
    private final android.os.Handler debounceHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable debounceRunnable = null;
    private static final long DEBOUNCE_DELAY_MS = 500;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
/* automatic scrape implementation
        // Filtering for window changes ensures we capture the DOM when the UI updates.
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return;
        }

        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
        debounceRunnable = () -> {
            Log.d(TAG, "Screen stabilized. Starting scrape...");
            tryGetRootNode(0);
        };

        debounceHandler.postDelayed(debounceRunnable, DEBOUNCE_DELAY_MS); */
    }

    private void tryGetRootNode(final int attempt) {
        if (attempt > 5) return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            JSONObject dom = serializeNodeToJson(rootNode);
            if (dom != null) {
                saveDomToFile(dom);
            }
            rootNode.recycle();
        } else {
            // Retry logic handles cases where the window is still transitioning.
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() ->
                    tryGetRootNode(attempt + 1), 200);
        }
    }
//manual scrape implementation
    public void captureCurrentWindow() {
        Log.d(TAG, "Manual scrape initiated.");
        tryGetRootNode(0);
    }

    private JSONObject serializeNodeToJson(AccessibilityNodeInfo node) {
        if (node == null) return null;

        JSONObject json = new JSONObject();
        try {
            json.put("class", node.getClassName() != null ? node.getClassName().toString() : "unknown");
            json.put("text", node.getText() != null ? node.getText().toString() : "");
            json.put("resourceId", node.getViewIdResourceName() != null ? node.getViewIdResourceName() : "");
            json.put("description", node.getContentDescription() != null ? node.getContentDescription().toString() : "");

            JSONArray children = new JSONArray();
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    JSONObject childJson = serializeNodeToJson(child);
                    if (childJson != null) {
                        children.put(childJson);
                    }
                    child.recycle(); // Critical: prevent Memory Leaks
                }
            }
            json.put("children", children);
        } catch (org.json.JSONException e) {
            Log.e(TAG, "JSON Serialization error", e);
        }
        return json;
    }

    private void saveDomToFile(JSONObject json) {
        String fileName = "dom_dump_" + System.currentTimeMillis() + ".json";
        File file = new File(getExternalFilesDir(null), fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json.toString(2));
            Log.d(TAG, "DOM saved: " + file.getAbsolutePath());
            LogManager.INSTANCE.addLog(TAG, "Saved DOM: " + fileName);
        } catch (IOException | org.json.JSONException e) {
            Log.e(TAG, "Failed to write JSON", e);
        }
    }

    @Override
    public void onInterrupt() {
        if (debounceHandler != null && debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
        Log.d(TAG, "Cookie Monster was interrupted.");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        instance = null; // Clear the static reference
        Log.d(TAG, "Cookie Monster went to sleep.");
        return super.onUnbind(intent);
    }
}