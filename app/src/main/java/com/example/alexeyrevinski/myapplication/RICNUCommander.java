package com.example.alexeyrevinski.myapplication;

import android.content.Intent;

/**
 *  Created by Alexey Revinski on 8/2/2017.
 */

class RICNUCommander {

    // COMMANDS
    enum commands {
        MOTOR_START,
        MOTOR_STOP,
        DIRECTION_FORWARD,
        DIRECTION_BACKWARD,
        NULL
    }

    // ACTION STRINGS
    final static String ACTION_WRITE_COMMAND = "com.example.alexeyrevinski.myapplication.ACTION_WRITE_COMMAND";

    // DATA VARIABLES
    private byte[] buffer = {0,0};
    private commands command;

    // CONSTRUCTOR
    RICNUCommander(){
        command = commands.NULL;
        buffer[0] = (byte)0x00;
        buffer[1] = (byte)0x00;
    }

    // SET COMMAND METHOD
    Intent setCommandIntent(commands command){
        // Positional variables
        int bytenum,bitnum;
        byte b1;
        int i1;
        byte b2;
        byte b3;
        // Set command
        this.command = command;
        // Modify buffer to reflect desired commmand
        switch (this.command){
            case MOTOR_STOP:
                bytenum = 0; bitnum = 0;
                buffer[bytenum] =(byte)(buffer[bytenum] &(~(1<<(7-bitnum)))); // Default
                break;
            case MOTOR_START:
                bytenum = 0; bitnum = 0;
                buffer[bytenum] =(byte)(buffer[bytenum] |(1<<(7-bitnum)));
                break;
            case DIRECTION_FORWARD:
                bytenum = 0; bitnum = 1;
                buffer[bytenum] =(byte)(buffer[bytenum] &(~(1<<(7-bitnum)))); // Default
                break;
            case DIRECTION_BACKWARD:
                bytenum = 0; bitnum = 1;
                buffer[bytenum] =(byte)(buffer[bytenum] |(1<<(7-bitnum)));
                break;
        }
        // Create an intent with the buffer as extra
        final Intent intent = new Intent();
        intent.setAction(ACTION_WRITE_COMMAND);
        intent.putExtra("command",buffer);
        return intent;
    }
}
