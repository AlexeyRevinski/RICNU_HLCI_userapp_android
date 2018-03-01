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
    private double fx;
    private double fy;
    private double fz;
    private double mx;
    private double my;
    private double mz;

    // Get methods

    public int getOffset() {
        return offset;
    }

    public double getGX(){
        return gyroX;
    }
    public double getGY(){
        return gyroY;
    }
    public double getGZ() {
        return gyroZ;
    }
    public double getAX() {
        return accelX;
    }
    public double getAY() {
        return accelY;
    }
    public double getAZ() {
        return accelZ;
    }
    public double getEM() {
        return encMotor;
    }
    public double getEJ() {
        return encJoint;
    }
    public double getCU() {
        return current;
    }
    public double getFX() { return fx;  }
    public double getFY() {
        return fy;
    }
    public double getFZ() {
        return fz;
    }
    public double getMX() {
        return mx;
    }
    public double getMY() {
        return my;
    }
    public double getMZ() {
        return mz;
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
        fx = 0;
        fy = 0;
        fz = 0;
        mx = 0;
        my = 0;
        mz = 0;
    }

    // Parse through the packet
    public boolean getData(byte [] data){
        int i = data.length-24;
        offset  = ((int)data[0])&0xFF;
        //if (!(offset==0||offset==1)){return false;}
        if (data.length!=25)
        {
            return false;
        }
        if(offset==0) {
            gyroX = twosComplement((double) (((((int) data[i]) & 0xFF) << 8) | (((int) data[i + 1])) & 0xFF), 16) / 164;
            gyroY = twosComplement((double) (((((int) data[i + 2]) & 0xFF) << 8) | (((int) data[i + 3])) & 0xFF), 16) / 164;
            gyroZ = twosComplement((double) (((((int) data[i + 4]) & 0xFF) << 8) | (((int) data[i + 5])) & 0xFF), 16) / 164;
            accelX = twosComplement((double) (((((int) data[i + 6]) & 0xFF) << 8) | (((int) data[i + 7])) & 0xFF), 16) / 8192;
            accelY = twosComplement((double) (((((int) data[i + 8]) & 0xFF) << 8) | (((int) data[i + 9])) & 0xFF), 16) / 8192;
            accelZ = twosComplement((double) (((((int) data[i + 10]) & 0xFF) << 8) | (((int) data[i + 11])) & 0xFF), 16) / 8192;
            encMotor = twosComplement(((double) (((((int) data[i + 12]) & 0xFF) << 24) | ((((int) data[i + 13]) & 0xFF) << 16) | ((((int) data[i + 14]) & 0xFF) << 8) | ((((int) data[i + 15]) & 0xFF)))), 32) * 0.021973;
            encJoint = twosComplement(((double) (((((int) data[i + 16]) & 0xFF) << 24) | ((((int) data[i + 17]) & 0xFF) << 16) | ((((int) data[i + 18]) & 0xFF) << 8) | ((((int) data[i + 19]) & 0xFF)))), 32) * 0.021973;
            current = twosComplement((double) (((((int) data[i + 20]) & 0xFF) << 8) | (((int) data[i + 21])) & 0xFF), 16);
        }
        else if (offset==1)
        {
            fx = ((double)((data[i+0 ]<<24)|(data[i+1 ]<<16)|(data[i+2 ]<<8)|(data[i+3 ])))/10;//TODO
            fy = ((double)((data[i+4 ]<<24)|(data[i+5 ]<<16)|(data[i+6 ]<<8)|(data[i+7 ])))/10;//TODO
            fz = ((double)((data[i+8 ]<<24)|(data[i+9 ]<<16)|(data[i+10]<<8)|(data[i+11])))/10;//TODO
            mx = ((double)((data[i+12]<<24)|(data[i+13]<<16)|(data[i+14]<<8)|(data[i+15])))/1000;//TODO
            my = ((double)((data[i+16]<<24)|(data[i+17]<<16)|(data[i+18]<<8)|(data[i+19])))/1000;//TODO
            mz = ((double)((data[i+20]<<24)|(data[i+21]<<16)|(data[i+22]<<8)|(data[i+23])))/1000;//TODO
        }
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
