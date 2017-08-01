package com.example.alexeyrevinski.myapplication;

/**
 * Created by Alexey Revinski on 7/18/2017.
 */

public class SelectedDevice {

    private int position;
    private String name;

    public String getName() {
        return name;
    }

    public int getPos() {
        return position;
    }

    public SelectedDevice setPos(int pos) {
        position = pos;
        return this;
    }

    public SelectedDevice setName(String nm) {
        name = nm;
        return this;
    }

    public void setDevice(int position, String name) {
        this.position = position;
        this.name = name;

    }
}