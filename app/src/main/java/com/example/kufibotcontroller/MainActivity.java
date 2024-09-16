package com.example.kufibotcontroller;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import android.view.View;
import android.view.MenuItem;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private WebSocketVideoClient webSocketVideoClient;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        WebSocketVideoClient videoClient = WebSocketVideoClient.getInstance();
        videoClient.setContext(this);
        videoClient.setImageView(imageView);
        videoClient.connect("ws://192.168.1.39:8765");  // Replace with your WebSocket server URL
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

        SeekBar right_arm = findViewById(R.id.right_arm);
        SeekBar left_arm = findViewById(R.id.left_arm);
        Switch rightEyeSwitch = findViewById(R.id.right_eye);
        Switch leftEyeSwitch = findViewById(R.id.left_eye);
        WebSocketControllerClient controllerClient = WebSocketControllerClient.getInstance();

        rightEyeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("Id", "right_eye");
                    jsonObject.put("Angle", isChecked ? 180 : 0);
                    String jsonData = jsonObject.toString();
                    Log.d("joysticks", jsonData);
                    controllerClient.send(jsonData);

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
                    String jsonData = jsonObject.toString();
                    Log.d("joysticks", jsonData);
                    controllerClient.send(jsonData);

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
                    String jsonData = jsonObject.toString();
                    Log.d("joysticks", jsonData);
                    controllerClient.send(jsonData);

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
                    String jsonData = jsonObject.toString();
                    Log.d("joysticks", jsonData);
                    controllerClient.send(jsonData);

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