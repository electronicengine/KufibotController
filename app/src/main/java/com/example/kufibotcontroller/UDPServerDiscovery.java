package com.example.kufibotcontroller;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UDPServerDiscovery {
    private static final int BROADCAST_PORT = 8888;
    private static final int TIMEOUT = 5000;

    public interface DiscoveryCallback {
        void onServerFound(String ip, int port);
        void onDiscoveryFailed(String error);
    }

    public void discoverServer(DiscoveryCallback callback) {
        new Thread(() -> {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                socket.setSoTimeout(TIMEOUT);

                // Discovery mesajı gönder
                String message = "DISCOVER_WEBSOCKET_SERVER";
                byte[] sendData = message.getBytes();

                // Broadcast adresi
                InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        broadcastAddr,
                        BROADCAST_PORT
                );

                socket.send(sendPacket);
                Log.d("Discovery", "Broadcast sent");

                // Yanıt bekle
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                socket.receive(receivePacket);

                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                String serverIp = receivePacket.getAddress().getHostAddress();

                Log.d("Discovery", "Response from " + serverIp + ": " + response);

                // Response formatı: "WEBSOCKET_SERVER:8080"
                if (response.startsWith("WEBSOCKET_SERVER:")) {
                    int port = Integer.parseInt(response.split(":")[1]);
                    callback.onServerFound(serverIp, port);
                }

            } catch (Exception e) {
                Log.e("Discovery", "Discovery failed", e);
                callback.onDiscoveryFailed(e.getMessage());
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        }).start();
    }
}
