package cz.kihitomi.cookiemonster;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


@SuppressLint("AccessibilityPolicy")
public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "Cookie_Monster";
    private static final int NOTIFICATION_ID = 1;


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Cookie Monster woke up.");

        AccessibilityServiceInfo checkInfo = getServiceInfo();
        if (checkInfo != null) {
            Log.d(TAG, "AccessibilityServiceInfo: " + checkInfo);
            Log.d(TAG, "Event types: " + checkInfo.eventTypes);
            Log.d(TAG, "Package names: " + (checkInfo.packageNames != null ? java.util.Arrays.toString(checkInfo.packageNames) : "ALL"));
            Log.d(TAG, "Feedback type: " + checkInfo.feedbackType);
            Log.d(TAG, "Flags: " + checkInfo.flags);
        } else {
            Log.d("debug", "ServiceInfo is NULL!");
        }

            LogManager.INSTANCE.addLog(TAG, "Cookie Monster woke up!");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final String targetWord = getTargetWord();

        if (event.getPackageName()==null|| !(event.getPackageName().toString().equals("com.android.chrome"))){
            return;
        }

        final int eventType = event.getEventType();
        Log.d(TAG, String.valueOf(eventType));
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
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
        boolean found = travelSearchHelper(node, searchCookie);

        if (found) {
            Log.d(TAG, "Nasli jsme slovo.");
            LogManager.INSTANCE.addLog(TAG, "Nasli jsme vase slovo.");

            showNotification(searchCookie);
        }
    }

    private void showNotification(String word) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "cookie_alert")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Slovo " + word)
                .setContentText("Nasli jsme vase slovo.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private boolean travelSearchHelper(AccessibilityNodeInfo node, String searchCookie){
        if (node == null) {
            return false;
        }

        if (node.getText() != null){
            String nodeText = node.getText().toString().toLowerCase();
            if (nodeText.contains(searchCookie.toLowerCase())){
                return true;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            boolean found = travelSearchHelper(child, searchCookie);
            if (child != null) {
                child.recycle();
            }
            if (found) {
                return true;
            }
        }

        return false;
    }

    private String getTargetWord() {
        SharedPreferences sharedPrefs = getSharedPreferences("CookieMonsterPrefs", Context.MODE_PRIVATE);
        return sharedPrefs.getString("target_word","cookie");
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
