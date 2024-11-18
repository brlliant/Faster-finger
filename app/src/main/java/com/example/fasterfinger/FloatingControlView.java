package com.example.fasterfinger;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.util.Log;
import com.google.android.material.imageview.ShapeableImageView;

public class FloatingControlView extends FrameLayout {
    private static final String TAG = "FloatingControlView";
    private DraggableImageView crosshairView;
    private ShapeableImageView pauseButton;
    private ShapeableImageView settingsButton;
    private ShapeableImageView stopButton;
    private OnControlListener listener;
    private boolean isPaused = true;
    private float dX, dY;
    private float lastTouchX, lastTouchY;

    public interface OnControlListener {
        void onPause();
        void onResume();
        void onStop();
        void onSettings();
        void onPositionChanged(int x, int y);
    }

    public FloatingControlView(Context context) {
        super(new ContextThemeWrapper(context, R.style.Theme_FasterFinger));
        inflate(getContext(), R.layout.floating_control, this);
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        crosshairView = findViewById(R.id.crosshair);
        pauseButton = findViewById(R.id.pauseButton);
        settingsButton = findViewById(R.id.settingsButton);
        stopButton = findViewById(R.id.stopButton);

        // Ensure views are visible and properly initialized
        if (crosshairView != null) {
            crosshairView.setVisibility(VISIBLE);
            crosshairView.setAlpha(0.7f); // Make it slightly transparent
        }

        if (pauseButton != null) {
            pauseButton.setVisibility(VISIBLE);
            updatePauseButtonState();
        }

        if (settingsButton != null) {
            settingsButton.setVisibility(VISIBLE);
        }

        if (stopButton != null) {
            stopButton.setVisibility(VISIBLE);
        }
    }

    private void updatePauseButtonState() {
        pauseButton.setImageResource(isPaused ? R.drawable.ic_play : R.drawable.ic_pause);
    }

    private void setupListeners() {
        crosshairView.setOnTouchListener((v, event) -> {
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            android.graphics.Point size = new android.graphics.Point();
            windowManager.getDefaultDisplay().getSize(size);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    lastTouchX = event.getRawX();
                    lastTouchY = event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;

                    // Boundary checks
                    newX = Math.max(0, Math.min(newX, size.x - v.getWidth()));
                    newY = Math.max(0, Math.min(newY, size.y - v.getHeight()));

                    v.setX(newX);
                    v.setY(newY);

                    if (listener != null) {
                        int centerX = (int) (newX + v.getWidth() / 2);
                        int centerY = (int) (newY + v.getHeight() / 2);
                        listener.onPositionChanged(centerX, centerY);
                        Log.d(TAG, "Position changed to: x=" + centerX + ", y=" + centerY);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (Math.abs(event.getRawX() - lastTouchX) < 10 &&
                            Math.abs(event.getRawY() - lastTouchY) < 10) {
                        v.performClick();
                    }
                    break;
            }
            return true;
        });

        pauseButton.setOnClickListener(v -> {
            if (listener != null) {
                if (isPaused) {
                    listener.onResume();
                    Log.d(TAG, "Resume clicked");
                } else {
                    listener.onPause();
                    Log.d(TAG, "Pause clicked");
                }
                isPaused = !isPaused;
                updatePauseButtonState();
            }
        });

        settingsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSettings();
                Log.d(TAG, "Settings clicked");
            }
        });

        stopButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStop();
                Log.d(TAG, "Stop clicked");
            }
        });
    }

    public void setOnControlListener(OnControlListener listener) {
        this.listener = listener;
    }
}