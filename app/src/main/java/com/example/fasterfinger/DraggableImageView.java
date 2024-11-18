package com.example.fasterfinger;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

public class DraggableImageView extends AppCompatImageView {
    public DraggableImageView(Context context) {
        super(context);
        init();
    }

    public DraggableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Ensure the crosshair is visible
        setVisibility(VISIBLE);
        // Set a default padding to make it easier to grab
        setPadding(8, 8, 8, 8);
    }

    @Override
    public boolean performClick() {
        // Call super.performClick() to handle accessibility events
        return super.performClick();
    }
}