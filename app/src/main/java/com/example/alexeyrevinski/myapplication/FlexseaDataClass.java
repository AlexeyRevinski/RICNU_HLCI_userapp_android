package com.example.alexeyrevinski.myapplication;

/**
 * Created by Alexey Revinski on 7/26/2017.
 */

public class FlexseaDataClass {
    private int offset;
    private double gyroX;
    private double gyroY;
    private double gyroZ;
    private double accelX;
    private double accelY;
    private double accelZ;
    private double encMotor;
    private double encJoint;
    private double current;

    // Get methods

    public int getOffset() {
        return offset;
    }

    public double getGyroX(){
        return gyroX;
    }
    public double getGyroY(){
        return gyroY;
    }
    public double getGyroZ() {
        return gyroZ;
    }
    public double getAccelX() {
        return accelX;
    }
    public double getAccelY() {
        return accelY;
    }
    public double getAccelZ() {
        return accelZ;
    }
    public double getEncMotor() {
        return encMotor;
    }
    public double getEncJoint() {
        return encJoint;
    }
    public double getCurrent() {
        return current;
    }

    // Constructor
    public FlexseaDataClass(){
        gyroX = 0;
        gyroY = 0;
        gyroZ = 0;
        accelX = 0;
        accelY = 0;
        accelZ = 0;
        encMotor = 0;
        encJoint = 0;
        current = 0;
    }

    // Parse through the packet
    public boolean getData(byte [] data){
        int i = data.length-24;
        offset  = ((int)data[0])&0xFF;
        gyroX = twosComplement((double)(((((int)data[i])&0xFF)<<8)|(((int)data[i+1]))&0xFF),16)/164;
        gyroY = twosComplement((double)(((((int)data[i+2])&0xFF)<<8)|(((int)data[i+3]))&0xFF),16)/164;
        gyroZ = twosComplement((double)(((((int)data[i+4])&0xFF)<<8)|(((int)data[i+5]))&0xFF),16)/164;
        accelX = twosComplement((double)(((((int)data[i+6])&0xFF)<<8)|(((int)data[i+7]))&0xFF),16)/8192;
        accelY = twosComplement((double)(((((int)data[i+8])&0xFF)<<8)|(((int)data[i+9]))&0xFF),16)/8192;
        accelZ = twosComplement((double)(((((int)data[i+10])&0xFF)<<8)|(((int)data[i+11]))&0xFF),16)/8192;
        encMotor = twosComplement(((double)(((((int)data[i+12])&0xFF)<<24)|((((int)data[i+13])&0xFF)<<16)|((((int)data[i+14])&0xFF)<<8)|((((int)data[i+15])&0xFF)))),32)*0.021973;
        encJoint = twosComplement(((double)(((((int)data[i+16])&0xFF)<<24)|((((int)data[i+17])&0xFF)<<16)|((((int)data[i+18])&0xFF)<<8)|((((int)data[i+19])&0xFF)))),32)*0.021973;
        current = twosComplement((double)(((((int)data[i+20])&0xFF)<<8)|(((int)data[i+21]))&0xFF),16);
        return true;
    }

    private int toUnsigned(byte data){
        int result = 0;
        if(data<0)
            result = ((int)data)+255;
        return result;
    }
    private double twosComplement(double data, int size){
        double power1 = Math.pow(2,size-1);
        if(data>power1){
            data = data-(Math.pow(2,size)-1);
        }
        return data;
    }
}
