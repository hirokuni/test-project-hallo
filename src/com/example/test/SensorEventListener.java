package com.example.test;

public interface SensorEventListener {
	void onGroChanged(int x, int y, int z);
	void onAccChanged(int x, int y, int z);
	void onMagChanged(int x, int y, int z);
	void onDstChanged(int data);
}
