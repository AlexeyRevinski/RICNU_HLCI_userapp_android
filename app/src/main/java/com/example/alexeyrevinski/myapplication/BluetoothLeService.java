package com.example.alexeyrevinski.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 *  Created by Alexey Revinski on 7/17/2017.
 */

public class BluetoothLeService extends Service {


    //==============================================================================================
    // CONSTANTS
    //==============================================================================================
    private static final long SCAN_PERIOD = 10000;

    long prev_start_time;
    long start_time;
    long start_time_process;
    long prev_notify_time = 0;
    long time_notifications;
    long end_time;
    long notify_time;
    long total_time;

    //==============================================================================================
    // OBJECTS
    //==============================================================================================
    private BluetoothManager            mBluetoothManager;
    private BluetoothAdapter            mBluetoothAdapter;
    private BluetoothGatt               mBluetoothGatt;
    public  BluetoothDevice             mSelectedDevice;
    public  BluetoothGattCharacteristic dataCharacteristic;
    public  BluetoothGattDescriptor     clientConfigDescriptor;
    private Handler                     mHandler;
    public  byte[]                      data;

    //==============================================================================================
    // CUSTOM ACTION STRINGS
    //==============================================================================================
    public final static String ACTION_ADAPTER_NOT_FOUND = "com.example.app.ACTION_ADAPTER_NOT_FOUND";
    public final static String ACTION_BT_NOT_ENABLED    = "com.example.app.ACTION_BT_NOT_ENABLED";
    public final static String ACTION_BT_ENABLED        = "com.example.app.ACTION_BT_ENABLED";
    public final static String ACTION_GATT_CONNECTED    = "com.example.app.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.app.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_MANAGER_NOT_FOUND = "com.example.app.ACTION_MANAGER_NOT_FOUND";
    public final static String ACTION_SCAN_FINISHED     = "com.example.app.ACTION_SCAN_FINISHED";
    public final static String ACTION_SCAN_STARTED      = "com.example.app.ACTION_SCAN_STARTED";
    public final static String ACTION_DEVICE_FOUND      = "com.example.app.ACTION_DEVICE_FOUND";
    public final static String ACTION_GATT_INCOMPATIBLE = "com.example.app.ACTION_GATT_INCOMPATIBLE";
    public final static String ACTION_GATT_COMPATIBLE   = "com.example.app.ACTION_GATT_COMPATIBLE";
    public final static String ACTION_DATA_RECEIVED     = "com.example.app.ACTION_DATA_RECEIVED";

    //==============================================================================================
    // RICNU SYSTEM UUIDS
    //==============================================================================================
    public final static UUID  UUID_RICNU_GenAccs  = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public final static UUID  UUID_RICNU_DevInfo  = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public final static UUID  UUID_RICNU_FlxServ  = UUID.fromString("be6545d8-67ef-11e7-907b-a6006ad3dba0");

    //==============================================================================================
    // RICNU SYSTEM UUIDS
    //==============================================================================================
    public final static UUID  UUID_RICNU_DevName  = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    public final static UUID  UUID_RICNU_AppChar  = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
    public final static UUID  UUID_RICNU_ManName  = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public final static UUID  UUID_RICNU_ModNumb  = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    public final static UUID  UUID_RICNU_SystmId  = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb");
    public final static UUID  UUID_RICNU_FlxData  = UUID.fromString("a0d6b998-67ef-11e7-907b-a6006ad3dba0");

    public final static UUID  UUID_CCCD             = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    public final static int COMPAT_NUM = 9;


    //==============================================================================================
    // SERVICE ASSESS OBJECTS
    //==============================================================================================
    private final IBinder mBinder = new LocalBinder();      // New binder object
    public class LocalBinder extends Binder {              // Returns binding activity
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    //==============================================================================================
    // ENTRY/EXIT CALLBACK METHODS
    //==============================================================================================
    @Override
    public IBinder  onBind(Intent intent) {
        Log.d(getString(R.string.TAG_BS), "Service bound");
        return mBinder;
    }
    @Override
    public int      onStartCommand(Intent intent, int flags, int startId) {
        Log.d(getString(R.string.TAG_BS), "Service started");
        return START_NOT_STICKY;
    }
    @Override
    public void     onDestroy() {

        disconnectLeGatt(); // Disconnect GATT

        try { // Close GATT
            mBluetoothGatt.close();
        }
        catch (NullPointerException e){
            Log.d("BS", "No gatt connections left to close.");
        }

        try { // Unregister receiver
            unregisterReceiver(mReceiver);
        }
        catch(IllegalArgumentException e){
            Log.d("BS", "No receivers left to unregister.");
        }

        Log.d(getString(R.string.TAG_BS), "Service destroyed");
        stopSelf();
        super.onDestroy();
    }

