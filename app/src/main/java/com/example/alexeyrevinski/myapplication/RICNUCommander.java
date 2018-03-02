package com.example.alexeyrevinski.myapplication;

import android.content.Intent;

/**
 *  Created by Alexey Revinski on 8/2/2017.
 */

class RICNUCommander {

    //TODO

    // Need:
    // Calibrate, start/stop, relax leg, log/don't log

    // 0    start       1     stop        0
    // 1    log         1     don't log   0
    // 2+3  3 = Calibrate
    //      2 = Relax Leg
    //      1 = Stiff Leg
    //      0 = no command


    // COMMANDS
    enum commands {
        START,
        STOP,
        //DIRECTION_FORWARD,
        //DIRECTION_BACKWARD,
        LOGON,
        LOGOFF,
        CALIBRATE,
        RELAX,
        STIFF,
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
            case STOP:
                bytenum = 0; bitnum = 0;
                buffer[bytenum] =(byte)(buffer[bytenum] &(~(1<<(7-bitnum)))); // Default
                break;
            case START:
                bytenum = 0; bitnum = 0;
                buffer[bytenum] =(byte)(buffer[bytenum] |(1<<(7-bitnum)));
                break;
            case LOGOFF:
                bytenum = 0; bitnum = 1;
                buffer[bytenum] =(byte)(buffer[bytenum] &(~(1<<(7-bitnum)))); // Default
                break;
            case LOGON:
                bytenum = 0; bitnum = 1;
                buffer[bytenum] =(byte)(buffer[bytenum] |(1<<(7-bitnum)));
                break;
            case CALIBRATE:
                bytenum = 0;
                buffer[bytenum] =(byte)(buffer[bytenum]&(~0x30));
                buffer[bytenum] =(byte)(buffer[bytenum]|0x30);
                break;
            case RELAX:
                bytenum = 0;
                buffer[bytenum] =(byte)(buffer[bytenum]&(~0x30));
                buffer[bytenum] =(byte)(buffer[bytenum]|0x20);
                break;
            case STIFF:
                bytenum = 0;
                buffer[bytenum] =(byte)(buffer[bytenum]&(~0x30));
                buffer[bytenum] =(byte)(buffer[bytenum]|0x10);
                break;
        }
        // Create an intent with the buffer as extra
        final Intent intent = new Intent();
        intent.setAction(ACTION_WRITE_COMMAND);
        byte[] tempbuffer = {0,0};
        tempbuffer[0] = buffer[0];
        tempbuffer[1] = buffer[1];
        intent.putExtra("command",tempbuffer);
        switch (this.command) {                         // Clear specific command field
            case CALIBRATE:
            case RELAX:
            case STIFF:
                bytenum = 0;
                buffer[bytenum] =(byte)(buffer[bytenum]&(~0x30));
                break;
        }
        return intent;
    }
}
