package com.example.fabcatv2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


public class bluetooth extends Fragment {
    //UUID
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //Bluetooth Connection
    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> mBTDevice;
    ArrayList<String> mBTDeviceExtras;

    //Layout
    Button btnONOFF;
    Button btnConnect;
    ListView lvNewDevices;
    View v;
    int deviceSelected = -1;

    public bluetooth() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Layout
        v = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        btnONOFF = v.findViewById(R.id.OnOffBtn);
        btnConnect = v.findViewById(R.id.btnConn);
        lvNewDevices = v.findViewById(R.id.list_device);

        //Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        //check Bluetooth Status
        if (bluetoothAdapter.isEnabled()) {
            btnONOFF.setText(R.string.off_bluetooth);
            printPairedDevice();
        } else {
            btnONOFF.setText(R.string.on_bluetooth);
        }

        //Listeners
        btnONOFF.setOnClickListener(view -> enabledisableBT());

        btnConnect.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d("TODO","Permission Fix");
            }
            //Log.d("TAG", String.valueOf(deviceSelected)+" "+mBTDevice.get(deviceSelected).getName());
            if (deviceSelected >= 0 && bluetoothAdapter.isEnabled()) {
                startConnection(mBTDevice.get(deviceSelected), MY_UUID_INSECURE);
            }

        });
        lvNewDevices.setOnItemClickListener((adapterView, view, i, l) -> deviceSelected = i);


        return v;
    }


    public void enabledisableBT() {

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            requireActivity().registerReceiver(broadcastReceiver, BTIntent);


        }
        if (bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d("LOG", "ERROR NO PERMISSION");

            }

            bluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            requireActivity().registerReceiver(broadcastReceiver, BTIntent);
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        btnONOFF.setText(R.string.on_bluetooth);
                        btnConnect.setEnabled(false);
                        clearPairedDevice();
                        Log.d("LOG", "STATE_OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("LOG", "STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        btnONOFF.setText(R.string.off_bluetooth);
                        btnConnect.setEnabled(true);
                        printPairedDevice();
                        Log.d("LOG", "STATE_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("LOG", "STATE_TURNING_ON");
                        break;
                }

            }
        }
    };

    private void printPairedDevice() {
        mBTDeviceExtras = new ArrayList<>();
        mBTDevice = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_list, mBTDeviceExtras);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TODO","Permission Fix");

        }
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            mBTDevice.add(device);
            mBTDeviceExtras.add(" " + device.getName());

        }
        lvNewDevices.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void clearPairedDevice() {
        mBTDeviceExtras = new ArrayList<>();
        mBTDevice = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_list, mBTDeviceExtras);
        lvNewDevices.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void startConnection(BluetoothDevice device, UUID uuid) {
        Log.d("LOG", "startBTConnection: Initializing Bluetooth Connection.");
        Date now = new Date(System.currentTimeMillis() + 10000);
        makeToast("Attempting to connect to FabCat");
        BluetoothConnectionService.begin();
        BluetoothConnectionService.startClient(device, uuid);
        btnConnect.setEnabled(false);

        Thread wait = new Thread() {
            @Override
            public void run() {
                super.run();
                while(now.after(new Date(System.currentTimeMillis())));
                Looper.prepare();
                if (BluetoothConnectionService.isConnected()) {
                    makeToast("Successfully connected");
                } else {
                    makeToast("Connection failed, try On/Off Bluetooth");
                    BluetoothConnectionService.cancel();
                }
                Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {

                        btnConnect.setEnabled(true);
                    }
                };
                handler.sendEmptyMessage(1);

                Looper.loop();
            }
        };
        wait.start();
    }

    void makeToast(String text) {
        Toast.makeText(MainActivity.getAppContext(), text, Toast.LENGTH_SHORT).show();

    }
}