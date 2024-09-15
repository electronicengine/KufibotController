package com.example.kufibotcontroller;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class SettingsActivity extends AppCompatActivity {

    private EditText controllerSocketEditText;
    private EditText videoSocketEditText;
    private Button saveButton;
    private Button getInfoButton;
    private Handler handler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        // Initialize views
        controllerSocketEditText = findViewById(R.id.controller_socket);
        videoSocketEditText = findViewById(R.id.video_socket);
        saveButton = findViewById(R.id.save_button);
        getInfoButton = findViewById(R.id.get_info_button);

        // Initialize your TextView components
        TextView rightArmTextView = findViewById(R.id.right_arm);
        TextView leftArmTextView = findViewById(R.id.left_arm);
        TextView neckDownTextView = findViewById(R.id.neck_down);
        TextView neckUpTextView = findViewById(R.id.neck_up);
        TextView neckRightTextView = findViewById(R.id.neck_right);
        TextView eyeLeftTextView = findViewById(R.id.eye_left);
        TextView eyeRightTextView = findViewById(R.id.eye_right);

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
                String videoSocket = videoSocketEditText.getText().toString();

                saveSettings(controllerSocket, videoSocket);
            }
        });

        getInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                WebSocketControllerClient controllerClient = WebSocketControllerClient.getInstance();
                if(controllerClient.isConnected())
                    controllerClient.send("Get Info");

                controllerClient.setMessageListener(new WebSocketControllerClient.MessageListener() {
                    @Override
                    public void onMessage(String message) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(message);

                            int rightArm = jsonObject.getInt("right_arm");
                            int leftArm = jsonObject.getInt("left_arm");
                            int neckDown = jsonObject.getInt("neck_down");
                            int neckUp = jsonObject.getInt("neck_up");
                            int neckRight = jsonObject.getInt("neck_right");
                            int eyeLeft = jsonObject.getInt("eye_left");
                            int eyeRight = jsonObject.getInt("eye_right");

                            // Update UI elements on the main thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    rightArmTextView.setText("Right Arm: " + rightArm);
                                    leftArmTextView.setText("Left Arm: " + leftArm);
                                    neckDownTextView.setText("Neck Down: " + neckDown);
                                    neckUpTextView.setText("Neck Up: " + neckUp);
                                    neckRightTextView.setText("Neck Right: " + neckRight);
                                    eyeLeftTextView.setText("Eye Left: " + eyeLeft);
                                    eyeRightTextView.setText("Eye Right: " + eyeRight);
                                }
                            });

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }



                    }
                });
            }
        });


        setFullScreen();
    }

    private void saveSettings(String controllerSocket, String videoSocket) {
        String controllerSocketText = controllerSocketEditText.getText().toString();
        String videoSocketText = videoSocketEditText.getText().toString();
        WebSocketControllerClient controllerClient = WebSocketControllerClient.getInstance();
        WebSocketVideoClient videoClient = WebSocketVideoClient.getInstance();


        Runnable delayedTask = new Runnable() {
            @Override
            public void run() {
                Log.d("Controller WebSocket", controllerSocketText);
                controllerClient.connect(controllerSocketText);

                Log.d("WebSocket", videoSocketText);
                videoClient.connect(videoSocketText);
                // Code to execute after the delay
                Log.d("DelayedTask", "This message is logged after a 1-second delay.");
            }
        };

        if(videoClient.isConnected()){
            videoClient.disconnect();
        }

        if(controllerClient.isConnected()){
            controllerClient.disconnect();
        }

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