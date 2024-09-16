package com.example.kufibotcontroller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;


public class TerminalActivity extends AppCompatActivity {

    private TextView cliOutput;
    private EditText cliInput;
    private ScrollView cliScrollView;
    private Session session;
    private String username = "";
    private String ip = "";
    private String password = "";

    private static final int MAX_OUTPUT_LENGTH = 10000; // Maximum length for the terminal output

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        cliOutput = findViewById(R.id.cliOutput);
        cliInput = findViewById(R.id.cliInput);
        cliScrollView = findViewById(R.id.cliScrollView);

        cliInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    String command = cliInput.getText().toString().trim();
                    appendToOutput("$ " + command);
                    executeCommand(command);
                    cliInput.setText("");  // Clear input field
                    return true;
                }
                return false;
            }
        });

        setFullScreen();
    }


    private void executeCommand(String command) {
        if (command.startsWith("ssh ")) {
            // Handle SSH connection command
            String[] parts = command.split(" ");
            if (parts.length == 4) {
                username = parts[1];
                ip = parts[2];
                password = parts[3];
                connectToSSH();
            } else {
                appendToOutput("\nInvalid SSH command format. Use: ssh <username> <ip> <password>");
            }
        } else if (session == null || !session.isConnected()) {
            appendToOutput("\nNot connected to SSH server.");
        } else {
            new SSHAsyncTask(session, cliOutput).execute(command);
        }

        // Scroll to the bottom to keep latest output visible
        cliScrollView.post(() -> cliScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void connectToSSH() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    if (session != null && session.isConnected()) {
                        session.disconnect();
                    }

                    JSch jsch = new JSch();
                    session = jsch.getSession(username, ip, 22);
                    session.setPassword(password);

                    // Avoid asking for key confirmation
                    session.setConfig("StrictHostKeyChecking", "no");

                    // Connect to the server
                    session.connect();
                    Log.d("TerminalActivity", "Connected to SSH server");
                    appendToOutput("\nConnected to SSH server");
                } catch (Exception e) {
                    Log.e("TerminalActivity", "Error connecting to SSH server: " + e.getMessage(), e);
                    runOnUiThread(() -> appendToOutput("\nError connecting to SSH server: " + e.getMessage()));
                }
                return null;
            }
        }.execute();
    }


    private void appendToOutput(String text) {
        runOnUiThread(() -> {
            if (cliOutput.getText().length() > MAX_OUTPUT_LENGTH) {
                cliOutput.setText(""); // Clear output if it exceeds max length
            }
            cliOutput.append(text + "\n");
            cliScrollView.post(() -> cliScrollView.fullScroll(View.FOCUS_DOWN));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (session != null && session.isConnected()) {
            session.disconnect();
            Log.d("TerminalActivity", "Disconnected from SSH server");
        }
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
}