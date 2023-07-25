package com.example.chatblue;


import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import io.flutter.Log;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.example.chatblue";
    String uuid = "e287a22c-29f6-48e1-9d49-72e5a63d47a9";
    private static MethodChannel METHOD_CHANNEL;
    SendReceive sendReceive;
    // Bluetooth Related ==============
    static BluetoothAdapter bluetoothAdapter;

    //    ================== Handler ========================
    static BluetoothClient bluetoothClient;
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    static final int STATE_DISCONNECTED = 6;
    static private boolean isConnected = false;
    static private boolean isListening = false;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case STATE_LISTENING:
                    isListening = true;
                    METHOD_CHANNEL.invokeMethod("btState", "4");
                    break;
                case STATE_CONNECTING:
                    METHOD_CHANNEL.invokeMethod("btState", "1");
                    break;
                case STATE_CONNECTED:
                    isConnected = true;
                    METHOD_CHANNEL.invokeMethod("btState", "2");
                    break;
                case STATE_CONNECTION_FAILED:
                    isConnected = false;
                    METHOD_CHANNEL.invokeMethod("connection", "Failed to connect");
                    METHOD_CHANNEL.invokeMethod("btState", "12");
                    break;
                case STATE_DISCONNECTED:
                    isConnected = false;
                    METHOD_CHANNEL.invokeMethod("connection", "Disconnected");
                    METHOD_CHANNEL.invokeMethod("btState", "12");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    METHOD_CHANNEL.invokeMethod("message", tempMsg);
                    break;
            }
            return true;
        }
    });
    static BluetoothServer bluetoothServer;

    static final int BLUETOOTH_TOGGLE = 12;

    @SuppressLint("MissingPermission")
    private HashMap<String, String> toggleBluetooth() {
        Log.d("info", "Toggling");
        HashMap<String, String> data = new HashMap<>();
        if (bluetoothAdapter == null) {
            data.put("message", "Bluetooth Not Supported");
            return data;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_TOGGLE);
            data.put("message", "Enabled");
        } else {
            bluetoothAdapter.disable();
            data.put("message", "Disabled");
        }
        return data;

    }

    @SuppressLint("MissingPermission")
    private ArrayList<HashMap<String, String>> getDevices() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        Object[] deviceSet = bluetoothAdapter.getBondedDevices().toArray();
        for (Object o : deviceSet) {
            BluetoothDevice d = (BluetoothDevice) o;
            HashMap<String, String> info = new HashMap<>();
            info.put("name", d.getName());
            info.put("address", d.getAddress());
            list.add(info);
        }
        return list;
    }

    boolean SendMessage(String message) {
        Log.d("info", message);
        sendReceive.write(message.getBytes());
        return true;
    }

    private boolean startServer() {
        if (!isListening) {
            Log.d("info", "Starting Server");

            bluetoothServer = new BluetoothServer(uuid);
            bluetoothServer.start();

        }
        return true;
    }

    @SuppressLint("MissingPermission")
    private boolean connectClient(String address) {
        BluetoothDevice currentDevice = null;
        Object[] adapters = bluetoothAdapter.getBondedDevices().toArray();
        for (Object adapter : adapters) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) adapter;
            if (bluetoothDevice.getAddress().equals(address)) {
                currentDevice = bluetoothDevice;
                break;
            }
        }
        if (currentDevice != null) {
            try {
                 bluetoothClient = new BluetoothClient(currentDevice);
                bluetoothClient.start();
            } catch (Exception e) {
                e.printStackTrace();

            }

            return true;
        }
        return false;
    }


//    ==============================================================================
    //                                  Bluetooth Server
    private static final String TAG = "MY_APP_DEBUG_TAG";
    class BluetoothServer extends Thread {

        String appName = "ChatBlue";
        BluetoothServerSocket serverSocket;


        @SuppressLint("MissingPermission")
        public BluetoothServer(String uuid) {
            try {

                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(appName, UUID.fromString(uuid));
                Log.d("info", "Starting Next");
            } catch (IOException e) {
                Log.d("error", e.toString());
            }

        }

        BluetoothSocket socket = null;

        public void run() {

            while (socket == null) {
                try {
                    // Sending message to handler Connecting
                    Log.d("info", "Inside server socket");
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                    Log.d("info", "Server Started");
                } catch (IOException e) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                    Log.d("error", e.toString());
                }
                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    sendReceive = new SendReceive(socket, handler);
                    sendReceive.start();
                    Log.d("message", "Connected");
                }
            }
        }



    }

    // =====================================================================


// ==================================================================================
    /////////                           Bluetooth Client
    // ====================================================================

    public class BluetoothClient extends Thread {
        private BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public BluetoothClient(BluetoothDevice bluetoothDevice) {

            Log.d("info", "Bluetooth Address " + bluetoothDevice.getAddress());
            try {
                socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public void run() {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);
                sendReceive = new SendReceive(socket, handler);
                sendReceive.start();
                Log.d("info", "Client Connected");
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }

    }

    /////=================================================================


    BroadcastReceiver blueToothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int value = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                METHOD_CHANNEL.invokeMethod("btState", String.valueOf(value));

//            if( == bluetoothAdapter.STATE){
//                Log.d("info", "Disabled");
//                METHOD_CHANNEL.invokeMethod("btState", "0");
//            }else{
//                Log.d("info", "Enabled");
//                METHOD_CHANNEL.invokeMethod("btState", "1");
//
//
//            }
            }
        }
    };


    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);


        METHOD_CHANNEL = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
        METHOD_CHANNEL.setMethodCallHandler(
                (call, result) -> {
                    String method = call.method;
                    switch (method) {
                        case "getDevices":
                            result.success(getDevices());
                            break;
                        case "toggle":
                            result.success(toggleBluetooth());
                            break;
                        case "start-server":
                            result.success(startServer());
                            break;
                        case "connect-client":
                            String address = call.arguments();
                            result.success(connectClient(address));
                            break;
                        case "send-message":
                            String message = call.arguments();
                            boolean isSent = false;
                            try {
                                isSent= SendMessage(message);
                            }catch (Exception e){
                                Log.d("info", e.toString());
                            }

                            result.success(isSent);
                        case "getInfo":
                            HashMap<String, Object> info = new HashMap<>();

                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                info.put("name", bluetoothAdapter.getName());
                            }
                            info.put("isEnabled", bluetoothAdapter.isEnabled());
                            result.success(info);
                            break;
                        case "disconnect":
                            try {

                                if(bluetoothServer!=null){
                                    Log.d("info", "Closing Bluetooth Server");
                                    bluetoothServer.interrupt();
                                    bluetoothServer=null;
                                }
                                if(bluetoothClient!=null){
                                    bluetoothServer.interrupt();
                                    bluetoothClient=null;
                                }
                                sendReceive.interrupt();
//                                Message message1 = Message.obtain();
//                                message1.what = STATE_DISCONNECTED;
//                                handler.sendMessage(message1);
                            }catch (Exception e){
    Log.d("info", e.toString());
                            }


                            result.success(true);
    break;
                        case "pair":
                            String btAddress = call.arguments();
                            try {
                                BluetoothDevice device =   bluetoothAdapter.getRemoteDevice(btAddress);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    device.createBond();
                                }
                            }catch (Exception er){
                                Log.d("info", er.getMessage());
                            }

                            result.success("true");
                            break;
                    }
                }
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(blueToothStateReceiver, filter);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(blueToothStateReceiver);
    }
}

