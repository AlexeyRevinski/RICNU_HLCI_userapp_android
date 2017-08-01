package com.example.alexeyrevinski.myapplication;

//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity {

    //TODO test "NO DEVICES FOUND" scenario - find a spot that doesn't have BLE devices
    //TODO onPause and onResume!!!

    public final static String ACTION_START_SCAN = "com.example.alexeyrevinski.myapplication.ACTION_START_SCAN";
    public final static String ACTION_STOP_SCAN = "com.example.alexeyrevinski.myapplication.ACTION_STOP_SCAN";
    public final static String ACTION_CONNECT = "com.example.alexeyrevinski.myapplication.ACTION_CONNECT";
    public final static String ACTION_DISCONNECT = "com.example.alexeyrevinski.myapplication.ACTION_DISCONNECT";

    //==============================================================================================
    // OBJECT ASSIGNMENTS
    //==============================================================================================
    public StateAp                     APSTATE; // App phase state
    public StateBt                     BTSTATE; // Bluetooth state
    public StateBu                     BUSTATE; // Toggle Button state
    public StateIc                     ICSTATE; // Icon state
    public StateUi                     UISTATE; // User interface state


    private Intent                      BTServiceIntent;
    private Intent                      BTEnableIntent;
    private BluetoothLeService          mBluetoothService;
    private ExtendedServiceConnection   mServiceConnection;
    private BluetoothCustomAdapter      mArrayAdapter;
    private SelectedDevice              mSelectedDevice;


    //==============================================================================================
    // GRAPHICS OBJECTS DECLARATIONS
    //==============================================================================================
    private Button                      singleButton;
    private ImageView                   iconAlert;
    private ImageView                   iconGood;
    private ProgressBar                 iconProg;
    private TextView                    statusText;
    private ToggleButton                scanButton;

    private BluetoothDialogFragment_connect mConnectFragment;

    //==============================================================================================
    // CONSTANTS
    //==============================================================================================
    private final int REQUEST_ENABLE_BT = 1;

    //==============================================================================================
    // ON CREATE
    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        ListView                    listView;
        ArrayList<BluetoothDevice>  mArrayList;

        //------------------------------------------------------------------------------------------
        // SETTING UP GRAPHICS-RELATED OBJECTS
        //------------------------------------------------------------------------------------------
        singleButton        = (Button)          findViewById(R.id.singleButton);
        iconAlert           = (ImageView)       findViewById(R.id.icon_alert);
        iconGood            = (ImageView)       findViewById(R.id.icon_good);
        iconProg            = (ProgressBar)     findViewById(R.id.icon_prog);
        statusText          = (TextView)        findViewById(R.id.statusText);
        scanButton          = (ToggleButton)    findViewById(R.id.toggleButton);
        listView            = (ListView)        findViewById(R.id.BTDeviceList);
        mArrayList          = new ArrayList<>();
        mArrayAdapter       = new BluetoothCustomAdapter(BluetoothActivity.this, mArrayList);
        listView.setAdapter(mArrayAdapter);

         mConnectFragment = new BluetoothDialogFragment_connect();

        //------------------------------------------------------------------------------------------
        // STATE OBJECTS
        //------------------------------------------------------------------------------------------
        APSTATE             = new StateAp();
        BTSTATE             = new StateBt();
        BUSTATE             = new StateBu();
        ICSTATE             = new StateIc();
        UISTATE             = new StateUi();

        //------------------------------------------------------------------------------------------
        // BLUETOOTH-RELATED OBJECTS
        //------------------------------------------------------------------------------------------
        BTServiceIntent     = new Intent(this, BluetoothLeService.class);
        BTEnableIntent      = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mServiceConnection  = new ExtendedServiceConnection();
        mSelectedDevice     = new SelectedDevice();

        //------------------------------------------------------------------------------------------
        // INTENT FILTER SETUP AND BROADCAST RECEIVER REGISTRATION
        //------------------------------------------------------------------------------------------
        IntentFilter  mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothLeService.ACTION_ADAPTER_NOT_FOUND);
        mIntentFilter.addAction(BluetoothLeService.ACTION_BT_NOT_ENABLED);
        mIntentFilter.addAction(BluetoothLeService.ACTION_BT_ENABLED);
        mIntentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        mIntentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        mIntentFilter.addAction(BluetoothLeService.ACTION_MANAGER_NOT_FOUND);
        mIntentFilter.addAction(BluetoothLeService.ACTION_SCAN_FINISHED);
        mIntentFilter.addAction(BluetoothLeService.ACTION_SCAN_STARTED);
        mIntentFilter.addAction(BluetoothLeService.ACTION_DEVICE_FOUND);
        mIntentFilter.addAction(BluetoothLeService.ACTION_GATT_COMPATIBLE);
        mIntentFilter.addAction(BluetoothLeService.ACTION_GATT_INCOMPATIBLE);
        mIntentFilter.addAction(BluetoothDialogFragment_connect.ACTION_CONNECT_CONFIRMED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter);

        //------------------------------------------------------------------------------------------
        // APP PHASE LISTENER
        //------------------------------------------------------------------------------------------
        APSTATE.setOnStateChangeListener(new StateAp.OnStateChangeListener(){
            @Override

            public void onChange() {

                switch (APSTATE.getState()){

                    case AP_INITIAL:
                        Log.d(getString(R.string.TAG_AP),getString(R.string.log_ap_initial));
                        UISTATE.setState(StateUi.states.UI_INITIAL);
                        BTSTATE.setState(StateBt.states.BT_INITIAL);
                        break;

                    case AP_WAITING_TO_SCAN:
                        Log.d(getString(R.string.TAG_AP),getString(R.string.log_ap_waiting_to_scan));
                        BUSTATE.setState(StateBu.states.BU_TOGGLE);
                        break;

                    case AP_WAITING_TO_CONNECT:
                        Log.d(getString(R.string.TAG_AP),getString(R.string.log_ap_waiting_to_connect));
                        BUSTATE.setState(StateBu.states.BU_TOGGLE);
                        break;

                    case AP_SCAN:
                        Log.d(getString(R.string.TAG_AP),getString(R.string.log_ap_scan));
                        scanButton.setTextOff("RE-SCAN");
                        break;

                    case AP_CONNECT:
                        Log.d(getString(R.string.TAG_AP),getString(R.string.log_ap_connect));
                        scanButton.setVisibility(View.INVISIBLE);
                        scanButton.setClickable(false);
                        singleButton.setVisibility(View.VISIBLE);
                        singleButton.setClickable(true);
                        BUSTATE.setState(StateBu.states.BU_SINGLE_CANCEL);
                        break;

                    case AP_CONNECTED:
                        Log.d(getString(R.string.TAG_AP),getString(R.string.log_ap_connected));
                        break;

                    case AP_ERROR:
                        Log.d(getString(R.string.TAG_AP),getString(R.string.log_ap_error));
                        break;
                }
            }

        });

        //------------------------------------------------------------------------------------------
        // BLUETOOTH BLE STATE LISTENER
        //------------------------------------------------------------------------------------------
        BTSTATE.setOnStateChangeListener(new StateBt.OnStateChangeListener() {
            @Override
            public void onChange() {

                switch (BTSTATE.getState()) {

                    case BT_INITIAL:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_initial));
                        startService(BTServiceIntent);      // Starting BluetoothLeService
                        bindService(BTServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                        break;

                    case BT_MANAGER_NOT_FOUND:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_manager_not_found));
                        UISTATE.setState(StateUi.states.UI_BT_NOT_SUPPORTED);
                        break;

                    case BT_ADAPTER_NOT_FOUND:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_adapter_not_found));
                        UISTATE.setState(StateUi.states.UI_BT_NOT_SUPPORTED);
                        break;

                    case BT_NOT_ENABLED:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_not_enabled));
                        UISTATE.setState(StateUi.states.UI_BT_NOT_ENABLED);
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_enable_request));
                        startActivityForResult(BTEnableIntent, REQUEST_ENABLE_BT);
                        break;

                    case BT_ENABLED:                                                                //TODO implement various error states
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_enabled));
                        if(APSTATE.getState()==StateAp.states.AP_INITIAL||
                                (APSTATE.getState()==StateAp.states.AP_WAITING_TO_SCAN&&
                                    UISTATE.getState()!=StateUi.states.UI_BT_NOT_ENABLED)){
                            UISTATE.setState(StateUi.states.UI_SCAN_READY);
                        }
                        else if (APSTATE.getState()==StateAp.states.AP_WAITING_TO_SCAN){
                            UISTATE.setState(StateUi.states.UI_SCAN_FAILED);
                        }
                        else if (APSTATE.getState()==StateAp.states.AP_SCAN){
                            UISTATE.setState(StateUi.states.UI_SCAN_FAILED);
                        }
                        else if (APSTATE.getState()==StateAp.states.AP_WAITING_TO_CONNECT){
                            UISTATE.setState(StateUi.states.UI_SCAN_FAILED);
                        }
                        else if (APSTATE.getState()==StateAp.states.AP_CONNECT){
                            UISTATE.setState(StateUi.states.UI_CONNECT_FAILED);
                        }
                        if(mConnectFragment.isAdded()){
                            mConnectFragment.dismiss();
                        }
                        break;

                    case BT_SCAN_START:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_scan_start));
                        mArrayAdapter.clear();
                        broadcastUpdate(ACTION_START_SCAN);
                        UISTATE.setState(StateUi.states.UI_SCAN_START);
                        break;

                    case BT_SCAN_STOPPED:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_scan_stopped));
                        broadcastUpdate(ACTION_STOP_SCAN);
                        break;

                    case BT_SCAN_FAILED:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_scan_failed));
                        UISTATE.setState(StateUi.states.UI_SCAN_FAILED);
                        break;

                    case BT_SCAN_FINISHED:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_scan_finished));
                        if (UISTATE.getState()!=StateUi.states.UI_SCAN_FINISHED&&
                                UISTATE.getState()!=StateUi.states.UI_SCAN_FINISHED_NONE_FOUND){
                            if(mArrayAdapter.isEmpty()){
                                UISTATE.setState(StateUi.states.UI_SCAN_FINISHED_NONE_FOUND);
                            }
                            else{
                                UISTATE.setState(StateUi.states.UI_SCAN_FINISHED);
                            }
                        }
                        break;

                    case BT_CONNECT:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_connect));
                        broadcastUpdate(ACTION_CONNECT, mArrayAdapter.getItem(mSelectedDevice.getPos()));
                        UISTATE.setState(StateUi.states.UI_CONNECT);
                        break;

                    case BT_GATT_CONNECTED:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_gatt_connected));
                        UISTATE.setState(StateUi.states.UI_CONNECT_SUCCESS);
                        break;

                    case BT_GATT_DISCONNECTED:                                                      //TODO implement - going back to connection screen...
                        break;

                    case BT_GATT_COMPATIBLE:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_gatt_compatible)); //TODO go on to the next activity
                        Intent intent = new Intent(getApplicationContext(), MonitorActivity.class);
                        startActivity(intent);
                        break;

                    case BT_GATT_INCOMPATIBLE:
                        Log.d(getString(R.string.TAG_BT),getString(R.string.log_bt_gatt_incompatible));
                        UISTATE.setState(StateUi.states.UI_INCOMPATIBLE);
                        break;
                }
            }
        });

        //------------------------------------------------------------------------------------------
        // BUTTON STATE LISTENER
        //------------------------------------------------------------------------------------------
        BUSTATE.setOnStateChangeListener(new StateBu.OnStateChangeListener() {
            @Override
            public void onChange() {

                switch (BUSTATE.getState()) {

                    case BU_INITIAL:
                        Log.d(getString(R.string.TAG_BU),getString(R.string.log_bu_initial));
                        BUSTATE.setState(StateBu.states.BU_TOGGLE);
                        break;

                    case BU_CHECKED:
                        Log.d(getString(R.string.TAG_BU),getString(R.string.log_bu_checked));
                        scanButton.setChecked(true);
                        if(APSTATE.getState()==StateAp.states.AP_WAITING_TO_SCAN||
                                APSTATE.getState()==StateAp.states.AP_WAITING_TO_CONNECT){
                            BTSTATE.setState(StateBt.states.BT_SCAN_START);
                        }
                        break;

                    case BU_UNCHECKED:
                        Log.d(getString(R.string.TAG_BU),getString(R.string.log_bu_unchecked));
                        scanButton.setChecked(false);
                        if(APSTATE.getState()==StateAp.states.AP_SCAN&&
                                UISTATE.getState()!=StateUi.states.UI_SCAN_FAILED){ //Only if we were actively scanning
                            BTSTATE.setState(StateBt.states.BT_SCAN_STOPPED);
                        }
                        break;

                    case BU_TOGGLE:
                        Log.d(getString(R.string.TAG_BU),getString(R.string.log_bu_toggle));
                        singleButton.setVisibility(View.INVISIBLE);
                        singleButton.setClickable(false);
                        scanButton.setVisibility(View.VISIBLE);
                        scanButton.setClickable(true);
                        BUSTATE.setState(StateBu.states.BU_UNCHECKED);
                        break;

                    case BU_SINGLE_CANCEL:
                        Log.d(getString(R.string.TAG_BU),getString(R.string.log_bu_single_cancel));
                        singleButton.setVisibility(View.VISIBLE);
                        singleButton.setClickable(true);
                        singleButton.setText(getString(R.string.dialog_BT_but_cancel));
                        scanButton.setVisibility(View.INVISIBLE);
                        scanButton.setClickable(false);
                        break;

                    case BU_SINGLE_EXIT:
                        Log.d(getString(R.string.TAG_BU),getString(R.string.log_bu_single_exit));
                        singleButton.setVisibility(View.VISIBLE);
                        singleButton.setClickable(true);
                        singleButton.setText(getString(R.string.dialog_BT_but_exit));
                        scanButton.setVisibility(View.INVISIBLE);
                        scanButton.setClickable(false);
                        break;

                    case BU_NONE:
                        Log.d(getString(R.string.TAG_BU),getString(R.string.log_bu_none));
                        singleButton.setVisibility(View.INVISIBLE);
                        singleButton.setClickable(false);
                        scanButton.setVisibility(View.INVISIBLE);
                        scanButton.setClickable(false);
                        break;
                }
            }
        });

        //------------------------------------------------------------------------------------------
        // ICON STATE LISTENER
        //------------------------------------------------------------------------------------------
        ICSTATE.setOnStateChangeListener(new StateIc.OnStateChangeListener() {
            @Override
            public void onChange() {

                switch (ICSTATE.getState()) {

                    case IC_INITIAL:
                        Log.d(getString(R.string.TAG_IC),getString(R.string.log_ic_initial));
                        ICSTATE.setState(StateIc.states.IC_GOOD);
                        break;

                    case IC_ALERT:
                        Log.d(getString(R.string.TAG_IC),getString(R.string.log_ic_alert));
                        iconAlert.setVisibility(View.VISIBLE);
                        iconGood.setVisibility(View.INVISIBLE);
                        iconProg.setVisibility(View.INVISIBLE);
                        break;

                    case IC_GOOD:
                        Log.d(getString(R.string.TAG_IC),getString(R.string.log_ic_good));
                        iconAlert.setVisibility(View.INVISIBLE);
                        iconGood.setVisibility(View.VISIBLE);
                        iconProg.setVisibility(View.INVISIBLE);
                        break;

                    case IC_PROGRESS:
                        Log.d(getString(R.string.TAG_IC),getString(R.string.log_ic_progress));
                        iconAlert.setVisibility(View.INVISIBLE);
                        iconGood.setVisibility(View.INVISIBLE);
                        iconProg.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        //------------------------------------------------------------------------------------------
        // USER INTERFACE STATE LISTENER
        //------------------------------------------------------------------------------------------
        UISTATE.setOnStateChangeListener(new StateUi.OnStateChangeListener() {
            @Override
            public void onChange() { // Always do IC, then BU, then BT, then AP

                switch (UISTATE.getState()) {

                    case UI_INITIAL:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_initial));
                        statusText.setText(R.string.state_initial);
                        ICSTATE.setState(StateIc.states.IC_INITIAL);
                        BUSTATE.setState(StateBu.states.BU_INITIAL);
                        break;

                    case UI_BT_NOT_SUPPORTED:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_bt_not_supported));
                        statusText.setText(R.string.state_bt_not_supported);
                        ICSTATE.setState(StateIc.states.IC_ALERT);
                        BUSTATE.setState(StateBu.states.BU_SINGLE_EXIT);
                        APSTATE.setState(StateAp.states.AP_ERROR);
                        break;

                    case UI_BT_NOT_ENABLED:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_bt_not_enabled));
                        statusText.setText(R.string.state_bt_not_enabled);
                        mArrayAdapter.clear();                                                      //TODO when implementing bonded devices
                        ICSTATE.setState(StateIc.states.IC_ALERT);
                        BUSTATE.setState(StateBu.states.BU_NONE);
                        break;

                    case UI_SCAN_READY:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_scan_ready));
                        statusText.setText(R.string.state_scan_ready);
                        ICSTATE.setState(StateIc.states.IC_GOOD);
                        APSTATE.setState(StateAp.states.AP_WAITING_TO_SCAN);
                        break;

                    case UI_SCAN_START:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_scan_start));
                        statusText.setText(R.string.state_scan_process);
                        ICSTATE.setState(StateIc.states.IC_PROGRESS);
                        APSTATE.setState(StateAp.states.AP_SCAN);
                        break;

                    case UI_SCAN_FAILED:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_scan_failed));
                        statusText.setText(R.string.state_scan_interrupted);
                        ICSTATE.setState(StateIc.states.IC_ALERT);
                        APSTATE.setState(StateAp.states.AP_WAITING_TO_SCAN);
                        break;

                    case UI_SCAN_FINISHED:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_scan_finished));
                        statusText.setText(R.string.state_scan_finished);
                        ICSTATE.setState(StateIc.states.IC_GOOD);
                        APSTATE.setState(StateAp.states.AP_WAITING_TO_CONNECT);
                        break;

                    case UI_SCAN_FINISHED_NONE_FOUND:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_scan_finished_none_found));
                        statusText.setText(R.string.state_scan_finished_none_found);
                        ICSTATE.setState(StateIc.states.IC_ALERT);
                        APSTATE.setState(StateAp.states.AP_WAITING_TO_SCAN);
                        break;

                    case UI_CONNECT:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_connect));
                        statusText.setText(getString(R.string.state_connect_process)+": \""+mSelectedDevice.getName()+"\"");
                        ICSTATE.setState(StateIc.states.IC_PROGRESS);
                        APSTATE.setState(StateAp.states.AP_CONNECT);
                        break;

                    case UI_CONNECT_FAILED:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_connect_failed));
                        statusText.setText(R.string.state_connect_fail);
                        ICSTATE.setState(StateIc.states.IC_ALERT);
                        APSTATE.setState(StateAp.states.AP_WAITING_TO_CONNECT);
                        break;

                    case UI_CONNECT_CANCELLED:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_connect_cancelled));
                        statusText.setText(R.string.state_scan_finished);                               //TODO a different status?
                        ICSTATE.setState(StateIc.states.IC_GOOD);
                        APSTATE.setState(StateAp.states.AP_WAITING_TO_CONNECT);
                        break;

                    case UI_CONNECT_SUCCESS:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_connect_success));
                        statusText.setText(getString(R.string.state_connect_done)+": \""+mSelectedDevice.getName()+"\"");
                        ICSTATE.setState(StateIc.states.IC_GOOD);
                        BUSTATE.setState(StateBu.states.BU_NONE);
                        APSTATE.setState(StateAp.states.AP_CONNECTED);
                        break;

                    case UI_INCOMPATIBLE:
                        Log.d(getString(R.string.TAG_UI),getString(R.string.log_ui_incompatible));
                        statusText.setText(R.string.state_incompatible);
                        ICSTATE.setState(StateIc.states.IC_ALERT);
                        BUSTATE.setState(StateBu.states.BU_TOGGLE);
                        APSTATE.setState(StateAp.states.AP_WAITING_TO_CONNECT);
                        break;
                }
            }
        });

        //------------------------------------------------------------------------------------------
        // BUTTON LISTENERS
        //------------------------------------------------------------------------------------------
        // Scan button listener
        scanButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (scanButton.isChecked()) { // If button is checked
                    if(BUSTATE.getState()!=StateBu.states.BU_CHECKED)  //In-software changes ignored
                    {
                        BUSTATE.setState(StateBu.states.BU_CHECKED);
                    }
                }
                else{
                    if(BUSTATE.getState()!=StateBu.states.BU_UNCHECKED)   //In-software changes ignored
                    {
                        BUSTATE.setState(StateBu.states.BU_UNCHECKED);
                    }
                }
            }
        });

        // List view listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (APSTATE.getState()==StateAp.states.AP_SCAN){
                    BTSTATE.setState(StateBt.states.BT_SCAN_STOPPED);
                }
                mSelectedDevice.setPos(position).setName(((TextView)arg1.findViewById(R.id.deviceName)).getText().toString());
                Bundle bundle = new Bundle();
                bundle.putString("name",mSelectedDevice.getName());
                mConnectFragment.setArguments(bundle);
                mConnectFragment.show(getSupportFragmentManager(), "bt_connect_confirm");
            }
        });

        // Cancel/proceed button listener
        singleButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(BUSTATE.getState()==StateBu.states.BU_SINGLE_EXIT){
                    BluetoothActivity.this.finishAffinity(); //Exit application
                }
                else if (BUSTATE.getState()==StateBu.states.BU_SINGLE_CANCEL&&
                        APSTATE.getState()!=StateAp.states.AP_CONNECTED){
                        if(!mArrayAdapter.isEmpty()){
                            UISTATE.setState(StateUi.states.UI_CONNECT_CANCELLED);
                        }
                }
            }
        });


        //------------------------------------------------------------------------------------------
        // INITIALIZING SEQUENCE
        //------------------------------------------------------------------------------------------
        APSTATE.setState(StateAp.states.AP_INITIAL);

    } //END onCreate()



    public void     onDestroy() {
        Log.d(getString(R.string.TAG_XX),getString(R.string.log_xx_on_destroy));
        unbindService(mServiceConnection);
        stopService(BTServiceIntent);
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

    private void broadcastUpdate(final String action, BluetoothDevice device) { // Plus device info
        final Intent intent = new Intent();
        intent  .setAction(action)
                .putExtra("device",device);
        sendBroadcast(intent);
    }

    /*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%%
    %%%                                 BLUETOOTH FUNCTIONALITY
    %%%
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

    //==============================================================================================
    // BLUETOOTH ENABLE REQUEST RESULT CATCH
    //==============================================================================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                Log.d(getString(R.string.TAG_XX),getString(R.string.log_xx_bt_user_denied));
                BluetoothActivity.this.finishAffinity(); //Exit application if request denied
            }
        }
    }
    //==============================================================================================
    // BLE SERVICE CONNECTION IMPLEMENTATION (EXTENDED)
    //==============================================================================================
    private class ExtendedServiceConnection implements ServiceConnection {
        //private boolean isServiceConnected = false;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothLeService.LocalBinder)service).getService();
            //isServiceConnected=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
            //isServiceConnected=false;
        }
        /*
        public boolean isServiceConnected() {
            return isServiceConnected;
        }
        */
    }
    //==============================================================================================
    // BLE SERVICE BROADCAST RECEIVER
    //==============================================================================================
    // Broadcast Receiver for bluetooth states sent by BluetoothLeService

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothLeService.ACTION_ADAPTER_NOT_FOUND.equals(action)) {
                BTSTATE.setState(StateBt.states.BT_ADAPTER_NOT_FOUND);
            }
            else if(BluetoothLeService.ACTION_BT_NOT_ENABLED.equals(action)) {
                BTSTATE.setState(StateBt.states.BT_NOT_ENABLED);
            }
            else if(BluetoothLeService.ACTION_BT_ENABLED.equals(action)) {
                BTSTATE.setState(StateBt.states.BT_ENABLED);
            }
            else if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                BTSTATE.setState(StateBt.states.BT_GATT_CONNECTED);
            }
            else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                BTSTATE.setState(StateBt.states.BT_GATT_DISCONNECTED);
            }
            else if(BluetoothLeService.ACTION_MANAGER_NOT_FOUND.equals(action)) {
                BTSTATE.setState(StateBt.states.BT_MANAGER_NOT_FOUND);
            }
            else if(BluetoothLeService.ACTION_SCAN_FINISHED.equals(action)) {
                BTSTATE.setState(StateBt.states.BT_SCAN_FINISHED);
            }
            /*
            else if(BluetoothLeService.ACTION_SCAN_STARTED.equals(action)) {
                BTSTATE.setState(StateBt.states.BT_SCAN_STARTED);
            }
            */
            else if(BluetoothDialogFragment_connect.ACTION_CONNECT_CONFIRMED.equals(action)){
                BTSTATE.setState(StateBt.states.BT_CONNECT);
            }
            else if(BluetoothLeService.ACTION_DEVICE_FOUND.equals(action)) {//Cannot put into separate state because needed
                BluetoothDevice device = intent.getParcelableExtra("device");
                if (mArrayAdapter.getPosition(device) < 0) {   //If device has not been found yet
                    mArrayAdapter.add(device);
                }
            }
            else if(BluetoothLeService.ACTION_GATT_COMPATIBLE.equals(action)){
                BTSTATE.setState(StateBt.states.BT_GATT_COMPATIBLE);
            }
            else if(BluetoothLeService.ACTION_GATT_INCOMPATIBLE.equals(action)){
                BTSTATE.setState(StateBt.states.BT_GATT_INCOMPATIBLE);
            }
        }
    };
}
