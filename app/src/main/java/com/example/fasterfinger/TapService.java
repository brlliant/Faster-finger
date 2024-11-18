package com.example.fasterfinger;

import android.content.Context;
import android.os.Build;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class TapService extends Service {
    private WindowManager windowManager;
    private FloatingControlView floatingControl;
    private AutoTapper autoTapper;
    private boolean isRunning = false;
    private Context materialContext;
    private static final String TAG = "TapService";

    @Override
    public void onCreate() {
        super.onCreate();
        // Ensure service context uses the correct theme
        Context materialContext = new ContextThemeWrapper(this, R.style.Theme_FasterFinger);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        autoTapper = new AutoTapper(); // Updated constructor call
        // Store materialContext to use later
        this.materialContext = materialContext;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && !isRunning) {
            long interval = intent.getLongExtra("interval", 1000);
            Log.d(TAG, "Starting service with interval: " + interval + "ms");
            showToast("Auto-tapper started");

            initializeFloatingControl();
            autoTapper.setInterval(interval);
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

        floatingControl.setOnControlListener(new FloatingControlView.OnControlListener() {
            @Override
            public void onPause() {
                autoTapper.pauseTapping();
                showToast("Auto-tapper paused");
                Log.d(TAG, "Auto-tapper paused");
            }

            @Override
            public void onResume() {
                autoTapper.resumeTapping();
                showToast("Auto-tapper resumed");
                Log.d(TAG, "Auto-tapper resumed");
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