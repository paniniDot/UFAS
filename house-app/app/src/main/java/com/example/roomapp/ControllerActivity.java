package com.example.roomapp;

import static android.content.ContentValues.TAG;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControllerActivity extends AppCompatActivity {
    private static final String NAME = "name";
    private static final String MEASURE = "measure";
    private static final String LIGHT_CHECKBOX = "lightcheckbox";
    private static final String LIGHT = "light";
    private static final String ROLL_CHECKBOX = "rollcheckbox";
    private static final String MANUAL_LIGHT = "manual_light";
    private static final String MANUAL_ROLL = "manual_roll";
    private static final String ROLL = "roll";
    private OutputStream bluetoothOutputStream;
    private MaterialSwitch lightSwitch;
    private CheckBox lightCheckBox;
    private boolean lightState;
    private Slider rollSlider;
    private CheckBox rollCheckBox;
    private int rollState;
    private TextView rollText;
    private BluetoothClientConnectionThread connectionThread;
    private boolean isDragging = false;
    private ExecutorService threadPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_controller);
        lightState = false;
        rollState = 0;
        init();
        threadPool = Executors.newFixedThreadPool(2);
    }

    private void init() {
        rollText = findViewById(R.id.textView3);
        lightSwitch = findViewById(R.id.remotebutton);
        lightSwitch.setOnClickListener((v) -> {
            lightState = !lightState;
            bluetoothSend(LIGHT, lightState ? 1 : 0);
            runOnUiThread(() -> {
                lightSwitch.setThumbIconDrawable(lightState ? ResourcesCompat.getDrawable(getResources(), R.drawable.lightbulb_filled_48px, null) : ResourcesCompat.getDrawable(getResources(), R.drawable.lightbulb_48px, null));
                lightSwitch.setText(LIGHT + (lightState ? " on" : " off"));
            });
        });
        lightCheckBox = findViewById(R.id.checkBox2);
        lightCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                runOnUiThread(() -> lightSwitch.setEnabled(isChecked));
                bluetoothSend(MANUAL_LIGHT, isChecked ? 1 : 0);
            }
        });
        rollSlider = findViewById(R.id.seekBar);
        rollSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                rollState = (int) value;
                isDragging = true;
            }
        });
        rollSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (isDragging) {
                    bluetoothSend(ROLL, rollState);
                    runOnUiThread(() -> {
                        rollText.setText(ROLL + " " + rollState);
                        rollSlider.setValue(rollState);
                    });
                    isDragging = false;
                }
            }
        });
        rollCheckBox = findViewById(R.id.checkBox);
        rollCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                runOnUiThread(() -> rollSlider.setEnabled(isChecked));
                bluetoothSend(MANUAL_ROLL, isChecked ? 1 : 0);
            }
        });
        lightSwitch.setEnabled(false);
        rollSlider.setEnabled(false);
        rollCheckBox.setEnabled(false);
        lightCheckBox.setEnabled(false);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(ScanActivity.X_BLUETOOTH_DEVICE_EXTRA);
        BluetoothAdapter btAdapter = getSystemService(BluetoothManager.class).getAdapter();
        connectionThread = new BluetoothClientConnectionThread(bluetoothDevice, btAdapter, this::manageConnectedSocket);
        connectionThread.start();
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        try {
            bluetoothOutputStream = socket.getOutputStream();
            bluetoothReceive(socket);
            runOnUiThread(() -> {
                lightCheckBox.setEnabled(true);
                rollCheckBox.setEnabled(true);
            });
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
    }

    private void bluetoothSend(String name, int measure) {
        threadPool.execute(() -> {
            try {
                JSONObject configJson = new JSONObject();
                configJson.put(NAME, name);
                configJson.put(MEASURE, measure);
                bluetoothOutputStream.write(configJson.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void bluetoothReceive(BluetoothSocket socket) {
        threadPool.execute(() -> {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while (socket.isConnected() && (message = input.readLine()) != null) {
                    Log.i(TAG, "Message received: " + message);
                    try {
                        JSONObject jsonObject = new JSONObject(message);
                        if (jsonObject.has(NAME) && jsonObject.has(MEASURE)) {
                            String name = jsonObject.getString(NAME);
                            int measure = jsonObject.getInt(MEASURE);
                            setComponentState(name, measure);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when reading input stream", e);
            }
        });
    }

    private void setComponentState(String name, int measure) {
        runOnUiThread(() -> {
            switch (name) {
                case LIGHT:
                    lightState = measure != 0;
                    lightSwitch.setThumbIconDrawable(lightState ? ResourcesCompat.getDrawable(getResources(), R.drawable.lightbulb_filled_48px, null) : ResourcesCompat.getDrawable(getResources(), R.drawable.lightbulb_48px, null));
                    lightSwitch.setChecked(lightState);
                    lightSwitch.setText(LIGHT + (lightState ? " on" : " off"));
                    break;
                case ROLL:
                    rollState = measure;
                    rollSlider.setValue(rollState);
                    rollText.setText(ROLL + " " +rollState);
                    break;
                case LIGHT_CHECKBOX:
                    boolean lightCheckboxValue = measure != 0;
                    lightCheckBox.setChecked(lightCheckboxValue);
                    lightSwitch.setEnabled(lightCheckboxValue);
                    break;
                case ROLL_CHECKBOX:
                    boolean rollCheckboxValue = measure != 0;
                    rollCheckBox.setChecked(rollCheckboxValue);
                    rollSlider.setEnabled(rollCheckboxValue);
                    break;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectionThread.cancel();
        threadPool.shutdownNow();
    }
}
