//package com.example.chatblue;
//
//import android.annotation.SuppressLint;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//
//import java.io.IOException;
//import java.util.UUID;
//
//import io.flutter.Log;
//import io.flutter.plugin.common.MethodChannel;
//
//public class BluetoothClient extends  Thread {
//    private  BluetoothDevice bluetoothDevice;
//    private   MethodChannel METHOD_CHANNEL;
//    private  BluetoothSocket socket;
//    SendReceive sendReceive;
//    @SuppressLint("MissingPermission")
//    public  BluetoothClient(BluetoothDevice bluetoothDevice, String uuid, MethodChannel METHOD_CHANNEL){
//        this.bluetoothDevice =bluetoothDevice;
//        this.METHOD_CHANNEL =METHOD_CHANNEL;
//        Log.d("info", "Bluetooth Address "+ bluetoothDevice.getAddress());
//        try{
//            socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//    }
//    @SuppressLint("MissingPermission")
//    public  void run(){
//        try{
//            socket.connect();
//        sendReceive = new SendReceive(socket, METHOD_CHANNEL);
//        sendReceive.start();
//        METHOD_CHANNEL.invokeMethod("connection", "Client Connected");
//        Log.d("info", "Client Connected");
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//    }
//
//    public  void write(byte [] bytes){
//        sendReceive.write(bytes);
//    }
//
//}
