package com.example.fasterfinger;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class AutoTapper {
    private static final String TAG = "AutoTapper";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private boolean isPaused = false;
    private long interval = 1000;
    private int currentX = 0;
    private int currentY = 0;
    private final Runnable tapRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning && !isPaused) {
                performTap();
                handler.postDelayed(this, interval);
            }
        }
    };

    public AutoTapper() {
        // Constructor now doesn't need the unused context parameter
    }

    public void setInterval(long interval) {
        this.interval = Math.max(50, interval);
        Log.d(TAG, "Interval set to: " + this.interval + "ms");
    }

    public void updateTapPoint(int x, int y) {
        this.currentX = x;
        this.currentY = y;
        Log.d(TAG, "Tap point updated to: x=" + x + ", y=" + y);
    }

    public void startTapping() {
        if (!isRunning) {
            isRunning = true;
            isPaused = false;
            handler.post(tapRunnable);
            Log.d(TAG, "Started tapping");
        } else if (isPaused) {
            resumeTapping();
        }
    }

    public void pauseTapping() {
        if (isRunning && !isPaused) {
            isPaused = true;
            handler.removeCallbacks(tapRunnable);
            Log.d(TAG, "Paused tapping");
        }
    }

    public void resumeTapping() {
        if (isRunning && isPaused) {
            isPaused = false;
            handler.post(tapRunnable);
            Log.d(TAG, "Resumed tapping");
        }
    }

    public void stopTapping() {
        isRunning = false;
        isPaused = false;
        handler.removeCallbacks(tapRunnable);
        Log.d(TAG, "Stopped tapping");
    }

    private void performTap() {
        TapAccessibilityService service = TapAccessibilityService.getInstance();
        if (service != null) {
            boolean success = service.performTap(currentX, currentY);
            Log.d(TAG, "Performing tap at x=" + currentX + ", y=" + currentY +
                    " (success=" + success + ")");
        } else {
            Log.e(TAG, "TapAccessibilityService not available");
            stopTapping();
        }
    }
}