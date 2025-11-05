package com.example.kufibotcontroller;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Locale;


public class WebSocketControllerClient {

    private WebSocket webSocket;
    private OkHttpClient client;
    private ImageView imageView;
    private TextView voltageText;
    private TextView compassText;
    private TextView distanceText;
    private TextView currentText;
    private TextView infoText;


    private Context context;  // Add context to show Toast messages
    private static WebSocketControllerClient instance;
    static private String connectionUrl;
    private boolean connected = false;
    private volatile JSONObject responseJson = new JSONObject();

    // Public method to provide access to the singleton instance
    public static synchronized WebSocketControllerClient getInstance() {
        if (instance == null) {
            instance = new WebSocketControllerClient();
        }
        return instance;
    }

    private WebSocketControllerClient() {
        client = new OkHttpClient();
    }

    public void setConnectionUrl(String Url){
        connectionUrl = Url;
    }

    public void setImageView(ImageView imageView){
        this.imageView = imageView;
    }

    public void setTextViews(TextView voltageText, TextView compassText, TextView distanceText, TextView currentText){
        this.voltageText = voltageText;
        this.compassText = compassText;
        this.distanceText = distanceText;
        this.currentText = currentText;
    }

    public void  setInfoView(TextView infoView){
        this.infoText = infoView;
    }


    public void setContext(Context context){
        this.context = context;  // Initialize context
    }

    private WebSocketControllerClient(ImageView imageView, Context context) {
        client = new OkHttpClient();

    }

    public boolean isConnected(){
        return connected;
    }

    public void connect(String url) {
        if (url == null || url.isEmpty() || connected) {
            Log.e("WebSocket", "Invalid URL");
            return;
        }
        if (client == null || client.dispatcher().executorService().isShutdown()) {
            client = new OkHttpClient(); // Reinitialize OkHttpClient
        }
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                connected = true;
                showToast("Connected to the server");
                Log.d("WebSocket", "Connected to the server: " + url );
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // Convert the byte data into a bitmap and display it in the ImageView
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes.toByteArray(), 0, bytes.size());

                // Update the ImageView on the main thread
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Maintains aspect ratio, fills the ImageView, and crops if necessary

                    }
                });

            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // null veya boş metin kontrolü
                if (text == null || text.trim().isEmpty()) {
                    Log.w("WebSocket", "Received empty or null message, ignoring...");

                    if (responseJson != null) {
                        webSocket.send(responseJson.toString());
                        responseJson = new JSONObject();
                    }
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(text);

                    // Alt objelerin varlığını kontrol et (ek güvenlik)
                    JSONObject powerData = jsonObject.optJSONObject("power");
                    JSONObject compassData = jsonObject.optJSONObject("compass");
                    JSONObject distanceData = jsonObject.optJSONObject("distance");
                    JSONObject jointData = jsonObject.optJSONObject("joint_angles");

                    if (powerData == null || compassData == null || distanceData == null || jointData == null) {
                        Log.w("WebSocket", "Some JSON fields missing in message: " + text);
                        return;
                    }

                    double voltage = powerData.optDouble("BusVoltage", 0.0);
                    double current = powerData.optDouble("BusCurrent", 0.0);
                    double compass = compassData.optDouble("angle", 0.0);
                    double distance = distanceData.optDouble("Distance", 0.0);

                    voltageText.post(() -> {
                        String formattedPower = String.format(Locale.getDefault(), "%.2f", voltage);
                        voltageText.setText("Voltage: " + formattedPower + " V");
                    });

                    compassText.post(() -> {
                        String formattedCompass = String.format(Locale.getDefault(), "%.1f", compass);
                        compassText.setText("Compass: " + formattedCompass + " °");
                    });

                    distanceText.post(() -> {
                        distanceText.setText("Distance: " + distance + " cm");
                    });

                    currentText.post(() -> {
                        String formattedCurrent = String.format(Locale.getDefault(), "%.1f", current);
                        currentText.setText("Current: " + formattedCurrent + " mah");
                    });

                    if (infoText != null) {
                        infoText.post(() -> {
                            infoText.setText("Power: \n" + powerData.toString() + "\n\n" +
                                    "Compass: \n" + compassData.toString() + "\n\n" +
                                    "Distance: \n" + distanceData.toString() + "\n\n" +
                                    "Joints: \n" + jointData.toString() + "\n\n");
                        });
                    }

                } catch (JSONException e) {
                    // Sadece uyarı logla, bağlantıyı kesme
                    Log.e("WebSocket", "Invalid JSON message: " + text, e);
                    if (responseJson != null && responseJson.length() > 0) {
                        webSocket.send(responseJson.toString());
                        responseJson = new JSONObject();
                    }
                    return; // RuntimeException fırlatma
                }

                // JSON cevabı varsa gönder
                if (responseJson != null) {
                    webSocket.send(responseJson.toString());
                    responseJson = new JSONObject();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                showToast(t.getMessage());
                Log.e("WebSocket", "Connection error: " + t.getMessage());
                connected = false;
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                showToast("Closing: " + code + " / " + reason);
                Log.d("WebSocket", "Closing: " + code + " / " + reason);
                webSocket.close(1000, null);
                connected = false;
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                showToast("\"Closed: \" + code + \" / \" + reason");
                Log.d("WebSocket", "Closed: " + code + " / " + reason);
                connected = false;
            }
        });

        client.dispatcher().executorService().shutdown();
    }


    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, null);
            connected = false;
            client.dispatcher().executorService().shutdown();
            webSocket = null;
        }
    }

    public void writeMotionControlData(String Id, JSONObject Data){
        try {
            responseJson.put(Id, Data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeArmControlData(String Id, JSONObject Data){
        try {
            responseJson.put(Id, Data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeEyeControlData(String Id, JSONObject Data){
        try {
            responseJson.put(Id, Data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeTalkie(String Text){
        try {
            responseJson.put("talkie", Text);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // Method to show a Toast message
    private void showToast(final String message) {
        if (context != null) {
            // Show Toast on the main thread

            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}