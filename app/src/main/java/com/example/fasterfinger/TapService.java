package com.example.fasterfinger;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import android.app.Service;

public class TapService extends Service {
    private WindowManager windowManager;
    private FloatingControlView floatingControl;
    private AutoTapper autoTapper;
    private boolean isRunning = false;
    private Context materialContext;
    private long savedInterval = 1000; // Default interval
    private static final String TAG = "TapService";

    @Override
    public void onCreate() {
        super.onCreate();
        // Ensure service context uses the correct theme
        materialContext = new ContextThemeWrapper(this, R.style.Theme_FasterFinger);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        autoTapper = new AutoTapper();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && !isRunning) {
            // Save interval for later use
            savedInterval = intent.getLongExtra("interval", 1000);
            boolean initialStart = intent.getBooleanExtra("initialStart", false);

            Log.d(TAG, "Starting service with interval: " + savedInterval + "ms");

            initializeFloatingControl();
            autoTapper.setInterval(savedInterval);

            // Only show floating control, do not start auto-tapping
            if (!initialStart) {
                showToast("Floating control activated");
            }

            isRunning = true;
        }
        return START_STICKY;
    }

    private void initializeFloatingControl() {
        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        floatingControl = new FloatingControlView(materialContext);
        floatingControl.setAutoTapper(autoTapper);
        floatingControl.setInitialTapPosition((int)(savedInterval / 2), (int)(savedInterval / 2));

        floatingControl.setOnControlListener(new FloatingControlView.OnControlListener() {
            @Override
            public void onPause() {
                autoTapper.pauseTapping();
                showToast("Auto-tapper paused");
                Log.d(TAG, "Auto-tapper paused");
            }

            @Override
            public void onResume() {
                // Start auto-tapping when resume is clicked
                autoTapper.startTapping();
                showToast("Auto-tapper started");
                Log.d(TAG, "Auto-tapper started");
            }

            @Override
            public void onStop() {
                Log.d(TAG, "Auto-tapper stopping");
                showToast("Auto-tapper stopped");
                stopSelf();
            }

            @Override
            public void onSettings() {
                Intent intent = new Intent(TapService.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onPositionChanged(int x, int y) {
                autoTapper.updateTapPoint(x, y);
                Log.d(TAG, "Tap position updated: x=" + x + ", y=" + y);
            }
        });

        windowManager.addView(floatingControl, params);
    }

    @Override
    public void onDestroy() {
        if (floatingControl != null) {
            windowManager.removeView(floatingControl);
        }
        if (autoTapper != null) {
            autoTapper.stopTapping();
        }
        isRunning = false;
        Log.d(TAG, "Service destroyed");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        );
    }
}