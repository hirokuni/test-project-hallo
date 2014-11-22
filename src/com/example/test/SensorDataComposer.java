package com.example.test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import android.util.Log;

public class SensorDataComposer {
	private static final String TAG = "SensorDataComposer";
	private StringBuffer mSbf = new StringBuffer();
	private static final String DST = "dst:";
	private static final String GRO = "gro:";
	private static final String ACC = "acc:";
	private static final String MAG = "mag:";

	LinkedList<Integer> mDstLinkedList = new LinkedList<Integer>();
	LinkedList<Integer[]> mGroLinkedList = new LinkedList<Integer[]>();
	LinkedList<Integer[]> mAccLinkedList = new LinkedList<Integer[]>();
	LinkedList<Integer[]> mMagLinkedList = new LinkedList<Integer[]>();

	private SensorEventListener mSensorEventListener;
	private boolean mIsRelease = false;
	private Object mLock = new Object();
	private Object mNotifyLock = new Object();
	private Thread mNotifyThread = new Thread(new Runnable() {

		@Override
		public void run() {
			while (!mIsRelease) {
				try {
					synchronized (mLock) {
						mLock.wait(100);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				synchronized (mNotifyLock) {
					if (mSensorEventListener != null) {
						if (getGroSize() > 0)
							try {
								int[] groValues = getAndRemoveGroData();
								mSensorEventListener.onGroChanged(groValues[0],
										groValues[1], groValues[2]);
							} catch (NoDataException e) {
								e.printStackTrace();
							}
						if (getMagSize() > 0)
							try {
								int[] magValues = getAndRemoveMagData();
								mSensorEventListener.onMagChanged(magValues[0],
										magValues[1], magValues[2]);
							} catch (NoDataException e) {
								e.printStackTrace();
							}
						if (getAccSize() > 0)
							try {
								int[] AccValues = getAndRemoveAccData();
								mSensorEventListener.onAccChanged(AccValues[0],
										AccValues[1], AccValues[2]);
							} catch (NoDataException e) {
								e.printStackTrace();
							}
						if (getDstSize() > 0)
							try {
								int dstValue = getAndRemoveDstData();
								mSensorEventListener.onDstChanged(dstValue);
							} catch (NoDataException e) {
								e.printStackTrace();
							}
					} else
						Log.w(TAG, "listener is null");
				}
			}
			Log.i(TAG, "thread is over");
		}

	});

	public SensorDataComposer(SensorEventListener sel) {
		Log.w(TAG,"create");
		mNotifyThread.start();
		mSensorEventListener = sel;
		if (sel == null)
			Log.w(TAG,"listener is null");
	}

	public void release() {
		mIsRelease = true;
		synchronized (mLock) {
			mLock.notifyAll();
		}

		if (mSensorEventListener != null) {
			synchronized (mNotifyLock) {
				mSensorEventListener = null;
			}
		}

	}

	public void addData(String string) {
		// remove LF
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			if (ch != 0x0a && ch != 0x0c)
				mSbf.append(ch);
		}

		Log.i(TAG, "mSbf : " + mSbf.toString() + " <<<");
		while (mSbf.length() > 4) {

			String type = mSbf.substring(0, 4);

			if (type.equalsIgnoreCase(DST)) {
				boolean isReturn = true;
				for (int i = 0; i < mSbf.length() - 4; i++) {
					String value = mSbf.substring(4, 4 + i + 1);
					try {
						Integer.parseInt(value);
					} catch (NumberFormatException e) {
						value = mSbf.substring(4, 4 + i);
						int val = 0;
						try {
							val = Integer.valueOf(value);
							mDstLinkedList.add(val);
							synchronized (mLock) {
								mLock.notify();
							}
						} catch (NumberFormatException ex) {
							Log.w(TAG, "invalid dst val : " + val);
						}
						mSbf.delete(0, 4 + i);
						isReturn = false;
						break;
					}
				}

				if (isReturn)
					return;
			} else if (type.equalsIgnoreCase(GRO)) {
				if (set3Values(mGroLinkedList) == false)
					return;
			} else if (type.equalsIgnoreCase(ACC)) {
				if (set3Values(mAccLinkedList) == false)
					return;
			} else if (type.equalsIgnoreCase(MAG)) {
				if (set3Values(mMagLinkedList) == false)
					return;
			} else {
				int index = 0;

				if ((index = mSbf.indexOf(DST)) > 0)
					mSbf.delete(0, index);
				else if ((index = mSbf.indexOf(GRO)) > 0)
					mSbf.delete(0, index);
				else if ((index = mSbf.indexOf(ACC)) > 0)
					mSbf.delete(0, index);
				else if ((index = mSbf.indexOf(MAG)) > 0)
					mSbf.delete(0, index);
				else {
					mSbf.delete(0, mSbf.length());
					return;
				}
			}
		}
	}

	// false : data number is not enough. Next data is required.
	private boolean set3Values(LinkedList<Integer[]> dataList) {
		boolean res = false;
		for (int i = 0; i < mSbf.length() - 4; i++) {
			String value = mSbf.substring(4, 4 + i + 1);

			if (!String.valueOf(value.charAt(value.length() - 1))
					.equalsIgnoreCase(":"))
				continue;
			try {
				String[] values = value.split(":");

				// error check
				if (values.length == 0) {
					mSbf.delete(0, mSbf.length());
					return false;
				}
				for (int j = 0; j < values.length; j++) {
					if (values[j] == "") {
						mSbf.delete(0, mSbf.length());
						return false;
					}
				}

				if (values.length == 3) {
					Integer[] groData = new Integer[3];

					Integer.parseInt(values[0]);
					Integer.parseInt(values[1]);
					Integer.parseInt(values[2]);

					groData[0] = Integer.valueOf(values[0]);
					groData[1] = Integer.valueOf(values[1]);
					groData[2] = Integer.valueOf(values[2]);
					synchronized (dataList) {
						dataList.add(groData);
					}
					synchronized (mLock) {
						mLock.notify();
					}
					mSbf.delete(0, 4 + i + 1);
					res = true;
					break;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	public int getDstSize() {
		int res = 0;
		synchronized (mDstLinkedList) {
			res = mDstLinkedList.size();
		}
		return res;
	}

	// this API is for only test. The performance is so bad, it must not be used
	// in the production code!
	public int getDstData(int i) {
		int res = 0;
		synchronized (mDstLinkedList) {
			res = mDstLinkedList.get(i);
		}
		return res;
	}

	public int getAndRemoveDstData() throws NoDataException {
		int res = 0;
		synchronized (mDstLinkedList) {
			if (mDstLinkedList.size() == 0)
				throw new NoDataException("No DST Data");

			res = mDstLinkedList.remove();
		}
		return res;
	}

	public int[] getAndRemoveGroData() throws NoDataException {
		int[] res = new int[3];
		synchronized (mGroLinkedList) {
			if (mGroLinkedList.size() == 0)
				throw new NoDataException("No GRO Data");
			Integer[] val = mGroLinkedList.remove();
			res[0] = val[0].intValue();
			res[1] = val[1].intValue();
			res[2] = val[2].intValue();
		}
		return res;
	}

	public int getStringBufferSize() {
		return mSbf.length();
	}

	public int getGroSize() {
		int res = 0;
		synchronized (mGroLinkedList) {
			res = mGroLinkedList.size();
		}
		return res;
	}

	public int[] getGroData(int i) {
		int[] res = new int[3];
		Integer[] val = mGroLinkedList.get(i);
		res[0] = val[0].intValue();
		res[1] = val[1].intValue();
		res[2] = val[2].intValue();
		return res;
	}

	public int getAccSize() {
		int res = 0;
		synchronized (mAccLinkedList) {
			res = mAccLinkedList.size();
		}
		return res;
	}

	public int[] getAndRemoveAccData() throws NoDataException {
		int[] res = new int[3];
		synchronized (mAccLinkedList) {
			if (mAccLinkedList.size() == 0)
				throw new NoDataException("No ACC Data");
			Integer[] val = mAccLinkedList.remove();
			res[0] = val[0].intValue();
			res[1] = val[1].intValue();
			res[2] = val[2].intValue();
		}
		return res;
	}

	public int getMagSize() {
		int res = 0;
		synchronized (mMagLinkedList) {
			res = mMagLinkedList.size();
		}
		return res;
	}

	public int[] getAndRemoveMagData() throws NoDataException {
		int[] res = new int[3];
		synchronized (mMagLinkedList) {
			if (mMagLinkedList.size() == 0)
				throw new NoDataException("No MAG Data");
			Integer[] val = mMagLinkedList.remove();
			res[0] = val[0].intValue();
			res[1] = val[1].intValue();
			res[2] = val[2].intValue();
		}
		return res;
	}

	public void register(SensorEventListener sensorEventListener) {
		mSensorEventListener = sensorEventListener;
	}

}
