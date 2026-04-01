package com.example.fabcatv2;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class main extends Fragment {
    //Bluetooth

    //Motor values
    static public int[] motorsValue = {90, 90, 90, 50, 50, 50, 50, 120, 120, 120, 120};
    static public final int[][] motorValueConstants = {
             {90, 90, 90, 50, 50, 50, 50, 120, 120, 120, 120},
            {90, 150, 120, 20, 18, 20, 20, 168, 170, 168, 168},
            {90, 130, 0, 30, 30, 30, 30, 30, 30, 120, 120},
            {90, 90, 90, 80, 80, 80, 80, 40, 40, 40, 40},
            {65, 90, 30, 70, 144, 0, 0, 65, 45, 150, 175},
            {90, 90, 90, 90, 90, 90, 90, 105, 105, 105, 105}};
    static private final int MAX_VALUE_MOTOR = 180;
    static private final int MIN_VALUE_MOTOR = 0;
    int selectedMotor = -1;

    //Layout Setup
    Chip chip;

    public main() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Layout Initialize
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        Button buttonPlus = v.findViewById(R.id.increase_button);
        Button buttonMinus = v.findViewById(R.id.decrease_button);
        TextView singleMotorValue = v.findViewById(R.id.valueMotor);
        singleMotorValue.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));
        ChipGroup chipGroup = v.findViewById(R.id.chip_group);
        TextView tvinfoMotor = v.findViewById(R.id.info_textview);
        tvinfoMotor.setTextColor(ContextCompat.getColor(MainActivity.getAppContext(), R.color.black));

        //Listener for Chip selection
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            chip = v.findViewById(chipGroup.getCheckedChipId());

            if (chip != null) {
                if (chip.getId() != R.id.chip11) {
                    tvinfoMotor.setText(String.format("%sselected", chip.getText()));
                    selectedMotor = Integer.parseInt((String.valueOf(chip.getText())).replaceAll("\\D+", "")) - 1;

                } else {
                    tvinfoMotor.setText(String.format("%sselected", chip.getText()));
                    if (BluetoothConnectionService.isConnected())
                        MainActivity.sendFunction(0);
                }
            }
        });
        //Listener for Plus button
        buttonPlus.setOnClickListener(view -> {
            if (BluetoothConnectionService.isConnected()) {
                if (chip != null && chip.getId() != R.id.chip11) {
                    motorsValue[selectedMotor] += MainActivity.multiplierSpeed;
                    if (motorsValue[selectedMotor] > MAX_VALUE_MOTOR) {
                        motorsValue[selectedMotor] = 180;
                        MainActivity.makeToast("Max Motor Value is 180!");
                    }
                    singleMotorValue.setText(String.valueOf(motorsValue[selectedMotor]));
                    MainActivity.setMotor(selectedMotor, motorsValue[selectedMotor]);
                }else{
                    MainActivity.makeToast("Select a motor");
                }
            }else{
                MainActivity.makeToast("No Bluetooth connection");
            }

        });
        //Listener for Less button
        buttonMinus.setOnClickListener(view -> {
            if (BluetoothConnectionService.isConnected()) {
                if (chip != null && chip.getId() != R.id.chip11) {
                    motorsValue[selectedMotor] -= MainActivity.multiplierSpeed;
                    if (motorsValue[selectedMotor] < MIN_VALUE_MOTOR) {
                        motorsValue[selectedMotor] = 0;
                        MainActivity.makeToast("Min Motor Value is 0!");
                    }
                    singleMotorValue.setText(String.valueOf(motorsValue[selectedMotor]));
                    MainActivity.setMotor(selectedMotor, motorsValue[selectedMotor]);
                }else{
                    MainActivity.makeToast("Select a motor");
                }
            }else{
                MainActivity.makeToast("No Bluetooth connection");
            }
        });

        return v;
    }

}