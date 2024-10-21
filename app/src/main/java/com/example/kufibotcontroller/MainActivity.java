package com.example.kufibotcontroller;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.SeekBar;
import android.widget.Switch;
import android.view.View;
import android.view.MenuItem;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private WebSocketControllerClient webSocketVideoClient;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        WebSocketControllerClient controllerClient = WebSocketControllerClient.getInstance();
        controllerClient.setContext(this);
        controllerClient.setImageView(imageView);
        controllerClient.setTextViews(findViewById(R.id.voltage),findViewById(R.id.compass),
                findViewById(R.id.distance), findViewById(R.id.current));
        controllerClient.connect("ws://192.168.1.44:8765");  // Replace with your WebSocket server URL
        setFullScreen();

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_settings) {
                    // Open SettingsActivity
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }else if (id == R.id.nav_terminal) {
                    // Open TerminalActivity
                    Intent intent = new Intent(MainActivity.this, TerminalActivity.class);
                    startActivity(intent);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        SeekBar right_arm = findViewById(R.id.info);
        SeekBar left_arm = findViewById(R.id.left_arm);
        Switch rightEyeSwitch = findViewById(R.id.right_eye);
        Switch leftEyeSwitch = findViewById(R.id.left_eye);

        rightEyeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("Id", "right_eye");
                    jsonObject.put("Angle", isChecked ? 180 : 0);
                    controllerClient.writeEyeControlData("right_eye", jsonObject);

                } catch (JSONException e) {
                    Log.e("JSON Error", "Failed to create JSON object: " + e.getMessage());
                }
            }
        });

        leftEyeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("Id", "left_eye");
                    jsonObject.put("Angle", isChecked ? 180 : 0);
                    controllerClient.writeEyeControlData("left_eye", jsonObject);

                } catch (JSONException e) {
                    Log.e("JSON Error", "Failed to create JSON object: " + e.getMessage());
                }
            }
        });


        right_arm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("Id", "right_arm");
                    jsonObject.put("Angle", progress);
                    controllerClient.writeMotionControlData("right_arm", jsonObject);

                } catch (JSONException e) {
                    Log.e("JSON Error", "Failed to create JSON object: " + e.getMessage());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        left_arm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("Id", "left_arm");
                    jsonObject.put("Angle", progress);
                    controllerClient.writeArmControlData( "left_arm", jsonObject);

                } catch (JSONException e) {
                    Log.e("JSON Error", "Failed to create JSON object: " + e.getMessage());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
    protected void onDestroy() {
        super.onDestroy();
        // Close WebSocket connection when the activity is destroyed
        if (webSocketVideoClient != null) {
            webSocketVideoClient.disconnect();
        }
    }
}