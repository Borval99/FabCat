package com.example.fabcatv2;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static Context context;
    private DrawerLayout drawer;
    public static int multiplierSpeed = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Creation of ToolBAr and fragment pages
        MainActivity.context = getApplicationContext();

        Toolbar toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.black));
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        MenuItem tools = navigationView.getMenu().findItem(R.id.CommandText);
        SpannableString spanString = new SpannableString(tools.getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.darkGray)), 0, spanString.length(), 0);
        spanString.setSpan(new AbsoluteSizeSpan(18, true), 0, spanString.length(), 0);
        tools.setTitle(spanString);

        navigationView.setItemTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        navigationView.setCheckedItem(R.id.nav_main);
        replaceFragment(new main());

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_switch);
        RadioGroup radioSpeedSelector = menuItem.getActionView().findViewById(R.id.speedSelector);

        radioSpeedSelector.check(R.id.radioBtn0);

        //Listener for drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            //item.setChecked(true);
            drawer.closeDrawer(GravityCompat.START);
            if(id == R.id.nav_main){
                replaceFragment(new main());
            }
            else if(id ==R.id.nav_Connection){
                replaceFragment(new bluetooth());
            }
            else if(id ==R.id.nav_Settings){
                replaceFragment(new settings());
            }
            else if(id ==R.id.nav_Meow){
                if (BluetoothConnectionService.isConnected()) {
                    makeToast("Command: Meow");
                    sendCommand(30);
                }else{
                    makeToast("No Bluetooth connection");
                }
            }
            else if(id ==R.id.nav_WakeUp){
                if (BluetoothConnectionService.isConnected()) {
                    makeToast("Command: WakeUp");
                    main.motorsValue = main.motorValueConstants[0];
                    sendFunction(1);
                }else{
                    makeToast("No Bluetooth connection");
                }
            }
            else if(id ==R.id.nav_Sleep){
                if (BluetoothConnectionService.isConnected()) {
                    makeToast("Command: Sleep");
                    main.motorsValue = main.motorValueConstants[1];
                    sendFunction(5);
                }else{
                    makeToast("No Bluetooth connection");
                }
            }
            else if(id ==R.id.nav_Seat){
                if (BluetoothConnectionService.isConnected()) {
                    makeToast("Command: Seat");
                    main.motorsValue = main.motorValueConstants[2];
                    sendFunction(11);
                }else{
                    makeToast("No Bluetooth connection");
                }
            }
            else if(id ==R.id.nav_StandUp){
                if (BluetoothConnectionService.isConnected()) {
                    makeToast("Command: StandUp");
                    main.motorsValue = main.motorValueConstants[3];
                    sendFunction(4);
                }else{
                    makeToast("No Bluetooth connection");
                }
            }
            else if(id ==R.id.nav_Greats){
                if (BluetoothConnectionService.isConnected()) {
                    makeToast("Command: Greats");
                    main.motorsValue = main.motorValueConstants[4];
                    sendFunction(10);
                }else{
                    makeToast("No Bluetooth connection");
                }
            }
            else if(id ==R.id.nav_Calibration){
                if (BluetoothConnectionService.isConnected()) {
                    makeToast("Command: Calibration");
                    main.motorsValue = main.motorValueConstants[5];
                    sendFunction(3);
                }else{
                    makeToast("No Bluetooth connection");
                }
            }

            return true;
        });

        //Listener for change increment Value
        radioSpeedSelector.setOnCheckedChangeListener((radioGroup, i) -> {
            if(i == R.id.radioBtn1){
                multiplierSpeed = 2;
            }
            else if(i == R.id.radioBtn2){
                multiplierSpeed = 5;
            }
            else{
                multiplierSpeed = 1;
            }

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothConnectionService.cancel();
    }

    //Function for SWAP Fragments
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    static public void makeToast(String text) {
        Toast.makeText(MainActivity.getAppContext(), text, Toast.LENGTH_SHORT).show();

    }
    //send set motor Value Function
    static public void setMotor(int numMotor, int value) {
        byte motorCommand = (byte) 220;
        byte[] bytes = {motorCommand, (byte) numMotor, (byte) value};
        BluetoothConnectionService.write(bytes);
    }


    //send Function Function
    static public void sendFunction(int numFunction) {
        byte functionCommand = (byte) 221;
        byte[] bytes = {functionCommand, (byte) numFunction};
        BluetoothConnectionService.write(bytes);
    }

    //send Command Function
    static public void sendCommand(int command) {
        byte commandCommand = (byte) 222;
        byte[] bytes = {commandCommand, (byte) command};
        BluetoothConnectionService.write(bytes);
    }
    static public void sendCommand(int command,int extra) {
        byte commandCommand = (byte) 222;
        byte[] bytes = {commandCommand, (byte) command, (byte) extra};
        BluetoothConnectionService.write(bytes);
    }
    //Send function On/Off
    static void sendFunctionWrite(int extra) {
        byte functionCommand = (byte) 223;
        byte[] bytes = {functionCommand, (byte) extra};
        BluetoothConnectionService.write(bytes);
    }
    //Send Pitch/Roll value
    static void sendPitchRoll(int pitch, int roll) {
        byte functionCommand = (byte) 223;
        byte[] bytes = {functionCommand, (byte) 2, (byte) pitch, (byte) roll};
        BluetoothConnectionService.write(bytes);
    }
}

