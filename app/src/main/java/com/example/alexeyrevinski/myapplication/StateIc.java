package com.example.alexeyrevinski.myapplication;

/**
 * Created by Alexey Revinski on 7/19/2017.
 */

public class StateIc {

    public enum states {
        IC_ALERT,
        IC_GOOD,
        IC_PROGRESS,
        IC_INITIAL
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
