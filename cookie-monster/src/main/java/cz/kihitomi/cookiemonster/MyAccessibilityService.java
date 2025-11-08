package cz.kihitomi.cookiemonster;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import java.util.List;


public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "Cookie_Monster";
    private static final String TEST = "Test_Tag";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Cookie Monster woke up.");
        LogManager.INSTANCE.addLog(TAG, "Cookie Monster woke up.");

        android.accessibilityservice.AccessibilityServiceInfo info = new android.accessibilityservice.AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.notificationTimeout = 100;

        setServiceInfo(info);

        android.accessibilityservice.AccessibilityServiceInfo checkInfo = getServiceInfo();
        if (checkInfo != null) {
            Log.d(TAG, "Event types: " + checkInfo.eventTypes);
            Log.d(TAG, "Package names: " + (checkInfo.packageNames != null ? java.util.Arrays.toString(checkInfo.packageNames) : "ALL"));
            Log.d(TAG, "Feedback type: " + checkInfo.feedbackType);
            Log.d(TAG, "Flags: " + checkInfo.flags);
        } else {
            Log.d(TAG, "ServiceInfo is NULL!");
        }


        Log.d(TAG, "Service configured programmatically");

        Toast.makeText(this, "Accessibility Service Connected!", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final String targetWord = getTargetWord();

        if (event.getPackageName()==null|| !(event.getPackageName().toString().equals("com.android.settings"))){
            return;
        }

        final int eventType = event.getEventType();
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return;
        }

        // Use the retry approach:
        tryGetRootNode(0, targetWord);
    }

    private void tryGetRootNode(final int attempt, final String targetWord) {
        if (attempt > 5) {
            Log.d(TAG, "Failed to get root after 5 attempts");
            return;
        }

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            Log.d(TAG, "Got rootNode on attempt " + attempt);
            travelSearch(rootNode, targetWord);
        } else {
            Log.d(TAG, "Attempt " + attempt + " failed, retrying...");
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    tryGetRootNode(attempt + 1, targetWord);
                }
            }, 200);
        }
    }

    private void travelSearch(AccessibilityNodeInfo node, String searchCookie){
        if (node==null) {
            Log.d(TAG, "node je null");
            return;
        }

        Log.d(TAG, "Checking node. Children: " + node.getChildCount());

        if (node.getText() != null){
            String nodeText = node.getText().toString().toLowerCase();
            Log.d(TEST, nodeText);
            LogManager.INSTANCE.addLog(TEST, nodeText);

            if (nodeText.contains(searchCookie.toLowerCase())){
                Log.d(TAG, "Nasli jsme slovo.");
                LogManager.INSTANCE.addLog(TAG, "Nasli jsme vase slovo.");

                //TODO pridat klikani na button, zatim jen loguji
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            travelSearch(child, searchCookie);
            if (child != null) {
                child.recycle();
            }
        }

    }

    private String getTargetWord() {
        SharedPreferences sharedPrefs = getSharedPreferences("CookieMonsterPrefs", Context.MODE_PRIVATE);
        return sharedPrefs.getString("target_word","");
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Cookie Monster byl prerusen.");
        LogManager.INSTANCE.addLog(TAG, "Cookie Monster byl prerusen.");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Cookie Monster byl uspan.");
        LogManager.INSTANCE.addLog(TAG, "Cookie Monster byl uspan.");
        return super.onUnbind(intent);
    }
}