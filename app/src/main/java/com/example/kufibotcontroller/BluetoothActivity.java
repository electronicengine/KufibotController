package com.example.kufibotcontroller;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothWifi";
    private static final String RASPBERRY_PI_MAC = "2C:CF:67:32:CF:8E";
    private static final UUID RFCOMM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket = null;
    private static final int BLUETOOTH_PERMISSION_REQUEST = 1; // Request code

    private EditText ssidInput, passwordInput;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        ssidInput = findViewById(R.id.ssidInput);
        passwordInput = findViewById(R.id.passwordInput);
        connectButton = findViewById(R.id.connectButton);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Set up the toolbar as the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = ssidInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (!ssid.isEmpty() && !password.isEmpty()) {
                    sendWifiCredentials(ssid, password);
                } else {
                    Toast.makeText(BluetoothActivity.this, "Please enter both SSID and Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to request Bluetooth permission
    private void requestBluetoothPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                BLUETOOTH_PERMISSION_REQUEST
        );
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendWifiCredentials(String ssid, String password) {
        BluetoothDevice raspberryPi = bluetoothAdapter.getRemoteDevice(RASPBERRY_PI_MAC);

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermission();
                Toast.makeText(getApplicationContext(), "Bluetooth permission is denied", Toast.LENGTH_LONG).show();
            }
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "No Bluetooth adapter available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if Bluetooth is enabled
            if (!bluetoothAdapter.isEnabled()) {
                Log.e(TAG, "Bluetooth is not enabled");
                Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
                return;
            }

            bluetoothSocket = raspberryPi.createRfcommSocketToServiceRecord(RFCOMM_UUID);
            bluetoothSocket.connect();
            Toast.makeText(this, "Connected to bluetooth", Toast.LENGTH_SHORT).show();

            OutputStream outputStream = bluetoothSocket.getOutputStream();
            String wifiData = ssid + ":" + password;
            outputStream.write(wifiData.getBytes());

            Toast.makeText(this, "Wi-Fi Credentials Sent!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Wi-Fi Credentials Sent: " + wifiData);

        } catch (IOException e) {
            Log.e(TAG, "Error sending Wi-Fi credentials", e);
            Toast.makeText(this, "Failed to send Wi-Fi Credentials", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (bluetoothSocket != null) {
                    bluetoothSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing Bluetooth socket", e);
            }
        }
    }
}