package cz.kihitomi.cookiemonster;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;



public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "Cookie_Monster";
    private static final String TEST = "Test_Tag";
    private static final String TARGET_WORD = "the";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Cookie Monster woke up.");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //chceme jen chrome
        if (event.getPackageName()==null|| !(event.getPackageName().toString().equals("com.android.chrome"))){
            Log.d(TAG, "Nejsme v chromu.");
            return;
        }

        final int eventType = event.getEventType();
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            Log.d(TAG, "Nejsme v TYPE_WINDOW_STATE_CHANGED ani TYPE_WINDOW_CONTENT_CHANGED.");
            return;
        }
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if(rootNode == null) {
            return;
        }
        travelSearch(rootNode, TARGET_WORD);
    }

    private void travelSearch(AccessibilityNodeInfo node, String searchCookie){
        if (node==null) {
            Log.d(TAG, "node je null");
            return;
        }

        if (node.getText() != null){
            String nodeText = node.getText().toString().toLowerCase();
            Log.d(TEST, nodeText);
            if (nodeText.contains(searchCookie.toLowerCase())){
                Log.d(TAG, "Nasli jsme cookie.");
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

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Cookie Monster byl prerusen.");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Cookie Monster byl uspan.");
        return super.onUnbind(intent);
    }
}