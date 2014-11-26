test
====



This project is for "project Haro" for Roboto no kai.

* How to get sensor data.


class test implements SensorEventListener {
    SensorDataComposer sdc;
public void test() {
    sdc = new SensorDataComposer(this);
}
//input sensor data
public void addData(String data) {
   sdc.addData(data);
}
//each call back for Gro, Acc, Mag, Dst.
@Override
public void onGroChanged(int x, int y, int z) {
}
@Override
public void onAccChanged(int x, int y, int z) {
}
@Override
public void onMagChanged(int x, int y, int z) {
}
@Override
public void onDstChanged(int data) {
}
}
