//package com.example.chatblue;
//
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothServerSocket;
//import android.bluetooth.BluetoothSocket;
//import android.content.Context;
//import android.content.pm.PackageManager;
//
//import androidx.core.app.ActivityCompat;
//
//import java.io.IOException;
//import java.util.UUID;
//
//import io.flutter.Log;
//import io.flutter.plugin.common.MethodChannel;
//
//
//class BluetoothServer extends Thread {
//    private static final String TAG = "MY_APP_DEBUG_TAG";
//    String appName = "ChatBlue";
//    SendReceive sendReceive;
//    BluetoothServerSocket serverSocket;
//    MethodChannel METHOD_CHANNEL;
//
//
//    @SuppressLint("MissingPermission")
//    public BluetoothServer(String uuid, MethodChannel METHOD_CHANNEL, Context context) {
//        try {
//            this.METHOD_CHANNEL = METHOD_CHANNEL;
//            serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(appName, UUID.fromString(uuid));
//            Log.d("info", "Starting Next");
//        } catch (IOException e){
//            Log.d("error", e.toString());
//        }
//
//    }
//    BluetoothSocket socket = null;
//    public void run() {
//
//        while(socket==null){
//            try {
//                // Sending message to handler Connecting
//                Log.d("info", "Inside server socket");
//                socket = serverSocket.accept();
////                Log.d("info", "Server Started");
////                METHOD_CHANNEL.invokeMethod("connection", "Client Connected");
//            }catch (IOException e){
//                // Sending message to handler
//                Log.d("error", e.toString());
//            }
//            if(socket!=null){
//                // Now it is connected
//                sendReceive = new SendReceive(socket, METHOD_CHANNEL);
//                sendReceive.start();
////                try{
////
////                }
////                METHOD_CHANNEL.invokeMethod("connection", "Socket Connected");
//                Log.d("message", "Connected");
//            }
//        }
//    }
//
//    public void cancel() {
//        try {
//            serverSocket.close();
//        } catch (IOException e) {
//            Log.e(TAG, "Could not close the connect socket", e);
//        }
//    }
//}
////}