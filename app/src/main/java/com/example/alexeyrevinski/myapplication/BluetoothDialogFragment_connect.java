package com.example.alexeyrevinski.myapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.widget.ToggleButton;

/**
 * Created by Alexey Revinski on 7/7/2017.
 */

public class BluetoothDialogFragment_connect extends android.support.v4.app.DialogFragment {

    public final static String ACTION_CONNECT_CONFIRMED = "com.example.alexeyrevinski.myapplication.ACTION_START_SCAN";
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get device name
        String mString = getArguments().getString("name");
        String prompt;
        if (!mString.equals("")) {
            prompt = getString(R.string.dialog_BT_connect_to_device)+" \""+mString+"\""+getString(R.string.dialog_BT_q_mark);
        }
        else {
            prompt = getString(R.string.dialog_BT_connect_to_unnamed_device);
        }
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder .setMessage(prompt)
                .setPositiveButton(R.string.dialog_BT_but_connect, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ToggleButton scanButton = (ToggleButton) getActivity().findViewById(R.id.toggleButton);
                        broadcastUpdate(ACTION_CONNECT_CONFIRMED);
                    }
                })
                .setNegativeButton(R.string.dialog_BT_but_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ;
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    //==============================================================================================
    // BROADCASTING METHODS
    //==============================================================================================
    private void broadcastUpdate(final String action) {     // Broadcast action strings to activity
        final Intent intent = new Intent();
        intent.setAction(action);
        LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(intent);
    }
}