    @Override
    public void     onCreate(){
        IntentFilter mIntentFilter = new IntentFilter();     // Register receiver
        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mIntentFilter.addAction(BluetoothDevice .ACTION_FOUND);
        mIntentFilter.addAction(BluetoothActivity.ACTION_START_SCAN);
        mIntentFilter.addAction(BluetoothActivity.ACTION_STOP_SCAN);
        mIntentFilter.addAction(BluetoothActivity.ACTION_CONNECT);
        mIntentFilter.addAction(MonitorActivity.ACTION_ENABLE_NOTIFICATIONS);
        mIntentFilter.addAction(RICNUCommander.ACTION_WRITE_COMMAND);
        this.registerReceiver(mReceiver, mIntentFilter);
        mHandler = new Handler();
        Log.d(getString(R.string.TAG_BS), "Service created");
        initialize(this);
    }

    //==============================================================================================
    // BROADCASTING METHODS
    //==============================================================================================
    private void broadcastUpdate(final String action) {     // Broadcast action strings to activity
        final Intent intent = new Intent();
        intent.setAction(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(BluetoothDevice device) {
        final Intent intent = new Intent();
        intent  .setAction(ACTION_DEVICE_FOUND)
                .putExtra("device",device);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(byte[] data){
        final Intent intent = new Intent();
        intent  .setAction(ACTION_DATA_RECEIVED)
                .putExtra("data",data)
                .putExtra("time",total_time)
                .putExtra("start",start_time_process)
                .putExtra("notifications",time_notifications);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //==============================================================================================
    // BLUETOOTH SERVICE INITIALIZE METHOD (CHECKS, RECEIVER REGISTRATION)
    //==============================================================================================
    public void initialize(Context context) {      // Check for bluetooth compatibility and status
        //------------------------------------------------------------------------------------------
        // BLUETOOTH MANAGER CHECK //TODO implement Lollipop and up
        //------------------------------------------------------------------------------------------
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.d("BS","BluetoothManager not found... Requesting safe exit");
                broadcastUpdate(ACTION_MANAGER_NOT_FOUND);
                stopSelf();
            }
            else {
                //----------------------------------------------------------------------------------
                // BLUETOOTH ADAPTER CHECK //TODO implement Lollipop and up
                //----------------------------------------------------------------------------------
                if (mBluetoothAdapter == null) {
                    mBluetoothAdapter = mBluetoothManager.getAdapter();
                    if (mBluetoothAdapter == null) {
                        Log.d("BS","BluetoothAdapter not found... Requesting safe exit");
                        broadcastUpdate(ACTION_ADAPTER_NOT_FOUND);
                        stopSelf();
                    }
                    else {
                        //--------------------------------------------------------------------------
                        // BLUETOOTH ENABLED CHECK //TODO implement Lollipop and up
                        //--------------------------------------------------------------------------
                        if (!mBluetoothAdapter.isEnabled()) {
                            broadcastUpdate(ACTION_BT_NOT_ENABLED);
                        }
                        else {
                            broadcastUpdate(ACTION_BT_ENABLED);
                        }
                    }
                }
            }
        }
    }

    //==============================================================================================
    // LOW ENERGY SCAN METHOD //TODO implement post-Lollipop methods
    //==============================================================================================
    private void startScan(){
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d("BS", "Scan started!");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d("BS", "LOLLIPOP or higher detected!");
                //mBluetoothAdapter.getBluetoothLeScanner().startScan(filterList, scanSetting, scanCallback);
            } else {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
            mHandler.postDelayed(timerLeScan, SCAN_PERIOD);
        }
        else {
            Log.d("BS", "BLE not supported.");
        }
    }

    private void stopScan() {

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d("BS", "Scan stopped!");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d("BS", "LOLLIPOP or higher detected!");
                //mBluetoothAdapter.getBluetoothLeScanner().stopScan(filterList, scanSetting, scanCallback);
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            mHandler.removeCallbacks(timerLeScan);
            broadcastUpdate(ACTION_SCAN_FINISHED);
        }
        else {
            Log.d("BS", "BLE not supported.");
        }
    }

    final Runnable timerLeScan = new Runnable() {
        public void run() {
            Log.d("BS", "Scan timeout reached:"+" "+Objects.toString(SCAN_PERIOD/1000, null)+" seconds");
            stopScan();
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    broadcastUpdate(device);
                }
            };

    //==============================================================================================
    // BLE GATT METHODS
    //==============================================================================================
    public void connectLeGatt(BluetoothDevice device) {
        try {
            Log.d("BS","Severing any previous connections...");
            mBluetoothGatt.disconnect();
        }
        catch(NullPointerException e){
            Log.d("BS","No previous connection detected!");
        }
        Log.d("BS","Connecting to: \""+device.getName()+"\"");
        mSelectedDevice = device;
        mBluetoothGatt = mSelectedDevice.connectGatt(this, false, mGattCallback);
    }

