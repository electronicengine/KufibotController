package com.example.kufibotcontroller;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;


public class SettingsActivity extends AppCompatActivity {

    private EditText controllerSocketEditText;
    private Button saveButton;
    private Handler handler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        // Initialize views
        controllerSocketEditText = findViewById(R.id.controller_socket_text);
        saveButton = findViewById(R.id.save_button);
        WebSocketControllerClient controllerClient= WebSocketControllerClient.getInstance();
        // Initialize your TextView components
        TextView infoText = findViewById(R.id.info);
        controllerClient.setInfoView(infoText);
        // Set up the toolbar as the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Handle Save button click
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String controllerSocket = controllerSocketEditText.getText().toString();

                saveSettings(controllerSocket);
            }
        });

        setFullScreen();
    }

    private void saveSettings(String controllerSocket) {
        String controllerSocketText = controllerSocketEditText.getText().toString();
        WebSocketControllerClient controllerClient = WebSocketControllerClient.getInstance();


        Runnable delayedTask = new Runnable() {
            @Override
            public void run() {
                Log.d("Controller WebSocket", controllerSocketText);
                controllerClient.connect(controllerSocketText);
                // Code to execute after the delay
                Log.d("DelayedTask", "This message is logged after a 1-second delay.");
            }
        };


        handler.postDelayed(delayedTask, 3000);

    }

    private void setFullScreen() {
        // Hide status bar and navigation bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the back button press
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}