package com.example.kufibotcontroller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class UDPClient {

    private static final int BUFFER_SIZE = 65535;
    private static final String TAG = "UdpControllerClient";

    private DatagramSocket socket;
    private Thread receiverThread;
    private boolean running = false;

    private ImageView imageView;
    private TextView voltageText, compassText, distanceText, currentText, infoText;
    private Context context;

    private int listenPort = 9000; // Raspberry Pi’nin gönderdiği port
    private static UDPClient instance;

    private UDPClient() {}

    public static synchronized UDPClient getInstance() {
        if (instance == null) {
            instance = new UDPClient();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void setTextViews(TextView voltageText, TextView compassText,
                             TextView distanceText, TextView currentText) {
        this.voltageText = voltageText;
        this.compassText = compassText;
        this.distanceText = distanceText;
        this.currentText = currentText;
    }

    public void setInfoView(TextView infoText) {
        this.infoText = infoText;
    }

    public void setListenPort(int port) {
        this.listenPort = port;
    }


    public void start() {
        if (running) return;

        try {
            socket = new DatagramSocket(listenPort);
            running = true;
            receiverThread = new Thread(this::receiveLoop);
            receiverThread.start();
            showToast("UDP listening on port " + listenPort);
        } catch (SocketException e) {
            Log.e(TAG, "Socket create failed: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (receiverThread != null && receiverThread.isAlive()) {
            receiverThread.interrupt();
        }
    }

    private void receiveLoop() {
        byte[] buffer = new byte[BUFFER_SIZE];

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                int length = packet.getLength();
                byte[] data = new byte[length];
                System.arraycopy(packet.getData(), 0, data, 0, length);

                // Eğer paket JSON metniyse (örneğin sensör verileri)
                String payload = new String(data, StandardCharsets.UTF_8);

                if (payload.trim().startsWith("{")) {
                    handleJson(payload);
                } else {
                    handleImage(data);
                }

            } catch (IOException e) {
                if (running)
                    Log.e(TAG, "Receive error: " + e.getMessage());
            }
        }
    }

    private void handleJson(String text) {
        try {
            JSONObject jsonObject = new JSONObject(text);

            JSONObject powerData = jsonObject.optJSONObject("power");
            JSONObject compassData = jsonObject.optJSONObject("compass");
            JSONObject distanceData = jsonObject.optJSONObject("distance");
            JSONObject jointData = jsonObject.optJSONObject("joint_angles");

            if (powerData == null || compassData == null || distanceData == null || jointData == null) {
                Log.w(TAG, "Some JSON fields missing");
                return;
            }

            double voltage = powerData.optDouble("BusVoltage", 0.0);
            double current = powerData.optDouble("BusCurrent", 0.0);
            double compass = compassData.optDouble("angle", 0.0);
            double distance = distanceData.optDouble("Distance", 0.0);

            if (voltageText != null)
                voltageText.post(() -> voltageText.setText(String.format(Locale.getDefault(),
                        "Voltage: %.2f V", voltage)));

            if (compassText != null)
                compassText.post(() -> compassText.setText(String.format(Locale.getDefault(),
                        "Compass: %.1f °", compass)));

            if (distanceText != null)
                distanceText.post(() -> distanceText.setText(String.format(Locale.getDefault(),
                        "Distance: %.1f cm", distance)));

            if (currentText != null)
                currentText.post(() -> currentText.setText(String.format(Locale.getDefault(),
                        "Current: %.1f mA", current)));

            if (infoText != null)
                infoText.post(() -> infoText.setText("Power: \n" + powerData.toString() + "\n\n" +
                        "Compass: \n" + compassData.toString() + "\n\n" +
                        "Distance: \n" + distanceData.toString() + "\n\n" +
                        "Joints: \n" + jointData.toString()));

        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON: " + text);
        }
    }

    private void handleImage(byte[] data) {
        final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bitmap != null && imageView != null) {
            imageView.post(() -> {
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            });
        } else {
            Log.w(TAG, "Invalid image packet");
        }
    }

    private void showToast(final String message) {
        if (context != null) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        }
    }
}
