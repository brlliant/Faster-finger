package com.example.fasterfinger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
    private boolean isServiceRunning = false;
    private Button startButton;
    private EditText intervalInput;
    private RadioGroup modeGroup;
    private Spinner intervalUnit;

    private final ActivityResultLauncher<Intent> overlayPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, R.string.overlay_permission_required, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
    );

    private final ActivityResultLauncher<Intent> accessibilityPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Permission result handled in isAccessibilityServiceEnabled()
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkRequiredPermissions();
        initializeUI();
    }

    private void checkRequiredPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );
            overlayPermissionLauncher.launch(intent);
        }

        if (!isAccessibilityServiceEnabled()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            accessibilityPermissionLauncher.launch(intent);
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED
            );
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String service = getPackageName() + "/" + TapAccessibilityService.class.getCanonicalName();
            String enabledServices = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            return enabledServices != null && enabledServices.contains(service);
        }
        return false;
    }

    private void initializeUI() {
        startButton = findViewById(R.id.startButton);
        intervalInput = findViewById(R.id.intervalInput);
        modeGroup = findViewById(R.id.modeGroup);
        intervalUnit = findViewById(R.id.intervalUnit);

        startButton.setOnClickListener(v -> handleStartStop());
    }

    private void handleStartStop() {
        if (!isServiceRunning) {
            if (validateInput()) {
                startTapService();
                startButton.setText(R.string.stop);
                isServiceRunning = true;
                //minimise the app after starting service
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            }
        } else {
            stopTapService();
            startButton.setText(R.string.start);
            isServiceRunning = false;
        }
    }

    private boolean validateInput()
    {
        String intervalText = intervalInput.getText().toString();
        if (intervalText.isEmpty())
        {
            intervalInput.setError(getString(R.string.interval_empty_error));
            return false;
        }

        try {
            int interval = Integer.parseInt(intervalText);
            if (interval < 50)
            {
                intervalInput.setError("Interval must be at least 50ms");
                return false;
            }
        }
        catch (NumberFormatException e)
        {
            intervalInput.setError(getString(R.string.interval_invalid_error));
            return false;
        }

        return true;
    }

    private void startTapService()
    {
        Intent intent = new Intent(this, TapService.class);
        intent.putExtra("interval", calculateInterval());
        intent.putExtra("singlePoint", modeGroup.getCheckedRadioButtonId() == R.id.singlePointMode);
        intent.putExtra("initialStart", false);  // Adding a flag to indicate initial start
        startService(intent);
    }

    private void stopTapService()
    {
        stopService(new Intent(this, TapService.class));
    }

    private long calculateInterval() {
        int baseInterval = Integer.parseInt(intervalInput.getText().toString());
        long multiplier;
        switch (intervalUnit.getSelectedItemPosition()) {
            case 1: // Seconds
                multiplier = 1000L;
                break;
            case 2: // Minutes
                multiplier = 60000L;
                break;
            default: // Milliseconds
                multiplier = 1L;
                break;
        }
        return baseInterval * multiplier;
    }
}