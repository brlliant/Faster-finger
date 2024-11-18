package com.example.fasterfinger;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class TapAccessibilityService extends AccessibilityService {
    private static TapAccessibilityService instance;
    private static final String TAG = "TapAccessibilityService";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "TapAccessibilityService created");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not used in this implementation
    }

    @Override
    public void onInterrupt() {
        // Not used in this implementation
    }

    public boolean performTap(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path clickPath = new Path();
            clickPath.moveTo(x, y);

            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 1));

            return dispatchGesture(builder.build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.d(TAG, "Tap completed at x=" + x + ", y=" + y);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.d(TAG, "Tap cancelled at x=" + x + ", y=" + y);
                }
            }, null);
        }
        return false;
    }

    public static TapAccessibilityService getInstance() {
        return instance;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "TapAccessibilityService destroyed");
    }
}