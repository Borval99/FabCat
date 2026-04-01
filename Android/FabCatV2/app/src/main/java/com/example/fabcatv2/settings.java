package com.example.fabcatv2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Date;



public class settings extends Fragment implements SensorEventListener {

    Integer msValueInt;
    static Boolean runPR = false;
    static Boolean runPRW = false;
    private SensorManager sensorManager;
    private Boolean sendPitchRollstate = false;
    public settings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        //Layout
        msValueInt = 200;
        TextView tvPR = v.findViewById(R.id.PitchRoll);
        SwitchMaterial swPR = v.findViewById(R.id.PitchRollBtn);
        TextView tvPRW = v.findViewById(R.id.PitchRollWrite);
        SwitchMaterial swPRW = v.findViewById(R.id.PitchRollWriteBtn);
        TextView tvBra =  v.findViewById(R.id.AutoStr);
        SwitchMaterial swBra =  v.findViewById(R.id.AutoStrBtn);
        TextView tvBal = v.findViewById(R.id.AutoBal);
        SwitchMaterial swBal = v.findViewById(R.id.AutoBalBtn);
        TextView tvPitch = v.findViewById(R.id.Pitch);
        TextView tvRoll = v.findViewById(R.id.Roll);
        TextView tvPitchVal = v.findViewById(R.id.PitchVal);
        TextView tvRollVal = v.findViewById(R.id.RollVal);
        tvPRW.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));
        swPRW.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));
        tvPR.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));
        tvBra.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));
        tvBal.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));
        tvPitch.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));
        tvRoll.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));
        tvPitch.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));
        tvPitchVal.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));
        tvRollVal.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));

        sensorManager = (SensorManager) MainActivity.getAppContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(this, sensor,SensorManager.SENSOR_DELAY_NORMAL);
        swPR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (BluetoothConnectionService.isConnected()) {
                    if (isChecked) {
                        Thread print = new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                while (runPR) {
                                    String[] splittedValue = BluetoothConnectionService.readedValue.split("222");
                                    if (splittedValue.length > 1) {
                                        StringBuilder resultString = new StringBuilder();
                                        for (int i = 1; i < splittedValue.length; i++) {
                                            resultString.append(splittedValue[i]);
                                        }
                                        BluetoothConnectionService.readedValue = resultString.toString();

                                        Handler handler = new Handler(Looper.getMainLooper()) {
                                            @Override
                                            public void handleMessage(Message msg) {
                                                try {
                                                    if (Integer.parseInt(splittedValue[0].substring(0, 3)) <= 180 && Integer.parseInt(splittedValue[0].substring(3, 6)) <= 180) {
                                                        tvPitchVal.setText(splittedValue[0].substring(0, 3));
                                                        tvRollVal.setText(splittedValue[0].substring(3, 6));
                                                    }
                                                } catch (Exception e) {
                                                    //  e.printStackTrace();
                                                }
                                            }
                                        };
                                        handler.sendEmptyMessage(1);
                                    }
                                }
                            }
                        };
                        print.start();
                        MainActivity.sendCommand(1, 8);
                        runPR = true;
                    } else {
                        MainActivity.sendCommand(0);
                        runPR = false;
                    }
                } else {
                    swPR.setChecked(false);
                    makeToast("No Bluetooth connection");
                    runPR = false;
                }
            }
        });
        swBra.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (BluetoothConnectionService.isConnected()) {
                if (isChecked) {
                    MainActivity.sendCommand(11);
                } else {
                    MainActivity.sendCommand(10);
                }
            } else {
                swBra.setChecked(false);
                makeToast("No Bluetooth connection");
            }
        });
        swBal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (BluetoothConnectionService.isConnected()) {
                if (isChecked) {
                    MainActivity.sendCommand(21);
                } else {
                    MainActivity.sendCommand(20);
                }
            } else {
                swBal.setChecked(false);
                makeToast("No Bluetooth connection");
            }
        });
        swPRW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (BluetoothConnectionService.isConnected()) {
                    if (sensorManager.getDefaultSensor((Sensor.TYPE_GRAVITY)) != null) {
                        if (isChecked) {

                            MainActivity.sendFunctionWrite(1);
                            runPRW = true;

                            Thread write = new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    while (runPRW) {
                                        Date now = new Date(System.currentTimeMillis()+200);
                                        while(now.after(new Date(System.currentTimeMillis())));
                                        if (BluetoothConnectionService.isConnected()) {
                                            sendPitchRollstate = true;
                                        } else {
                                            makeToast("No Bluetooth connection");
                                            runPRW = false;
                                        }

                                    }
                                }
                            };
                            write.start();

                        } else {
                            MainActivity.sendFunctionWrite(0);
                            runPRW = false;
                        }
                    } else {
                        swPRW.setChecked(false);
                        makeToast("No Pitch/Roll Sensor");
                    }
                } else {
                    swPRW.setChecked(false);
                    makeToast("No Bluetooth connection");
                }
            }
        });
        return v;
    }

    void makeToast(String text) {
        Toast.makeText(MainActivity.getAppContext(), text, Toast.LENGTH_SHORT).show();

    }

    static void offAll() {
        if (BluetoothConnectionService.isConnected()) {
            runPR = false;
            runPRW = false;
            MainActivity.sendCommand(20);
            MainActivity.sendCommand(10);
            MainActivity.sendCommand(0);
            MainActivity.sendFunctionWrite(0);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        offAll();
        sensorManager.unregisterListener(this);
    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sendPitchRollstate) {
            MainActivity.sendPitchRoll((int) ((sensorEvent.values[0] * 10)/10)*9+90, (int) ((sensorEvent.values[1] * 10)/10)*9+90);
            sendPitchRollstate = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}