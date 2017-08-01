package com.example.alexeyrevinski.myapplication;

/**
 * Created by Alexey Revinski on 7/19/2017.
 */

public class StateUi {

    public enum states {
        UI_INITIAL,
        UI_BT_NOT_SUPPORTED,
        UI_BT_NOT_ENABLED,
        UI_SCAN_READY,
        UI_SCAN_START,
        UI_SCAN_FAILED,
        UI_SCAN_FINISHED,
        UI_SCAN_FINISHED_NONE_FOUND,
        UI_CONNECT,
        UI_CONNECT_SUCCESS,
        UI_CONNECT_FAILED,
        UI_CONNECT_CANCELLED,
        UI_INCOMPATIBLE
    }

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