    public void disconnectLeGatt() {
        try {
            mBluetoothGatt.disconnect();
        }
        catch(NullPointerException e) {
            Log.d("BS","No gatt devices to disconnect from.");
        }
    }

    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d("BS", "Connected to GATT server.");
                        broadcastUpdate(ACTION_GATT_CONNECTED);
                        mBluetoothGatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.d("BS", "Disconnected from GATT server.");
                        broadcastUpdate(ACTION_GATT_DISCONNECTED);
                    }
                }
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    List<BluetoothGattService> services = gatt.getServices();
                    checkGattCompatibility(services);
                }
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
                    start_time_process = System.nanoTime();
                    notify_time = System.nanoTime();
                    if(prev_notify_time==0){prev_notify_time=notify_time;}
                    if(start_time!=prev_start_time){
                        end_time = notify_time;
                        total_time = end_time-start_time;
                        prev_start_time = start_time;
                    }
                    time_notifications = notify_time-prev_notify_time;
                    prev_notify_time = notify_time;
                    if(characteristic.getUuid().equals(UUID_RICNU_FlxData)){
                        data = characteristic.getValue();
                        broadcastUpdate(data);
                    }
                }
            };


    public void checkGattCompatibility(List<BluetoothGattService> services) {
        int compat = 0;
        for (BluetoothGattService service : services){
            if(service.getUuid().equals(UUID_RICNU_GenAccs)) {
                compat++;                                                                       //Generic Access Service exists
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics){
                    if(characteristic.getUuid().equals(UUID_RICNU_DevName)){compat++;}        //Device Name Characteristic exists
                    else if(characteristic.getUuid().equals(UUID_RICNU_AppChar)){compat++;}   //Appearance Characteristic exists
                }
            }
            else if(service.getUuid().equals(UUID_RICNU_DevInfo)) {
                compat++;                                                                       //Device Information Service exists
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics){
                    if(characteristic.getUuid().equals(UUID_RICNU_ManName)){compat++;}        //Manufacturer Name Characteristic exists
                    else if(characteristic.getUuid().equals(UUID_RICNU_ModNumb)){compat++;}   //Model Number Characteristic exists
                    else if(characteristic.getUuid().equals(UUID_RICNU_SystmId)){compat++;}   //System ID Characteristic exists
                }
            }
            else if(service.getUuid().equals(UUID_RICNU_FlxServ)) {
                compat++;                                                                       //RICNU Data Service exists
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics){
                    if(characteristic.getUuid().equals(UUID_RICNU_FlxData)){compat++;}        //RICNU Data Characteristic exists
                }
            }
        }
        if(compat!=COMPAT_NUM){
            Log.d("BS","DEVICE IS NOT COMPATIBLE!");
            broadcastUpdate(ACTION_GATT_INCOMPATIBLE);
            disconnectLeGatt();
        }
        else {
            Log.d("BS","DEVICE IS COMPATIBLE!");
            broadcastUpdate(ACTION_GATT_COMPATIBLE);
        }
    }

    //==============================================================================================
    // BROADCAST RECEIVER SETUP
    //==============================================================================================
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // Broadcast Receiver for bluetooth states
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                broadcastUpdate(ACTION_SCAN_FINISHED);
            }

            /* // For Bluetooth Classic only!
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                broadcastUpdate(device);
            }
            */

            else if (BluetoothActivity.ACTION_START_SCAN.equals(action)) {
                Log.d("BS","Received request to start scan. Starting scan...");
                startScan();
            }

            else if (BluetoothActivity.ACTION_STOP_SCAN.equals(action)) {
                Log.d("BS","Received request to stop scan. Stopping scan...");
                stopScan();
            }

            else if (BluetoothActivity.ACTION_CONNECT.equals(action)) {
                Log.d("BS","Received request to connect. Connecting...");
                BluetoothDevice device = intent.getParcelableExtra("device");
                connectLeGatt(device);
            }

            else if (BluetoothActivity.ACTION_DISCONNECT.equals(action)) {
                Log.d("BS","Received request to disconnect. Disonnecting...");
                disconnectLeGatt();
            }

            else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_ON:
                        broadcastUpdate(ACTION_BT_ENABLED);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        broadcastUpdate(ACTION_BT_NOT_ENABLED);
                        break;
                }
            }

            else if (MonitorActivity.ACTION_ENABLE_NOTIFICATIONS.equals(action)) {
                Log.d("BS","Received request to start data stream. Initiating stream...");
                dataCharacteristic = mBluetoothGatt.getService(UUID_RICNU_FlxServ).getCharacteristic(UUID_RICNU_FlxData);
                mBluetoothGatt.setCharacteristicNotification(dataCharacteristic, true);
                clientConfigDescriptor = dataCharacteristic.getDescriptor(UUID_CCCD);
                clientConfigDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(clientConfigDescriptor);
            }

            else if (RICNUCommander.ACTION_WRITE_COMMAND.equals(action)){
                Log.d("BS","Writing command!");
                dataCharacteristic = mBluetoothGatt.getService(UUID_RICNU_FlxServ).getCharacteristic(UUID_RICNU_FlxData);
                dataCharacteristic.setValue(intent.getByteArrayExtra("command"));
                start_time = System.nanoTime();
                dataCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                mBluetoothGatt.writeCharacteristic(dataCharacteristic);
            }
        }
    };
}
