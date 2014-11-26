test
====



This project is for "project Haro" for Roboto no kai. Please refer to the sample code about how to get sensor data.





package com.example.test;

public class test implements SensorEventListener {
    SensorDataComposer sdc;

    public test() {
        sdc = new SensorDataComposer(this);
    }

    // input sensor data
    public void addData(String data) {
        sdc.addData(data);
    }

    public void onGroChanged(int x, int y, int z) {
        // TODO Auto-generated method stub

    }

    public void onAccChanged(int x, int y, int z) {
        // TODO Auto-generated method stub

    }

    public void onMagChanged(int x, int y, int z) {
        // TODO Auto-generated method stub

    }

    public void onDstChanged(int data) {
        // TODO Auto-generated method stub

    }

}
