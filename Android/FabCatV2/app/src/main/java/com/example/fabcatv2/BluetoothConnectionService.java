package com.example.fabcatv2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;


public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothSocket mmSocket;

    private  static BluetoothAdapter mBluetoothAdapter;

    public static String readedValue = "";


    private static BluetoothDevice mmDevice;
    private static UUID deviceUUID;
    private static ConnectedThread mConnectedThread;

    static boolean state = false;

    public BluetoothConnectionService() {
        //empty constructor
    }

    public static void begin(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static boolean isConnected(){

        return state;
    }

    private static class  ConnectThread extends Thread {


        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        @SuppressLint("MissingPermission")
        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_UUID_INSECURE );
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
                state = false;
            }

            mmSocket = tmp;

            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                state = true;
                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                state = false;
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE );
            }

            connected(mmSocket);
        }

    }




    public static void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startClient: Started.");

        ConnectThread mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    public static void cancel() {
        state = false;
        try {
            Log.d(TAG, "cancel: Closing Client Socket.");
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
        }
    }


    private static class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                state = false;
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];

            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes).replaceAll("\\D+", "");
                    BluetoothConnectionService.readedValue = BluetoothConnectionService.readedValue + incomingMessage;
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    state = true;
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }
    }

    private static void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public static void write(byte[] out) {
        try {
            mConnectedThread.write(out);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}























