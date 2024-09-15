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
import android.widget.Toast;

public class WebSocketVideoClient {

    private WebSocket webSocket;
    private OkHttpClient client;
    private ImageView imageView;
    private Context context;  // Add context to show Toast messages
    private static WebSocketVideoClient instance;
    static private String connectionUrl;
    private boolean connected = false;


    // Public method to provide access to the singleton instance
    public static synchronized WebSocketVideoClient getInstance() {
        if (instance == null) {
            instance = new WebSocketVideoClient();
        }
        return instance;
    }

    private WebSocketVideoClient() {
        client = new OkHttpClient();
    }

    public void setConnectionUrl(String Url){
        connectionUrl = Url;
    }

    public void setImageView(ImageView imageView){
        this.imageView = imageView;

    }

    public void setContext(Context context){
        this.context = context;  // Initialize context
    }

    private WebSocketVideoClient(ImageView imageView, Context context) {
        client = new OkHttpClient();

    }

    public boolean isConnected(){
        return connected;
    }

    public void connect(String url) {
        if (url == null || url.isEmpty()) {
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
                Log.d("WebSocket", "Connected to the server");
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