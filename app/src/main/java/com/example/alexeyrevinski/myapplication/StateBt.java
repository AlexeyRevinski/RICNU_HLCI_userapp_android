package com.example.alexeyrevinski.myapplication;

/**
 * Created by Alexey Revinski on 7/19/2017.
 */

public class StateBt {

    public enum states {
        BT_MANAGER_NOT_FOUND,
        BT_ADAPTER_NOT_FOUND,
        BT_NOT_ENABLED,
        BT_ENABLED,
        BT_GATT_CONNECTED,
        BT_GATT_DISCONNECTED,
        BT_GATT_INCOMPATIBLE,
        BT_GATT_COMPATIBLE,
        BT_SCAN_STOPPED,
        BT_SCAN_FINISHED,
        BT_SCAN_START,
        BT_SCAN_FAILED,
        BT_SCAN_STARTED,
        BT_CONNECT,
        BT_INITIAL
    };
    public states state;

    private OnStateChangeListener listener;

    public states getState() {
        return state;
    }

    public void setState(states state) {
        this.state = state;
        if (listener != null) listener.onChange();
    }

    public OnStateChangeListener getListener() {
        return listener;
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.listener = listener;
    }

    public interface OnStateChangeListener {
        void onChange();
    }
}
