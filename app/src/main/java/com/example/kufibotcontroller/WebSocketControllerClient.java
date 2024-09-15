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

public class WebSocketControllerClient {

    private WebSocket webSocket;
    private OkHttpClient client;
    static private boolean connected = false;
    static private String connectionUrl;

    // Private static instance of the class
    private static WebSocketControllerClient instance;
    private MessageListener messageListener;


    // Private constructor to prevent instantiation
    private WebSocketControllerClient() {
        client = new OkHttpClient();
    }

    // Listener interface
    public interface MessageListener {
        void onMessage(String message);
    }

    // Method to set the listener
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    // Call this method when a message is received
    private void notifyMessageReceived(String message) {
        if (messageListener != null) {
            messageListener.onMessage(message);
        }
    }

    // Public method to provide access to the singleton instance
    public static synchronized WebSocketControllerClient getInstance() {
        if (instance == null) {
            instance = new WebSocketControllerClient();
        }
        return instance;
    }

    public boolean isConnected(){
        return connected;
    }

    public void connect(String url) {
        if(!connected){
            connected = true;
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
                    Log.d("Controller WebSocket", "Connected to the server");
                    connected = true;
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    Log.d("Controller WebSocket", "Received: " + text);
                    notifyMessageReceived(text);  // Notify the listener with the received message
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    Log.e("Controller WebSocket", "Connection error: " + t.getMessage());
                    connected = false;
                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    Log.d("Controller WebSocket", "Closing: " + code + " / " + reason);
                    webSocket.close(1000, null);
                    connected = false;
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    Log.d("Controller WebSocket", "Closed: " + code + " / " + reason);
                    connected = false;
                }
            });

            client.dispatcher().executorService().shutdown();

        }

    }

    public void setConnectionUrl(String Url){
        connectionUrl = Url;
    }

    public void disconnect() {
        if (webSocket != null && connected) {
            webSocket.close(1000, "Disconnecting");
            connected = false;
            webSocket = null;
        }
    }

    public void send(String message) {
        if (webSocket != null && connected) {
            Log.d("Controller WebSocket", "Sending: " + message);

            webSocket.send(message);
        }
    }

}