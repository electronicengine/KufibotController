package com.example.kufibotcontroller;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.Manifest;
import android.media.AudioAttributes;
import android.media.SoundPool;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import android.widget.SeekBar;
import android.widget.Switch;
import android.view.View;
import android.view.MenuItem;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Button;
import java.util.ArrayList;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private WebSocketControllerClient webSocketVideoClient;
    private UDPServerDiscovery discovery;

    private ImageView imageView;
    private static final int SPEECH_REQUEST_CODE = 100;
    private SpeechRecognizer speechRecognizer;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        discovery = new UDPServerDiscovery();

        imageView = findViewById(R.id.imageView);
        final MediaPlayer button_pressed_sound = MediaPlayer.create(this, R.raw.press);
        final MediaPlayer button_released_sound = MediaPlayer.create(this, R.raw.release);

        // Initialize SoundPool with appropriate AudioAttributes
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();


        WebSocketControllerClient controllerClient = WebSocketControllerClient.getInstance();
        controllerClient.setContext(this);
        controllerClient.setImageView(imageView);
        controllerClient.setTextViews(findViewById(R.id.voltage),findViewById(R.id.compass),
                findViewById(R.id.distance), findViewById(R.id.current));

        discovery.discoverServer(new UDPServerDiscovery.DiscoveryCallback() {
            @Override
            public void onServerFound(String ip, int port) {
                runOnUiThread(() -> {
                    // WebSocket'e bağlan
                    controllerClient.connect("ws://" + ip + ":" + port);  // Replace with your WebSocket server URL
                    Toast.makeText(MainActivity.this, "Found: " + ip + ":" + port, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onDiscoveryFailed(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Couldn't Find: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
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
                }else if (id == R.id.nav_bluetooth) {
                    // Open TerminalActivity
                    Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        }

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new SpeechRecognitionListener());

        Button speechButton = findViewById(R.id.speech_button);
        speechButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startSpeechRecognition();
                        Log.d("Speech", "Button pressed - starting recognition.");
                        button_pressed_sound.start();
                        v.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));

                        v.performClick();
                        return true;

                    case MotionEvent.ACTION_UP:
                        stopSpeechRecognition();
                        Log.d("Speech", "Button released - stopping recognition.");
                        button_released_sound.start();
                        v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#673AB7")));

                        v.performClick();
                        return true;

                    default:
                        return false;
                }
            }
        });

        Button aiModeButton = findViewById(R.id.ai_mode_button);
        final boolean[] isAiModeActivated = {false}; // Butonun başlangıç durumu

        aiModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAiModeActivated[0]) {
                    // Buton şu an ON durumunda, OFF yap
                    aiModeButton.setBackgroundTintList(
                            ColorStateList.valueOf(Color.parseColor("#2196F3"))
                    );
                    isAiModeActivated[0] = false;
                    Toast.makeText(MainActivity.this, "AI Mode OFF", Toast.LENGTH_SHORT).show();
                    controllerClient.writeTalkie("switch ai mode off");

                } else {
                    // Buton OFF durumunda, ON yap
                    aiModeButton.setBackgroundTintList(
                            ColorStateList.valueOf(Color.GREEN) // ON rengi
                    );
                    isAiModeActivated[0] = true;
                    Toast.makeText(MainActivity.this, "AI Mode ON", Toast.LENGTH_SHORT).show();
                    controllerClient.writeTalkie("switch ai mode on");
                }
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

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR");  // Turkish language
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);  // Get partial results
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Konuşun...");

        speechRecognizer.startListening(intent);
    }


    private void stopSpeechRecognition() {
        speechRecognizer.stopListening();

    }

    private class SpeechRecognitionListener implements RecognitionListener {

        private void showToast(String message) {
            TextView compass = findViewById(R.id.compass);

            runOnUiThread(() -> {
                Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 1000, 1000); // Adjust the position
                toast.show();

            });
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d("Speech", "Ready for speech.");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d("Speech", "Speech started.");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // Optional: Handle volume level changes
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // Optional: Handle audio buffer
        }

        @Override
        public void onEndOfSpeech() {
            Log.d("Speech", "Speech ended.");
        }

        @Override
        public void onError(int error) {
            Log.e("Speech Error", "Error: " + error);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String recognizedText = matches.get(0);
                Log.d("Speech Result", "Recognized text: " + recognizedText);
                showToast(recognizedText);
                WebSocketControllerClient client = WebSocketControllerClient.getInstance();
                client.writeTalkie(recognizedText);
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                Log.d("Speech Partial", "Partial: " + matches.get(0));

            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // Optional: Handle other events
        }
    }
}

