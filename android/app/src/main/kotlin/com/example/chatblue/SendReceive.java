package com.example.chatblue;

import static com.example.chatblue.MainActivity.STATE_MESSAGE_RECEIVED;

import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.Handler;

public class SendReceive extends  Thread {
    private  final Handler handler;
    private final InputStream inputStream;
    private  final OutputStream outputStream;
    public SendReceive(BluetoothSocket bluetoothSocket, Handler handler){
        this.handler=handler;
        InputStream tempIn=null;
        OutputStream tempOut =  null;
        try{
            tempIn=bluetoothSocket.getInputStream();
            tempOut=bluetoothSocket.getOutputStream();

        }catch (IOException e){
            e.printStackTrace();
        }
        inputStream=tempIn;
        outputStream=tempOut;
    }
    public  void run(){
        byte[] buffer = new byte[1024];
        int bytes;
        while (true){
            try {
//                Received Message
               bytes= inputStream.read(buffer);
                handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public  void write(byte[] bytes){
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
