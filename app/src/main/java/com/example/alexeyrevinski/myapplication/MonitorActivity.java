package com.example.alexeyrevinski.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.content.ServiceConnection;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;

public class MonitorActivity extends AppCompatActivity {

    public final static String ACTION_ENABLE_NOTIFICATIONS = "com.example.alexeyrevinski.myapplication.ACTION_ENABLE_NOTIFICATIONS";

    //private Intent BTServiceIntent;
    //private ServiceConnection mServiceConnection;
    public FlexseaDataClass receivedData;
    public RICNUCommander command;
    public TextView dataView_GX;
    public TextView dataView_GY;
    public TextView dataView_GZ;
    public TextView dataView_AX;
    public TextView dataView_AY;
    public TextView dataView_AZ;
    public TextView dataView_EM;
    public TextView dataView_EJ;
    public TextView dataView_CU;
    private ToggleButton motorButton;
    private ToggleButton directButton;

    //private BluetoothLeService mBluetoothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothLeService.ACTION_DATA_RECEIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter);
        receivedData = new FlexseaDataClass();
        dataView_GX = (TextView) findViewById(R.id.dataText_GX);
        dataView_GY = (TextView) findViewById(R.id.dataText_GY);
        dataView_GZ = (TextView) findViewById(R.id.dataText_GZ);
        dataView_AX = (TextView) findViewById(R.id.dataText_AX);
        dataView_AY = (TextView) findViewById(R.id.dataText_AY);
        dataView_AZ = (TextView) findViewById(R.id.dataText_AZ);
        dataView_EM = (TextView) findViewById(R.id.dataText_EM);
        dataView_EJ = (TextView) findViewById(R.id.dataText_EJ);
        dataView_CU = (TextView) findViewById(R.id.dataText_CU);
        motorButton = (ToggleButton) findViewById(R.id.motorButton);
        directButton = (ToggleButton) findViewById(R.id.directButton);
        command = new RICNUCommander();

        //------------------------------------------------------------------------------------------
        // BUTTON LISTENERS
        //------------------------------------------------------------------------------------------
        // Motor button listener
        motorButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (motorButton.isChecked()) { // If button is checked
                    sendBroadcast(command.setCommandIntent(RICNUCommander.commands.MOTOR_START));
                }
                else{
                    sendBroadcast(command.setCommandIntent(RICNUCommander.commands.MOTOR_STOP));
                }
            }
        });
        // Direction button listener
        directButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (directButton.isChecked()) { // If button is checked
                    sendBroadcast(command.setCommandIntent(RICNUCommander.commands.DIRECTION_BACKWARD));
                }
                else{
                    sendBroadcast(command.setCommandIntent(RICNUCommander.commands.DIRECTION_FORWARD));
                }
            }
        });


        broadcastUpdate(ACTION_ENABLE_NOTIFICATIONS);
    }

    public void     onDestroy() {
        Log.d("MA",getString(R.string.log_xx_on_destroy));
        //unbindService(mServiceConnection);
        //stopService(BTServiceIntent);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    //==============================================================================================
    // BROADCASTING METHODS
    //==============================================================================================
    private void broadcastUpdate(final String action) {     // Broadcast action strings to activity
        final Intent intent = new Intent();
        intent  .setAction(action);
        sendBroadcast(intent);
    }

    /*
    private void broadcastUpdate(final String action, BluetoothDevice device) { // Plus device info
        final Intent intent = new Intent();
        intent  .setAction(action)
                .putExtra("device",device);
        sendBroadcast(intent);
    }
    */



    //==============================================================================================
    // BROADCAST RECEIVER
    //==============================================================================================
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothLeService.ACTION_DATA_RECEIVED.equals(action)) {
                byte [] data = intent.getByteArrayExtra("data");
                if(receivedData.getData(data)){
                    dataView_GX.setText(String.format(Locale.US,"%4.3f",receivedData.getGyroX()));
                    dataView_GY.setText(String.format(Locale.US,"%4.3f",receivedData.getGyroY()));
                    dataView_GZ.setText(String.format(Locale.US,"%4.3f",receivedData.getGyroZ()));
                    dataView_AX.setText(String.format(Locale.US,"%4.3f",receivedData.getAccelX()));
                    dataView_AY.setText(String.format(Locale.US,"%4.3f",receivedData.getAccelY()));
                    dataView_AZ.setText(String.format(Locale.US,"%4.3f",receivedData.getAccelZ()));
                    dataView_EM.setText(String.format(Locale.US,"%4.3f",receivedData.getEncMotor()));
                    dataView_EJ.setText(String.format(Locale.US,"%4.3f",receivedData.getEncJoint()));
                    dataView_CU.setText(String.format(Locale.US,"%4.3f",receivedData.getCurrent()));
                }
            }
        }
    };
}
