package cz.kihitomi.cookiemonster;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.util.Log;
import android.widget.Toast;


public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "Cookie_Monster";
    private static final String TEST = "Test_Tag";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Cookie Monster woke up.");
        LogManager.INSTANCE.addLog(TAG, "Cookie Monster woke up.");

        android.accessibilityservice.AccessibilityServiceInfo checkInfo = getServiceInfo();
        if (checkInfo != null) {
            Log.d(TAG, "Event types: " + checkInfo.eventTypes);
            Log.d(TAG, "Package names: " + (checkInfo.packageNames != null ? java.util.Arrays.toString(checkInfo.packageNames) : "ALL"));
            Log.d(TAG, "Feedback type: " + checkInfo.feedbackType);
            Log.d(TAG, "Flags: " + checkInfo.flags);
        } else {
            Log.d("debug", "ServiceInfo is NULL!");
        }


        //Log.d(TAG, "Service configured programmatically");

        Toast.makeText(this, "Accessibility Service Connected!", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final String targetWord = getTargetWord();

        if (event.getPackageName()==null|| !(event.getPackageName().toString().equals("com.android.chrome"))){
            return;
        }

        final int eventType = event.getEventType();
        Log.d(TAG, String.valueOf(eventType));
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

            AccessibilityServiceInfo info = getServiceInfo();
            Log.d("debug", "Service info: " + (info != null ? "exists" : "null"));

            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() ->
                    tryGetRootNode(attempt + 1, targetWord), 200);
        }
    }

    private void travelSearch(AccessibilityNodeInfo node, String searchCookie){
        if (node==null) {
            Log.d("debugTravelSearch", "node je null");
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